package com.example.lucenePrototype.writer;

import com.example.lucenePrototype.analysis.Analyzer;
import com.example.lucenePrototype.document.Document;
import com.example.lucenePrototype.document.Field;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Index writer creates an inverted index and stores it in a file.
 * Thread-safe as it uses ConcurrentHashMaps - not tested though
 * Note that here the "Directory" counterpart mentioned in document is local folder
 * You can upgrade and build a distributed index by having multiple machines and IndexWriter write to different directories
 * This doesn't do stuff such as compaction etc yet, maybe in the future
 * It still holds the documents in memory and commit to directory when commit() is called.
 * Focus on understanding the inverted index data structure and the concept will easily make sense
 * We have inherited Closeable interface to close the writer and release resources when we are done
 */
public class IndexWriter implements Closeable {
    private final Path indexDir; // Directory where the index is stored
    private final Analyzer analyzer; // Analyzer used to tokenize text
    /*
    Structure of the inverted index:
    Map<
        String,                     // Term (e.g., "search")
        Map<
            String,                 // Field name (e.g., "title")
            Set<String>             // Document IDs containing this term in this field
        >
    >
    The real reason why it's called "inverted" is because it's a map where the keys are terms and the
    values are sets of document IDs.
     */
    private final Map<String, Map<String, Set<String>>> invertedIndex = new ConcurrentHashMap<>();

    /*
    Map<
    String,                     // Document ID
    Map<
        String,                 // Field name
        String                  // Field value
        >
    >
     */
    private final Map<String, Map<String, String>> storedFields = new ConcurrentHashMap<>();

    // Original documents (for impl simplicity, in a real system you'd use storedFields)
    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    public IndexWriter(String indexDirectory, Analyzer analyzer) throws IOException {
        this.indexDir = Paths.get(indexDirectory);
        this.analyzer = analyzer;

        // Create the index for the first time
        if (!Files.exists(indexDir)) {
            Files.createDirectories(indexDir);
        }
    }

    /**
     * Adds a document to the invenrted index, processing each field.
     */
    public void addDocument(Document document) {
        String docId = document.getId();
        documents.put(docId, document);

        // Process each field
        for (Field field : document.getFields().values()) {
            String fieldName = field.getName();

            // Store the field if user input isStored is true
            // We have used computeIfAbsent here and across this code for readability and efficiency
            if (field.isStored()) {
                storedFields
                    .computeIfAbsent(docId, k -> new ConcurrentHashMap<>())
                    .put(fieldName, field.getValue());
            }

            // Index the field if isIndexed is asked by the user
            if (field.isIndexed()) {
                indexField(docId, field);
            }
        }
    }

    private void indexField(String docId, Field field) {
        // Tokenize the field
        List<String> terms = analyzer.tokenize(field.getValue());

        // Now for each term, add it to the inverted index

        for (String term : terms) {
            // If fieldName exists in the inner map, returns the existing Set<String>
            //If not, creates a new HashSet for that field
            // Finally, adds the docId to the Set
            invertedIndex
                .computeIfAbsent(term, k -> new HashMap<>())
                .computeIfAbsent(field.getName(), k -> new HashSet<>())
                .add(docId);
        }
    }

    /**
     * Commits all pending changes to disk
     * Consider that until commit() is called, the index is in-memory
     * Commit() writes the inverted index and stored fields to disk which is the flush equivalent
     * We are not doing compaction here for simplicity - can do it later maybe
     * We are not doing compaction here for simplicity - can do it later maybe
     * Note that we are doing serialization here to store the data structures- you can do something like bin format or
     *  similar algorithms
     *  Also this function is not thread-safe in our implementation - you can make it thread-safe if needed but we are
     *  assuming this is called from a single thread after all documents are added
     */
    public void commit() throws IOException {
        // Write the inverted index
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(indexDir.resolve("inverted_index.ser").toFile()))) {
            oos.writeObject(invertedIndex);
        }

        // Write the stored fields
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(indexDir.resolve("stored_fields.ser").toFile()))) {
            oos.writeObject(storedFields);
        }

        // Write the documents (for backward compatibility)
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(indexDir.resolve("documents.ser").toFile()))) {
            oos.writeObject(documents);
        }
    }

    // Call commit automatically on close - something like auto-commit after adding all documents
    @Override
    public void close() throws IOException {
        commit();
    }
}
