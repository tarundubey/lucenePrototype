package com.example.lucenePrototype.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This analyzer performs the following steps as mentioned in the Lucene doc:
 * converts the input to lowercase
 * splits on whitespace
 * removes common English stop words
 * Note: this is a simple example and may not be suitable for all use cases
 */
public class StandardAnalyzer extends Analyzer {
//    common English stop words suggested by LLM
    private static final String[] STOP_WORDS = {
        "a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
        "if", "in", "into", "is", "it", "no", "not", "of", "on", "or",
        "such", "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with"
    };

    @Override
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerText = text.toLowerCase(Locale.ROOT);
        
        // Split on non-word characters like punctuation etc
        String[] words = lowerText.split("\\W+");
        
        // Remove stop words and return
        List<String> tokens = new ArrayList<>();
        for (String word : words) {
            if (!isStopWord(word) && !word.trim().isEmpty()) {
                tokens.add(word);
            }
        }
        
        return tokens;
    }
    
    private boolean isStopWord(String word) {
        for (String stopWord : STOP_WORDS) {
            if (stopWord.equals(word)) {
                return true;
            }
        }
        return false;
    }
}
