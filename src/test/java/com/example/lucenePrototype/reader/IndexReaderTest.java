package com.example.lucenePrototype.reader;

import com.example.lucenePrototype.document.Document;
import com.example.lucenePrototype.document.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IndexReaderTest {
    
    @TempDir
    Path tempDir;
    
    private IndexReader reader;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a test index
        createTestIndex();
        reader = new IndexReader(tempDir.toString());
    }
    
    @AfterEach
    void tearDown() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void createTestIndex() throws Exception {
        // Create a simple document
        Document doc1 = new Document("1");
        doc1.addField(new Field("title", "Test Document 1", true, true));
        doc1.addField(new Field("content", "This is a test document", true, true));
        
        // Create a writer to write the test index with StandardAnalyzer
        com.example.lucenePrototype.writer.IndexWriter writer = 
            new com.example.lucenePrototype.writer.IndexWriter(
                tempDir.toString(),
                new com.example.lucenePrototype.analysis.StandardAnalyzer()
            );
        
        try {
            writer.addDocument(doc1);
            writer.commit();
        } finally {
            writer.close();
        }
    }
    
    @Test
    void testGetDocument() {
        Document doc = reader.getDocument("1");
        assertNotNull(doc);
        assertEquals("1", doc.getId());
        assertEquals("Test Document 1", doc.getFields().get("title").getValue());
        assertEquals("This is a test document", doc.getFields().get("content").getValue());
    }
    
    @Test
    void testGetNonExistentDocument() {
        Document doc = reader.getDocument("nonexistent");
        assertNull(doc);
    }
    
    @Test
    void testGetStoredFields() {
        // Test getting all fields
        Map<String, String> fields = reader.getStoredFields("1", null);
        assertNotNull(fields);
        assertEquals("Test Document 1", fields.get("title"));
        assertEquals("This is a test document", fields.get("content"));
        
        // Test getting specific fields
        fields = reader.getStoredFields("1", List.of("title"));
        assertEquals(1, fields.size());
        assertEquals("Test Document 1", fields.get("title"));
        assertNull(fields.get("content"));
    }
    
    @Test
    void testGetDocumentCount() {
        int count = reader.getDocumentCount();
        assertEquals(1, count);
    }
    
    @Test
    void testGetAllTerms() {
        Set<String> terms = reader.getAllTerms();
        assertFalse(terms.isEmpty());
        // Should contain terms from both title and content
        assertTrue(terms.contains("test") || terms.contains("document") || 
                  terms.contains("this") || terms.contains("is") || 
                  terms.contains("a"));
    }
}
