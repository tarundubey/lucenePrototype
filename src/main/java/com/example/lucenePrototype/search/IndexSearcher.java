package com.example.lucenePrototype.search;

import com.example.lucenePrototype.analysis.Analyzer;
import com.example.lucenePrototype.reader.IndexReader;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Searches an index for documents matching queries.
 * Uses IndexReader for all index read operations.
*  Simple TF: Currently using 1 as TF (can be improved by storing actual term frequencies during indexing)
*  No Field Boosting: All fields are treated equally
*  No Length Normalization: Doesn't account for document length
 *
 */
public class IndexSearcher implements Closeable {
    private final IndexReader reader;
    private final Analyzer analyzer;

    /**
     * Creates a new IndexSearcher for the specified index directory.
     *
     * @param indexDirectory The directory containing the index files
     * @param analyzer The analyzer to use for tokenizing queries
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a class cannot be found during deserialization
     */
    public IndexSearcher(String indexDirectory, Analyzer analyzer) throws IOException, ClassNotFoundException {
        this.reader = new IndexReader(indexDirectory);
        this.analyzer = analyzer;
    }

    /**
     * Searches for documents containing all terms in the query across all fields.
     * Searches for documents containing all terms in the query across all fields.
     * Uses TF-IDF scoring to rank results by relevance.
     * Refer docs for details on TF-IDF
     * @param query The search query
     * @param fieldsToLoad list of fields to include
     * @return List of document ids and their stored fields
     */
    public List<Map<String, String>> search(String query, List<String> fieldsToLoad) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Tokenize the query
        List<String> terms = analyzer.tokenize(query);
        
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        // Get total number of documents for IDF calculation
        // Using a small epsilon to avoid division by zero and log(0)
        int totalDocs = Math.max(1, reader.getDocumentCount());
        
        // Map to store document scores
        Map<String, Double> docScores = new HashMap<>();
        
        // For each term in the query
        for (String term : terms) {
            // Get fields and documents containing this term
            Map<String, Set<String>> fields = reader.getFieldsForTerm(term);
            if (fields.isEmpty()) continue;
            
            // Calculate document frequency (DF) for this term
            int docFreq = fields.values().stream().mapToInt(Set::size).sum();
            
            // Calculate IDF (Inverse Document Frequency)
            // Using log10 to avoid extremely large values
            // Adding 1 to docFreq to avoid division by zero
            double idf = Math.log10((double) totalDocs / (docFreq + 1)) + 1;
            
            // For each document containing this term
            for (Set<String> docIds : fields.values()) {
                for (String docId : docIds) {
                    // Simple TF: Count of this term in this document
                    // In a real implementation, we'd get this from the index
                    // For now, we'll use 1 as a placeholder
                    double tf = 1.0;
                    
                    // Calculate score and add to document's total score
                    // Ensure we don't get -Infinity in scores
                    double score = (tf > 0 && Double.isFinite(idf)) ? tf * idf : 0.0;
                    docScores.merge(docId, score, Double::sum);
                }
            }
        }
        
        // Sort documents by score (highest first) and convert to result format
        return docScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, String> doc = getStoredFields(entry.getKey(), fieldsToLoad);
                    if (doc != null) {
                        // Ensure document ID is included in the result
                        doc.put("id", entry.getKey());
                        // Add the score to the result
                        doc.put("_score", String.format("%.4f", entry.getValue()));
                    }
                    return doc;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves stored fields for a document
     * @param docId Document ID
     * @param fieldsToLoad List of fields to include (null for all stored fields)
     * @return Map of field names to values, or null if document not found
     */
    public Map<String, String> getStoredFields(String docId, List<String> fieldsToLoad) {
        return reader.getStoredFields(docId, fieldsToLoad);
    }

    /**
     * Gets the underlying IndexReader instance.
     * @return The IndexReader used by this searcher
     */
    public IndexReader getReader() {
        return reader;
    }

    @Override
    public void close() throws IOException {
        // Close the reader
        if (reader != null) {
            reader.close();
        }
    }
}
