package com.example.lucenePrototype.document;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {
    
    @Test
    void testDocumentCreation() {
        Document doc = new Document("1");
        assertEquals("1", doc.getId());
        assertTrue(doc.getFields().isEmpty());
    }
    
    @Test
    void testAddAndRetrieveFields() {
        Document doc = new Document("1");
        
        // Add fields
        doc.addField(new Field("title", "Test Title", true, true));
        doc.addField(new Field("content", "Test content", true, true));
        
        // Verify fields were added
        Map<String, Field> fields = doc.getFields();
        assertEquals(2, fields.size());
        assertEquals("Test Title", fields.get("title").getValue());
        assertEquals("Test content", fields.get("content").getValue());
        
        // Test updating existing field
        doc.addField(new Field("title", "Updated Title", true, true));
        assertEquals("Updated Title", doc.getFields().get("title").getValue());
    }
    
    @Test
    void testDocumentEquality() {
        Document doc1 = new Document("1");
        doc1.addField(new Field("title", "Test", true, true));
        
        Document doc2 = new Document("1");
        doc2.addField(new Field("title", "Test", true, true));
        
        Document doc3 = new Document("2");
        
        // Documents with same ID should be considered equal
        assertEquals(doc1, doc2);
        assertEquals(doc1.hashCode(), doc2.hashCode());
        
        // Different IDs should not be equal
        assertNotEquals(doc1, doc3);
    }
}
