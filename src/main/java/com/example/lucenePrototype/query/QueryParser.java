package com.example.lucenePrototype.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses search queries to Query objects.
 * Supports fielded searches (like "title:lucene") and phrase searches (like "title:\"apache lucene\"").
 */
public class QueryParser {
    
    // Pattern for fielded queries: field:term or field:"phrase"
    /*
    ([a-zA-Z0-9_]+)   :   ( \" ([^\"]+) \"  |  (\S+) )
       field name         quote  value  quote   or   term
     */
    private static final Pattern FIELDED_QUERY_PATTERN = 
        Pattern.compile("([a-zA-Z0-9_]+):(\"([^\"]+)\"|(\\S+))");
    
    /**
     * Parses a search query string into a list of Query objects.
     * Supports both fielded and non-fielded queries:
     * Fielded: {@code field:value} or {@code field:"phrase with spaces"}</li>
     * Non-fielded: {@code value} (searches all fields)
     * @param query The query string to parse
     * @return A list of Query objects representing the parsed query
     * @throws IllegalArgumentException if the query is null, empty, or contains invalid syntax
     */
    public static List<Query> parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        List<Query> queries = new ArrayList<>();
        // create a matcher for the fielded query pattern for pattern matching
        Matcher matcher = FIELDED_QUERY_PATTERN.matcher(query);
        int lastEnd = 0;
        
        // Find all fielded queries (field:value or field:"phrase"), anywhere in the query
        while (matcher.find()) {
            // If there's text before this match, it's a non-fielded query
            if (matcher.start() > lastEnd) {
                String nonFieldedText = query.substring(lastEnd, matcher.start()).trim();
                if (!nonFieldedText.isEmpty()) {
                    queries.addAll(parseNonFieldedQuery(nonFieldedText));
                }
            }
            
            // Process the fielded query
            String field = matcher.group(1);
            // Capture the quoted phrase or single term
            String phrase = matcher.group(3);
            // Capture the single term
            String term = matcher.group(4);
            
            if (phrase != null) {
                // Handle phrase queries with tokenization
                queries.add(new Query(field, tokenizePhrase(phrase)));
            } else if (term != null) {
                // No tokenization needed
                queries.add(new Query(field, term));
            }
            
            lastEnd = matcher.end();
        }
        
        // Handle any remaining non-fielded text after the last fielded query
        if (lastEnd < query.length()) {
            String remainingText = query.substring(lastEnd).trim();
            if (!remainingText.isEmpty()) {
                queries.addAll(parseNonFieldedQuery(remainingText));
            }
        }
        
        // If no queries were created (no fielded queries and no non-fielded text)
        if (queries.isEmpty()) {
            throw new IllegalArgumentException("No valid query terms found");
        }
        
        return queries;
    }
    
    /**
     * Parses a non-fielded query string into Query objects.
     * Handles both single terms and phrases in the input.
     * 
     * @param query The non-fielded query string to parse
     * @return List of Query objects
     */
    private static List<Query> parseNonFieldedQuery(String query) {
        List<Query> queries = new ArrayList<>();
        
        // Simple approach: split on whitespace and create a query for each term
        String[] terms = query.trim().split("\\s+");
        for (String term : terms) {
            if (!term.trim().isEmpty()) {
                queries.add(new Query(term.trim()));
            }
        }
        
        return queries;
    }
    
    /**
     * Tokenizes a phrase into individual terms using whitespace as a delimiter.
     * Note that this is simple tokenization and different from analyzers in Lucene.
     * 
     * @param phrase The phrase to tokenize
     * @return A list of terms in the phrase
     */
    private static List<String> tokenizePhrase(String phrase) {
        List<String> terms = new ArrayList<>();
        if (phrase != null && !phrase.trim().isEmpty()) {
            String[] tokens = phrase.trim().split("\\s+");
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    terms.add(token.trim());
                }
            }
        }
        return terms;
    }
    
    /**
     * Determines if the input string contains any fielded search terms.
     * A fielded query is one that specifies a field name followed by a colon and a value,
     * for example: "title:java" or "author:\"john smith\"".
     * @param query The query string to check. Can be null or empty.
     */
    public static boolean isFieldedQuery(String query) {
        return query != null && FIELDED_QUERY_PATTERN.matcher(query).find();
    }
}
