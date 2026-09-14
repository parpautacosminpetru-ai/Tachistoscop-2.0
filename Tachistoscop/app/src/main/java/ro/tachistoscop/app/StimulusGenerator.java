package ro.tachistoscop.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

final class StimulusGenerator {
    private static final String LETTERS = "abcdefghijklmnopqrstuvxyzăâîșț";

    private static final String[] CORPUS = {
            "dimineața liniștită aduce lumină peste casele vechi din centrul orașului",
            "copilul deschide încet fereastra și privește păsările deasupra grădinii",
            "cartea nouă rămâne deschisă pe masa mare din camera luminoasă",
            "trenul trece repede printre dealuri verzi înainte să ajungă în gară",
            "oamenii merg grăbiți pe strada îngustă în timp ce ploaia începe",
            "profesorul explică ideea clar iar elevii urmăresc cu atenție fiecare exemplu",
            "lumina caldă a serii se întinde peste câmpurile liniștite din depărtare",
            "fata ridică privirea și observă norii albi mișcându-se foarte încet",
            "cuvintele bine grupate pot fi recunoscute mai repede decât elementele izolate",
            "memoria păstrează pentru scurt timp informația care tocmai a fost văzută",
            "cititorul antrenat încearcă să perceapă simultan mai multe cuvinte apropiate",
            "atenția rămâne fixată în centru înainte ca textul să apară pentru o clipă",
            "după dispariția textului utilizatorul reproduce exact ordinea cuvintelor observate",
            "antrenamentul regulat compară viteza expunerii cu precizia răspunsului imediat"
    };

    private final Random random = new Random();
    private final List<String> corpusWords = new ArrayList<>();

    StimulusGenerator() {
        for (String sentence : CORPUS) {
            corpusWords.addAll(Arrays.asList(sentence.split("\\s+")));
        }
    }

    String generate(int quantityIndex) {
        if (quantityIndex <= 0) {
            int index = random.nextInt(LETTERS.length());
            return String.valueOf(LETTERS.charAt(index));
        }

        int wordCount = Math.min(10, Math.max(1, quantityIndex));
        String[] sentence = CORPUS[random.nextInt(CORPUS.length)].split("\\s+");
        if (sentence.length >= wordCount) {
            int start = random.nextInt(sentence.length - wordCount + 1);
            return join(sentence, start, wordCount);
        }

        int start = random.nextInt(Math.max(1, corpusWords.size() - wordCount + 1));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) builder.append(' ');
            builder.append(corpusWords.get((start + i) % corpusWords.size()));
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private String join(String[] words, int start, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) builder.append(' ');
            builder.append(words[start + i]);
        }
        return builder.toString();
    }
}
