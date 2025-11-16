package com.example.lucenePrototype.demo;

import com.example.lucenePrototype.analysis.Analyzer;
import com.example.lucenePrototype.document.Document;
import com.example.lucenePrototype.document.Field;
import com.example.lucenePrototype.writer.IndexWriter;
import com.example.lucenePrototype.search.IndexSearcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Demo class showcasing the enhanced query capabilities of the search library.
 * Demonstrates various types of queries including fielded, non-fielded, and mixed queries.
 * Most of the utils and pretty print are build using Windsurf
 */
public class LuceneDemo {
    
    public static void main(String[] args) {
        String indexDir = "lucene-index";
        
        try {
            // Clean up any existing index
            cleanupIndex(indexDir);
            
            // Create an analyzer
            Analyzer analyzer = Analyzer.standardAnalyzer();
            
            // Index some documents
            indexDocuments(indexDir, analyzer);
            
            // Search for documents
            searchDocuments(indexDir, analyzer);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void cleanupIndex(String indexDir) throws IOException {
        Path path = Paths.get(indexDir);
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted((a, b) -> -a.compareTo(b)) // delete children first
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch (IOException e) {
                         System.err.println("Failed to delete: " + p);
                     }
                 });
        }
    }
    
    private static void indexDocuments(String indexDir, Analyzer analyzer) throws Exception {
        try (IndexWriter writer = new IndexWriter(indexDir, analyzer)) {
            // Document 1 - Java Programming (with repeated terms for TF testing)
            Document doc1 = new Document("1");
            doc1.addField(new Field("title", "Java Java Programming", true, true)); // Repeated 'Java' for TF
            doc1.addField(new Field("author", "John Smith", true, true));
            doc1.addField(new Field("content", "Learn Java programming with this comprehensive guide. Java is great!", true, true));
            writer.addDocument(doc1);
            
            // Document 2 - Python Book (common term 'programming' for DF testing)
            Document doc2 = new Document("2");
            doc2.addField(new Field("title", "Python for Beginners", true, true));
            doc2.addField(new Field("author", "Jane Doe", true, true));
            doc2.addField(new Field("content", "Introduction to Python programming language. Programming is fun!", true, true));
            writer.addDocument(doc2);
            
            // Document 3 - Advanced Java
            Document doc3 = new Document("3");
            doc3.addField(new Field("title", "Advanced Java Programming", true, true));
            doc3.addField(new Field("author", "John Smith", true, true));
            doc3.addField(new Field("content", "Advanced concepts in Java programming. Deep dive into Java.", true, true));
            writer.addDocument(doc3);
            
            // Document 4 - Search Engines (with special characters)
            Document doc4 = new Document("4");
            doc4.addField(new Field("title", "Building Search Engines (2024 Edition)", true, true));
            doc4.addField(new Field("author", "Alex Johnson & Team", true, true));
            doc4.addField(new Field("content", "Learn how to build search engines using modern techniques. C++/Python/Java.", true, true));
            writer.addDocument(doc4);
            
            // Document 5 - Stop words and case sensitivity
            Document doc5 = new Document("5");
            doc5.addField(new Field("title", "The Art of Programming", true, true));
            doc5.addField(new Field("author", "The Programming Team", true, true));
            doc5.addField(new Field("content", "This is a test of stop words and CASE sensitivity.", true, true));
            writer.addDocument(doc5);
            
            // Commit the changes
            writer.commit();
            System.out.println("Indexed 5 sample documents with enhanced test cases");
        }
    }
    
    private static void searchDocuments(String indexDir, Analyzer analyzer) throws Exception {
        try (IndexSearcher searcher = new IndexSearcher(indexDir, analyzer)) {
            // 1. Test Term Frequency (TF)
            System.out.println("\n=== 1. Term Frequency (TF) Test ===");
            System.out.println("Searching for 'java' - Document with more repetitions should score higher");
            searchAndPrint(searcher, "java", List.of("title", "content"));
            
            // 2. Test Document Frequency (DF) and IDF
            System.out.println("\n=== 2. Document Frequency (DF) and IDF Test ===");
            System.out.println("Searching for 'programming' - Common term, should have lower IDF");
            System.out.println("Searching for 'comprehensive' - Rare term, should have higher IDF");
            searchAndPrint(searcher, "programming comprehensive", List.of("content"));
            
            // 3. Phrase Search
            System.out.println("\n=== 4. Phrase Search ===");
            System.out.println("Searching for '\"java programming\"' (exact phrase)");
            searchAndPrint(searcher, "\"java programming\"", List.of("title", "content"));
            
            // 5. Special Characters
            System.out.println("\n=== 5. Special Characters ===");
            System.out.println("Searching for 'c++' and '2024' (special chars and numbers)");
            searchAndPrint(searcher, "c++ 2024", List.of("title", "content"));
            
            // 6. Stop Words and Case Sensitivity
            System.out.println("\n=== 6. Stop Words and Case Sensitivity ===");
            System.out.println("Searching for 'the art' (stop word handling)");
            searchAndPrint(searcher, "the art", List.of("title"));
            System.out.println("Searching for 'CASE' (case sensitivity)");
            searchAndPrint(searcher, "CASE", List.of("content"));
            
            // 7. Empty/Whitespace Query
            System.out.println("\n=== 7. Empty/Whitespace Query ===");
            System.out.println("Searching with empty string:");
            searchAndPrint(searcher, "", List.of("title"));
            System.out.println("Searching with whitespace:");
            searchAndPrint(searcher, "   ", List.of("title"));
            
            // 8. Non-existent term
            System.out.println("\n=== 8. Non-existent Term ===");
            System.out.println("Searching for 'nonexistentterm':");
            searchAndPrint(searcher, "nonexistentterm", List.of("title", "content"));
        }
    }
    
    private static void searchAndPrint(IndexSearcher searcher, String query, List<String> fields) throws Exception {
        System.out.println("Query: " + query);
        System.out.println("-".repeat(50));
        
        List<Map<String, String>> results = searcher.search(query, fields);
        if (results.isEmpty()) {
            System.out.println("No results found.\n");
            return;
        }
        
        System.out.println("Found " + results.size() + " result(s):");
        for (Map<String, String> docFields : results) {
            System.out.println("\nDocument ID: " + docFields.get("id"));
            docFields.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("id"))
                .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
        }
        System.out.println();
    }
}
