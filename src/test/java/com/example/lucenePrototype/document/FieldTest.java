package com.example.lucenePrototype.document;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FieldTest {
    
    @Test
    void testFieldCreation() {
        // Test field with all properties
        Field field = new Field("title", "Test Title", true, true);
        
        assertEquals("title", field.getName());
        assertEquals("Test Title", field.getValue());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        
        // Test field with only stored = true
        Field storedOnly = new Field("content", "Test Content", true, false);
        assertTrue(storedOnly.isStored());
        assertFalse(storedOnly.isIndexed());
        
        // Test field with only indexed = true
        Field indexedOnly = new Field("author", "Test Author", false, true);
        assertFalse(indexedOnly.isStored());
        assertTrue(indexedOnly.isIndexed());
    }
    
    @Test
    void testFieldEquality() {
        Field field1 = new Field("title", "Test", true, true);
        Field field2 = new Field("title", "Test", true, true);
        Field field3 = new Field("title", "Different", true, true);
        
        // Test equals and hashCode
        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
        assertNotEquals(field1, field3);
    }
}
