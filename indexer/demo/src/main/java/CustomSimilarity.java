package com.example.lucene.service;

import org.apache.lucene.search.similarities.ClassicSimilarity;

public class CustomSimilarity extends ClassicSimilarity {
    @Override
    public float tf(float freq) {
        // Customize term frequency (tf) scoring
        return super.tf(freq) * 2; // Boost the score by multiplying with 2
    }

    @Override
    public float idf(long docFreq, long docCount) {
        // Customize inverse document frequency (idf) scoring
        return super.idf(docFreq, docCount) * 1.5f; // Boost the score by multiplying with 1.5
    }
}