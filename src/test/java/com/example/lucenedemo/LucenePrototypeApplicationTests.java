package com.example.lucenedemo;

import com.example.lucenePrototype.analysis.Analyzer;
import com.example.lucenePrototype.analysis.StandardAnalyzer;
import com.example.lucenePrototype.document.Document;
import com.example.lucenePrototype.document.Field;
import com.example.lucenePrototype.writer.IndexWriter;
import com.example.lucenePrototype.search.IndexSearcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LucenePrototypeApplicationTests {

    @TempDir
    Path tempDir;
    private IndexWriter writer;
    private IndexSearcher searcher;
    private Analyzer analyzer;

    private String indexDir;
    
    @BeforeEach
    void setUp() throws Exception {
        try {
            // Get the test directory path and initialize analyzer
            indexDir = tempDir.toAbsolutePath().toString();
            analyzer = new StandardAnalyzer();
            
            // Create the index directory if it doesn't exist
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Test setup failed: " + e.getMessage(), e);
        }
    }

    @Test
    void testBasicSearch() throws Exception {
        // Create and populate the index
        try (IndexWriter writer = new IndexWriter(indexDir, analyzer)) {
            // Create and index a test document
            String docId = "doc1";
            Document doc = new Document(docId);
            doc.addField(new Field("id", docId, true, true));
            doc.addField(new Field("title", "Test Document", true, true));
            doc.addField(new Field("content", "This is a test document for Lucene demo", true, true));
            
            writer.addDocument(doc);
            writer.commit();
        }
        
        // Search the index
        try (IndexSearcher searcher = new IndexSearcher(indexDir, analyzer)) {
            List<Map<String, String>> results = searcher.search("test", List.of("title", "content"));
            
            // Verify results
            assertFalse(results.isEmpty(), "Search should return at least one result");
            
            Map<String, String> firstResult = results.get(0);
            assertNotNull(firstResult, "First result should not be null");
            
            String title = firstResult.get("title");
            assertNotNull(title, "Title should not be null");
            assertEquals("Test Document", title, "Title should match");
        }
    }
}
