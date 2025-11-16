package com.example.lucenePrototype.service;

import com.example.lucenePrototype.analysis.Analyzer;
import com.example.lucenePrototype.analysis.StandardAnalyzer;
import com.example.lucenePrototype.api.dto.DocumentDto;
import com.example.lucenePrototype.api.dto.FieldDto;
import com.example.lucenePrototype.document.Document;
import com.example.lucenePrototype.document.Field;
import com.example.lucenePrototype.writer.IndexWriter;
import com.example.lucenePrototype.query.Query;
import com.example.lucenePrototype.query.QueryParser;
import com.example.lucenePrototype.search.IndexSearcher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/*
Has set of business logic for search - currently exposed by API in search controller
Note that this is not part of the core search library and is specific to this application for exposing APIs
 */
@Service
public class SearchService {

    @Value("${search.index.directory:lucene-index}")
    private String indexDirectory;
    
    private final Analyzer analyzer = new StandardAnalyzer();
    private IndexWriter writer;
    private IndexSearcher searcher;
    
    @PostConstruct
    public void init() throws Exception {
        // Ensure the index directory exists
        Path path = Paths.get(indexDirectory);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        
        // Initialize writer and searcher
        this.writer = new IndexWriter(indexDirectory, analyzer);
        this.searcher = new IndexSearcher(indexDirectory, analyzer);
        
        // Ensure the index is properly initialized
        writer.commit();
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (searcher != null) {
                searcher.close();
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Error cleaning up search service: " + e.getMessage());
            // print stack trace - can remove in production maybe
            e.printStackTrace();
        }
    }
    
    public void indexDocument(DocumentDto docDto) throws Exception {
        Document doc = new Document(docDto.getId());
        
        for (var fieldDto : docDto.getFields()) {
            Field field = new Field(
                fieldDto.getName(),
                fieldDto.getValue(),
                fieldDto.isStored(),
                fieldDto.isIndexed()
            );
            doc.addField(field);
        }
        
        writer.addDocument(doc);
        writer.commit();
    }
    
    /**
     * Searches for documents matching the query string.
     * The query can contain:
     * - Fielded searches: "title:java"
     * - Phrase searches: "author:\"john smith\""
     * - Mixed queries: "java title:programming"
     * - Non-fielded terms (searches all fields): "java programming"
     *
     * @param queryString The search query string
     * @param fields List of fields to search
     * @return List of matching documents
     * @throws IllegalArgumentException if the query is invalid
     */
    public List<DocumentDto> search(String queryString, List<String> fields) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return List.of();
        }
        
        // Validate query format before processing
        if (QueryParser.isFieldedQuery(queryString) && (fields != null && !fields.isEmpty())) {
            System.out.println("Fielded query detected with explicit fields parameter. Field specifications in query will take precedence.");
        }
        
        try {
            // Parse the query string into structured Query objects
            List<Query> queries = QueryParser.parse(queryString);

            
            // For each query, build a search string that the searcher can understand
            // In a more advanced implementation, we would use the field information
            // to search specific fields in the index
            String searchString = queries.stream()
                .flatMap(query -> {
                    if (query.isPhraseQuery()) {
                        return Stream.of('"' + String.join(" ", query.getTerms()) + '"');
                    } else {
                        return Stream.of(query.getTerm());
                    }
                })
                .collect(Collectors.joining(" "));
                
            return executeSearch(searchString);
        } catch (Exception e) {
            throw new RuntimeException("Error searching documents: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the search against the index.
     * This is a helper method to avoid code duplication.
     * 
     * @param query The query string to search for
     * @return List of matching documents as DTOs
     */
    private List<DocumentDto> executeSearch(String query) throws Exception {
        List<Map<String, String>> results = searcher.search(query, null);
        return results.stream()
                .map(this::mapToDocumentDto)
                .collect(Collectors.toList());
    }
    
    private DocumentDto mapToDocumentDto(Map<String, String> docFields) {
        DocumentDto dto = new DocumentDto();
        dto.setId(docFields.get("id"));
        
        List<FieldDto> fieldDtos = docFields.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("id")) // Exclude id as it's already set
                .map(entry -> new FieldDto(entry.getKey(), entry.getValue(), true, true))
                .collect(Collectors.toList());
        
        dto.setFields(fieldDtos);
        return dto;
    }
    
    public void deleteAll() throws Exception {
        writer = new IndexWriter(indexDirectory, analyzer); // Recreate writer to clear index
        writer.commit();
    }
    
    /**
     * Gets document statistics including total terms and document count.
     * @return Map containing statistics
     */
    public Map<String, Object> getIndexStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTerms", searcher.getReader().getAllTerms().size());
            // Get document count from stored fields since we don't have a direct method
            stats.put("documentsIndexed", searcher.getReader().getStoredFields(null, null).size());
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get index statistics", e);
        }
    }
    
    /**
     * Gets a document by its ID.
     * @param docId The document ID to retrieve
     * @return The document DTO, or null if not found
     */
    public DocumentDto getDocumentById(String docId) {
        try {
            Document doc = searcher.getReader().getDocument(docId);
            if (doc == null) {
                return null;
            }
            
            DocumentDto dto = new DocumentDto();
            dto.setId(docId);
            
            List<FieldDto> fields = doc.getFields().values().stream()
                .map(field -> new FieldDto(
                    field.getName(),
                    field.getValue().toString(),
                    field.isStored(),
                    field.isIndexed()
                ))
                .collect(Collectors.toList());
                
            dto.setFields(fields);
            return dto;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve document: " + docId, e);
        }
    }
}
