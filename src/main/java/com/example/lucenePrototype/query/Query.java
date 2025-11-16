package com.example.lucenePrototype.query;

import java.util.List;
import java.util.Objects;

/**
 * Represents a search query with its components.
 * Can be either a term query (single term) or a phrase query (multiple terms).
 * Can be either fielded (search in specific field) or non-fielded (search all fields).
 */
public class Query {
    private final String field; // which field to search in (null means search all fields)
    private final String term;  // single term
    private final List<String> terms; // For phrase queries

    /**
     * Creates a non-fielded query that searches across all fields.
     * @param term The search term
     */
    public Query(String term) {
        this(null, term);
    }

    /**
     * Creates a fielded query that searches in a specific field.
     * @param field The field to search in (can be null for all fields)
     * @param term The search term
     */
    public Query(String field, String term) {
        this.field = field;  // Can be null for all-fields search
        this.term = Objects.requireNonNull(term, "Term cannot be null");
        this.terms = null;
    }

    /**
     * Creates a fielded phrase query that searches in a specific field.
     * @param field The field to search in (can be null for all fields)
     * @param terms The phrase terms
     */
    public Query(String field, List<String> terms) {
        this.field = field;  // Can be null for all-fields search
        this.term = null;
        this.terms = List.copyOf(Objects.requireNonNull(terms, "Terms list cannot be null"));
        if (this.terms.isEmpty()) {
            throw new IllegalArgumentException("Terms list cannot be empty");
        }
    }

    /**
     * @return The field to search in, or null if this is an all-fields search
     */
    public String getField() { 
        return field; 
    }
    
    /**
     * @return The search term for non-phrase queries
     */
    public String getTerm() {
        return term;
    }

    /**
     * @return The search terms for phrase queries
     */
    public List<String> getTerms() { 
        return terms; 
    }
    
    /**
     * @return true if this is a phrase query (multiple terms)
     */
    public boolean isPhraseQuery() { 
        return terms != null; 
    }
    
    /**
     * @return true if this is a fielded query (has a specific field to search in)
     */
    public boolean isFielded() {
        return field != null;
    }
    
    @Override
    public String toString() {
        String queryText;
        if (isPhraseQuery()) {
            queryText = "\"" + String.join(" ", terms) + "\"";
        } else {
            queryText = term;
        }
        
        if (isFielded()) {
            return field + ":" + queryText;
        }
        return queryText;
    }
}
