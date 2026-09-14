package ro.tachistoscop.app;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class RecallScorer {
    static final class Result {
        final int exactPercent;
        final int wordsPercent;
        final int orderPercent;
        final boolean perfect;

        Result(int exactPercent, int wordsPercent, int orderPercent, boolean perfect) {
            this.exactPercent = exactPercent;
            this.wordsPercent = wordsPercent;
            this.orderPercent = orderPercent;
            this.perfect = perfect;
        }

        int scoreForMode(boolean adLitteram) {
            if (adLitteram) return exactPercent;
            return Math.round(wordsPercent * 0.7f + orderPercent * 0.3f);
        }
    }

    static Result score(String expectedRaw, String actualRaw) {
        String expectedExact = normalizeSpaces(expectedRaw);
        String actualExact = normalizeSpaces(actualRaw);
        boolean perfect = expectedExact.equals(actualExact);
        int exact = perfect ? 100 : similarityPercent(expectedExact, actualExact);

        String expected = expectedExact.toLowerCase(Locale.ROOT);
        String actual = actualExact.toLowerCase(Locale.ROOT);
        String[] e = expected.isEmpty() ? new String[0] : expected.split(" ");
        String[] a = actual.isEmpty() ? new String[0] : actual.split(" ");

        int words = wordRecallPercent(e, a);
        int order = orderPercent(e, a);
        return new Result(exact, words, order, perfect);
    }

    private static String normalizeSpaces(String text) {
        if (text == null) return "";
        return text.trim().replaceAll("\\s+", " ");
    }

    private static int similarityPercent(String expected, String actual) {
        int max = Math.max(expected.length(), actual.length());
        if (max == 0) return 100;
        int distance = levenshtein(expected, actual);
        return Math.max(0, Math.round((1f - (float) distance / max) * 100f));
    }

    private static int wordRecallPercent(String[] expected, String[] actual) {
        if (expected.length == 0) return actual.length == 0 ? 100 : 0;
        Map<String, Integer> counts = new HashMap<>();
        for (String word : expected) counts.put(word, counts.getOrDefault(word, 0) + 1);
        int matched = 0;
        for (String word : actual) {
            int left = counts.getOrDefault(word, 0);
            if (left > 0) {
                matched++;
                counts.put(word, left - 1);
            }
        }
        return Math.round((float) matched / expected.length * 100f);
    }

    private static int orderPercent(String[] expected, String[] actual) {
        if (expected.length == 0) return actual.length == 0 ? 100 : 0;
        int[][] dp = new int[expected.length + 1][actual.length + 1];
        for (int i = 1; i <= expected.length; i++) {
            for (int j = 1; j <= actual.length; j++) {
                if (expected[i - 1].equals(actual[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return Math.round((float) dp[expected.length][actual.length] / expected.length * 100f);
    }

    private static int levenshtein(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) previous[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }
}
