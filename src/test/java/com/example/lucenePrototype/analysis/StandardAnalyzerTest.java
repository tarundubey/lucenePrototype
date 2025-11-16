package com.example.lucenePrototype.analysis;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StandardAnalyzerTest {
    
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    
    @Test
    void testTokenizeSimpleText() {
        String text = "This is a test";
        List<String> tokens = analyzer.tokenize(text);
        
        // "a", "is", and "this" are stop words and should be removed
        assertEquals(1, tokens.size());
        assertTrue(tokens.contains("test"), "Should contain 'test'");
    }
    
    @Test
    void testTokenizeWithPunctuation() {
        String text = "Hello, world! This is a test.";
        List<String> tokens = analyzer.tokenize(text);
        
        // "a" and "is" are stop words and should be removed
        // Punctuation should be removed
        assertEquals(3, tokens.size());
        assertTrue(tokens.contains("hello"), "Should contain 'hello'");
        assertTrue(tokens.contains("world"), "Should contain 'world'");
        assertTrue(tokens.contains("test"), "Should contain 'test'");
    }
    
    @Test
    void testTokenizeEmptyString() {
        List<String> tokens = analyzer.tokenize("");
        assertTrue(tokens.isEmpty());
        
        tokens = analyzer.tokenize("   ");
        assertTrue(tokens.isEmpty());
    }
    
    @Test
    void testTokenizeSpecialCharacters() {
        String text = "C++ & Java 2023 - Let's test #hashtags and @mentions!";
        List<String> tokens = analyzer.tokenize(text);
        
        assertTrue(tokens.contains("c"));
        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("2023"));
        assertTrue(tokens.contains("let"));
        assertTrue(tokens.contains("test"));
        assertTrue(tokens.contains("hashtags"));
        assertTrue(tokens.contains("mentions"));
    }
    
    @Test
    void testTokenizeStopWords() {
        // Only "the" is in our stop words list
        String text = "the quick brown fox jumps over the lazy dog";
        List<String> tokens = analyzer.tokenize(text);
        
        // Only "the" should be removed from the original 8 words
        assertEquals(7, tokens.size());
        assertTrue(tokens.contains("quick"), "Should contain 'quick'");
        assertTrue(tokens.contains("brown"), "Should contain 'brown'");
        assertTrue(tokens.contains("fox"), "Should contain 'fox'");
        assertTrue(tokens.contains("jumps"), "Should contain 'jumps'");
        assertTrue(tokens.contains("over"), "Should contain 'over'");
        assertTrue(tokens.contains("lazy"), "Should contain 'lazy'");
        assertTrue(tokens.contains("dog"), "Should contain 'dog'");
    }
}
