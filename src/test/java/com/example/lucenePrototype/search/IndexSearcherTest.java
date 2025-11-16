package com.example.lucenePrototype.search;

import com.example.lucenePrototype.analysis.StandardAnalyzer;
import com.example.lucenePrototype.document.Document;
import com.example.lucenePrototype.document.Field;
import com.example.lucenePrototype.writer.IndexWriter;
import com.example.lucenePrototype.reader.IndexReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IndexSearcherTest {
    
    @Mock
    private IndexReader mockReader;
    
    private IndexSearcher searcher;
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    
    @BeforeEach
    void setUp() throws IOException, ClassNotFoundException {
        MockitoAnnotations.openMocks(this);
        
        // Create a spy of IndexSearcher to inject our mock reader
        searcher = Mockito.spy(new IndexSearcher("test-index", analyzer));
        
        // Mock the getReader() method to return our mock reader
        doReturn(mockReader).when(searcher).getReader();
        
        // Mock the getStoredFields method to return a default empty map for any document not explicitly mocked
        when(mockReader.getStoredFields(anyString(), any())).thenReturn(Collections.emptyMap());
    }
    
    @AfterEach
    void tearDown() {
        try {
            searcher.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    @Test
    void testSearchWithEmptyQuery() {
        List<Map<String, String>> results = searcher.search("", List.of("title", "content"));
        assertTrue(results.isEmpty());
        
        results = searcher.search("   ", List.of("title", "content"));
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testSearchWithNoResults() throws Exception {
        // Mock the reader to return empty results
        when(mockReader.getDocumentCount()).thenReturn(10);
        when(mockReader.getFieldsForTerm(anyString())).thenReturn(Collections.emptyMap());
        
        List<Map<String, String>> results = searcher.search("nonexistentterm", List.of("title", "content"));
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testSearchWithSingleTerm() throws Exception {
        // Create a temporary directory for the index
        Path tempDir = Files.createTempDirectory("lucene-test-index");
        try {
            // Create an index with test data
            StandardAnalyzer analyzer = new StandardAnalyzer();
            
            // Create an index writer
            IndexWriter writer = new IndexWriter(tempDir.toString(), analyzer);
            
            // Add test documents
            Document doc1 = new Document("1");
            doc1.addField(new Field("id", "1", true, true));
            doc1.addField(new Field("title", "Test Document 1", true, true));
            doc1.addField(new Field("content", "This is a test document", true, true));
            writer.addDocument(doc1);
            
            Document doc2 = new Document("2");
            doc2.addField(new Field("id", "2", true, true));
            doc2.addField(new Field("title", "Another Test Document", true, true));
            writer.addDocument(doc2);
            
            // Commit and close the writer
            writer.commit();
            writer.close();
            
            // Create a searcher with the test index
            IndexSearcher searcher = new IndexSearcher(tempDir.toString(), analyzer);
            
            try {
                // Perform search - "this is a" are stop words and will be removed
                String searchQuery = "This is a test document";
                
                // Perform the search
                List<Map<String, String>> results = searcher.search(searchQuery, List.of("title", "content"));
                
                // Verify results - both documents should be returned
                assertFalse(results.isEmpty(), "Expected at least one search result");
                assertEquals(2, results.size(), "Expected exactly two search results");
                
                // Document 1 should have a higher score as it contains both "test" and "document" in title and content
                // Document 2 only contains "test" in the title
                String firstDocId = results.get(0).get("id");
                String secondDocId = results.get(1).get("id");
                
                // Verify the document with more matches is ranked higher
                if ("2".equals(firstDocId) && "1".equals(secondDocId)) {
                    // If the order is wrong, fail with a descriptive message
                    double score1 = Double.parseDouble(results.get(0).get("_score"));
                    double score2 = Double.parseDouble(results.get(1).get("_score"));
                    fail(String.format("Documents in wrong order. Doc 2 (score=%.4f) should not be ranked higher than Doc 1 (score=%.4f)", 
                        score1, score2));
                }
                
                // Verify scores were added and are in descending order
                double score1 = Double.parseDouble(results.get(0).get("_score"));
                double score2 = Double.parseDouble(results.get(1).get("_score"));
                assertTrue(score1 >= score2, "Documents should be ordered by score in descending order");
                
            } finally {
                searcher.close();
            }
        } finally {
            // Clean up the temporary directory
            Files.walk(tempDir)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }
    
    // Helper method to combine multiple field maps
    private Map<String, Set<String>> combineMaps(Map<String, Set<String>>... maps) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map<String, Set<String>> map : maps) {
            map.forEach((key, value) -> {
                result.computeIfAbsent(key, k -> new HashSet<>()).addAll(value);
            });
        }
        return result;
    }
}
