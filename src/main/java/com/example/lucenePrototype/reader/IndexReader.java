package com.example.lucenePrototype.reader;

import com.example.lucenePrototype.document.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Reads and provides access to the search index.
 * Handles loading and accessing the inverted index and stored fields along with documents.
 * Operations are thread safe
 */
public class IndexReader implements AutoCloseable {
    private final Path indexDir;
    private Map<String, Map<String, Set<String>>> invertedIndex;
    private Map<String, Map<String, String>> storedFields;
    private Map<String, Document> documents;

    /**
     * Creates a new IndexReader for the specified index directory.
     *
     * @param indexDirectory The directory containing the index files
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a class cannot be found during deserialization , a fail-safe
     */
    public IndexReader(String indexDirectory) throws IOException, ClassNotFoundException {
        this.indexDir = Paths.get(indexDirectory);
        this.invertedIndex = new HashMap<>();
        this.storedFields = new HashMap<>();
        this.documents = new HashMap<>();
        loadIndex();
    }

    /**
     * Loads the index from disk.
     * This index is the one that is committed to disk by the IndexWriter.
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a class cannot be found during deserialization
     */
    @SuppressWarnings("unchecked")
    private void loadIndex() throws IOException, ClassNotFoundException {
        loadFromFile(indexDir.resolve("inverted_index.ser"), invertedIndex);
        
        loadFromFile(indexDir.resolve("stored_fields.ser"), storedFields);
        
        loadFromFile(indexDir.resolve("documents.ser"), documents);
    }

    /**
     * Loads data from a file into the specified map.
     * Using generic type to allow loading different types of data.
     * @param file The file to load from
     * @param target The target map to load data into
     * @param <T> The type of the map values
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a class cannot be found during deserialization
     */
    @SuppressWarnings("unchecked")
    private <T> void loadFromFile(Path file, Map<String, T> target) throws IOException, ClassNotFoundException {
        if (Files.exists(file)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
                Map<String, T> loadedMap = (Map<String, T>) ois.readObject();
                target.putAll(loadedMap);
            } catch (FileNotFoundException e) {
                System.out.println("File not found (possibly deleted): " + file);
            }
        }
    }

    /**
     * Retrieves stored fields for a document.
     * Refer Index Writer for more details
     * @param docId The ID of the document
     * @param fieldsToLoad List of fields to include (null for all stored fields)
     * @return Map of field names to values, or null if document not found
     */
    public Map<String, String> getStoredFields(String docId, List<String> fieldsToLoad) {
        Map<String, String> docFields = storedFields.get(docId);
        if (docFields == null) {
            return null;
        }
        
        if (fieldsToLoad == null || fieldsToLoad.isEmpty()) {
            return new HashMap<>(docFields);
        }
        
        // Only include requested fields
        Map<String, String> result = new HashMap<>();
        for (String field : fieldsToLoad) {
            if (docFields.containsKey(field)) {
                result.put(field, docFields.get(field));
            }
        }
        return result;
    }

    /**
     * Gets the document with the specified ID.
     *
     * @param docId The ID of the document to retrieve
     * @return The document, or null if not found
     */
    public Document getDocument(String docId) {
        return documents.get(docId);
    }

    /**
     * Gets the set of document IDs that contain the specified term in the specified field.
     * Currently not used but can be implemented if needed in SearchService
     * @param term The term to search for
     * @param field The field to search in
     * @return Set of document IDs containing the term in the field, or empty set if none found
     */
    public Set<String> getDocumentsForTerm(String term, String field) {
        return Optional.ofNullable(invertedIndex.get(term))
                .map(fieldMap -> fieldMap.get(field))
                .orElse(Collections.emptySet());
    }

    /**
     * Gets all terms in the index.
     *
     * @return Set of all terms in the index
     */
    public Set<String> getAllTerms() {
        return invertedIndex.keySet();
    }
    
    /**
     * Gets the total number of documents in the index.
     *
     * @return The number of documents in the index
     */
    public int getDocumentCount() {
        return documents.size();
    }

    /**
     * Gets all fields for a specific term.
     *
     * @param term The term to get fields for
     * @return Map of field names to document IDs, or empty map if term not found
     */
    public Map<String, Set<String>> getFieldsForTerm(String term) {
        return invertedIndex.getOrDefault(term, Collections.emptyMap());
    }

    @Override
    public void close() {
        // Clear resources
        invertedIndex.clear();
        storedFields.clear();
        documents.clear();
    }
}
