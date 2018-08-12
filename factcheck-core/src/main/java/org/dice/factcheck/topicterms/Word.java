package org.dice.factcheck.topicterms;

public class Word {

    public boolean isFromWikipedia() {

        return isFromWikipedia;
    }

    private String word;
    private float score_CNPMI;
    private boolean isFromWikipedia; // Is that term extracted from a Wikipedia
                                     // article

    public Word(String word, float Score, boolean fromWikipedia) {

        isFromWikipedia = fromWikipedia;
        this.word = word;
        this.score_CNPMI = Score;
    }

    public Word(String word, float Score) {

        this(word, Score, true);
    }

    public String getWord() {

        return word;
    }

    public float getScore() {

        return score_CNPMI;
    }

    @Override
    public String toString() {

        return word;
    }

    public Word setScore(int i) {

        this.score_CNPMI = i;
        return this;
    }
}
