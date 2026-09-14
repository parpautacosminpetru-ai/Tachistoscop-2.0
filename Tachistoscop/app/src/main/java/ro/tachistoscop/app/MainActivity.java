package ro.tachistoscop.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int FIXATION_MS = 650;
    private static final int MASK_MS = 100;

    private final int[] exposureValues = {50, 75, 100, 150, 200, 300, 500, 750, 1000};
    private final int[] retentionValues = {0, 250, 500, 1000, 2000, 4000, 8000};
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StimulusGenerator stimulusGenerator = new StimulusGenerator();

    private Spinner quantitySpinner;
    private Spinner exposureSpinner;
    private Spinner retentionSpinner;
    private Spinner modeSpinner;
    private Switch autoSwitch;
    private TextView trainingText;
    private TextView resultText;
    private TextView statsText;
    private EditText answerInput;
    private Button actionButton;

    private String currentStimulus = "";
    private boolean waitingForAnswer = false;
    private int attempts = 0;
    private int totalScore = 0;
    private int bestScore = 0;
    private int successStreak = 0;
    private int failStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(250, 250, 250));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(this);
        title.setText("Tachistoscop");
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.rgb(20, 20, 20));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Percepție rapidă • memorie instant • ad litteram");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, dp(2), 0, dp(14));
        root.addView(subtitle);

        statsText = new TextView(this);
        statsText.setText("0 probe");
        statsText.setTextSize(14);
        statsText.setTextColor(Color.DKGRAY);
        statsText.setPadding(0, 0, 0, dp(12));
        root.addView(statsText);

        quantitySpinner = addSpinner(root, "Cantitate", new String[]{
                "1 literă", "1 cuvânt", "2 cuvinte", "3 cuvinte", "4 cuvinte",
                "5 cuvinte", "6 cuvinte", "7 cuvinte", "8 cuvinte", "9 cuvinte", "10 cuvinte"
        });
        quantitySpinner.setSelection(1);

        exposureSpinner = addSpinner(root, "Expunere", new String[]{
                "50 ms", "75 ms", "100 ms", "150 ms", "200 ms", "300 ms", "500 ms", "750 ms", "1000 ms"
        });
        exposureSpinner.setSelection(4);

        retentionSpinner = addSpinner(root, "Memorie", new String[]{
                "imediat", "0,25 s", "0,5 s", "1 s", "2 s", "4 s", "8 s"
        });
        retentionSpinner.setSelection(2);

        modeSpinner = addSpinner(root, "Mod", new String[]{
                "Memorie instant", "Ad litteram"
        });

        autoSwitch = new Switch(this);
        autoSwitch.setText("AUTO — adaptează dificultatea");
        autoSwitch.setTextSize(16);
        autoSwitch.setPadding(0, dp(8), 0, dp(10));
        root.addView(autoSwitch);

        trainingText = new TextView(this);
        trainingText.setText("+");
        trainingText.setGravity(Gravity.CENTER);
        trainingText.setTextSize(48);
        trainingText.setTextColor(Color.BLACK);
        trainingText.setTypeface(Typeface.DEFAULT_BOLD);
        trainingText.setMinHeight(dp(220));
        trainingText.setPadding(dp(8), dp(24), dp(8), dp(24));
        root.addView(trainingText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        answerInput = new EditText(this);
        answerInput.setHint("Scrie exact ce ai văzut");
        answerInput.setTextSize(18);
        answerInput.setSingleLine(false);
        answerInput.setMaxLines(3);
        answerInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        answerInput.setVisibility(View.GONE);
        root.addView(answerInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        actionButton = new Button(this);
        actionButton.setText("START");
        actionButton.setTextSize(18);
        actionButton.setAllCaps(false);
        actionButton.setOnClickListener(v -> {
            if (waitingForAnswer) submitAnswer();
            else startTrial();
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        buttonParams.topMargin = dp(12);
        root.addView(actionButton, buttonParams);

        resultText = new TextView(this);
        resultText.setTextSize(16);
        resultText.setTextColor(Color.rgb(30, 30, 30));
        resultText.setPadding(0, dp(16), 0, 0);
        root.addView(resultText);

        setContentView(scroll);
    }

    private Spinner addSpinner(LinearLayout root, String label, String[] values) {
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(13);
        labelView.setTypeface(Typeface.DEFAULT_BOLD);
        labelView.setTextColor(Color.DKGRAY);
        labelView.setPadding(0, dp(6), 0, dp(2));
        root.addView(labelView);

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                values
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        root.addView(spinner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        ));
        return spinner;
    }

    private void startTrial() {
        handler.removeCallbacksAndMessages(null);
        currentStimulus = stimulusGenerator.generate(quantitySpinner.getSelectedItemPosition());
        waitingForAnswer = false;
        answerInput.setText("");
        answerInput.setVisibility(View.GONE);
        resultText.setText("");
        actionButton.setEnabled(false);
        actionButton.setText("…");
        setControlsEnabled(false);

        trainingText.setTextSize(48);
        trainingText.setText("+");
        handler.postDelayed(this::showStimulus, FIXATION_MS);
    }

    private void showStimulus() {
        int words = currentStimulus.trim().isEmpty() ? 0 : currentStimulus.trim().split("\\s+").length;
        if (quantitySpinner.getSelectedItemPosition() == 0) trainingText.setTextSize(58);
        else if (words >= 7) trainingText.setTextSize(30);
        else if (words >= 4) trainingText.setTextSize(36);
        else trainingText.setTextSize(44);

        trainingText.setText(currentStimulus);
        int exposureMs = exposureValues[exposureSpinner.getSelectedItemPosition()];
        handler.postDelayed(this::showMask, exposureMs);
    }

    private void showMask() {
        trainingText.setTextSize(34);
        trainingText.setText("✦ ✦ ✦ ✦ ✦ ✦ ✦");
        handler.postDelayed(() -> {
            trainingText.setText("");
            int retentionMs = retentionValues[retentionSpinner.getSelectedItemPosition()];
            handler.postDelayed(this::showRecall, retentionMs);
        }, MASK_MS);
    }

    private void showRecall() {
        trainingText.setTextSize(24);
        trainingText.setText("Ce ai văzut?");
        answerInput.setVisibility(View.VISIBLE);
        answerInput.requestFocus();
        actionButton.setEnabled(true);
        actionButton.setText("VERIFICĂ");
        waitingForAnswer = true;

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(answerInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void submitAnswer() {
        RecallScorer.Result score = RecallScorer.score(currentStimulus, answerInput.getText().toString());
        boolean adLitteram = modeSpinner.getSelectedItemPosition() == 1;
        int overall = score.scoreForMode(adLitteram);

        attempts++;
        totalScore += overall;
        bestScore = Math.max(bestScore, overall);

        StringBuilder result = new StringBuilder();
        if (score.perfect) {
            result.append("✓ Exact — 100%\n");
        } else if (adLitteram) {
            result.append(String.format(Locale.ROOT, "Ad litteram: %d%%\n", score.exactPercent));
        } else {
            result.append(String.format(Locale.ROOT, "Memorie: %d%%\n", overall));
        }
        result.append(String.format(
                Locale.ROOT,
                "Litere: %d%%  •  Cuvinte: %d%%  •  Ordine: %d%%\n",
                score.exactPercent,
                score.wordsPercent,
                score.orderPercent
        ));
        result.append("Țintă: ").append(currentStimulus);

        String adaptation = "";
        if (autoSwitch.isChecked()) adaptation = adapt(overall);
        if (!adaptation.isEmpty()) result.append("\n").append(adaptation);

        resultText.setText(result.toString());
        updateStats();

        waitingForAnswer = false;
        actionButton.setText("URMĂTOAREA");
        setControlsEnabled(true);
        answerInput.clearFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(answerInput.getWindowToken(), 0);
    }

    private String adapt(int score) {
        if (score >= 90) {
            successStreak++;
            failStreak = 0;
            if (successStreak >= 3) {
                successStreak = 0;
                return increaseDifficulty() ? "AUTO: dificultate crescută" : "AUTO: nivel maxim";
            }
        } else if (score < 60) {
            failStreak++;
            successStreak = 0;
            if (failStreak >= 2) {
                failStreak = 0;
                return decreaseDifficulty() ? "AUTO: dificultate redusă" : "AUTO: nivel minim";
            }
        } else {
            successStreak = 0;
            failStreak = 0;
        }
        return "";
    }

    private boolean increaseDifficulty() {
        int quantity = quantitySpinner.getSelectedItemPosition();
        if (quantity < quantitySpinner.getCount() - 1) {
            quantitySpinner.setSelection(quantity + 1);
            return true;
        }
        int exposure = exposureSpinner.getSelectedItemPosition();
        if (exposure > 0) {
            exposureSpinner.setSelection(exposure - 1);
            return true;
        }
        int retention = retentionSpinner.getSelectedItemPosition();
        if (retention < retentionSpinner.getCount() - 1) {
            retentionSpinner.setSelection(retention + 1);
            return true;
        }
        return false;
    }

    private boolean decreaseDifficulty() {
        int retention = retentionSpinner.getSelectedItemPosition();
        if (retention > 0) {
            retentionSpinner.setSelection(retention - 1);
            return true;
        }
        int exposure = exposureSpinner.getSelectedItemPosition();
        if (exposure < exposureSpinner.getCount() - 1) {
            exposureSpinner.setSelection(exposure + 1);
            return true;
        }
        int quantity = quantitySpinner.getSelectedItemPosition();
        if (quantity > 0) {
            quantitySpinner.setSelection(quantity - 1);
            return true;
        }
        return false;
    }

    private void updateStats() {
        int average = attempts == 0 ? 0 : Math.round((float) totalScore / attempts);
        statsText.setText(String.format(
                Locale.ROOT,
                "%d probe  •  medie %d%%  •  maxim %d%%",
                attempts,
                average,
                bestScore
        ));
    }

    private void setControlsEnabled(boolean enabled) {
        quantitySpinner.setEnabled(enabled);
        exposureSpinner.setEnabled(enabled);
        retentionSpinner.setEnabled(enabled);
        modeSpinner.setEnabled(enabled);
        autoSwitch.setEnabled(enabled);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
