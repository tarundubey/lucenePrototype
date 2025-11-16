package com.example.lucenePrototype.analysis;

import java.util.List;

/**
 * An abstract base class for text analysis.
 * Subclasses must implement the tokenize method to provide specific
 * text analysis functionality.
 * This follows factory design pattern and provides a way to create different types of analyzers
 */
public abstract class Analyzer {
    
    /**
     * Tokenizes the input text into a list of terms.
     * 
     * @param text The text to tokenize
     * @return A list of terms extracted from the text
     */
    public abstract List<String> tokenize(String text);
    
    /**
     * Creates a new instance of the standard analyzer.
     * 
     * @return A new StandardAnalyzer instance
     */
    public static Analyzer standardAnalyzer() {
        return new StandardAnalyzer();
    }
}
