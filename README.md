> **Before we dive in**, check out my previous article on the [Theoretical Explanation of Lucene Concepts](https://medium.com/@tarunkumardubey6793/lucene-explained-simply-the-core-technology-behind-modern-search-engines-a9a7f49b57ee) for a solid foundation on the core concepts we'll be implementing.

# Lucene-like Search Library

A lightweight, Java-based search library inspired by Apache Lucene, providing core search functionality with a simple API.

## Features

- **Document Indexing**: Index documents with multiple fields
- **Full-Text Search**: Powerful text search capabilities
- **Field-Specific Search**: Search within specific document fields
- **TF-IDF Scoring**: Term Frequency-Inverse Document Frequency scoring for relevant results
- **Stop Word Filtering**: Built-in stop word removal for common English words
- **REST API**: Ready-to-use HTTP endpoints for integration
- **Extensible Architecture**: Easy to extend with custom analyzers and scorers

## Prerequisites

- Java 11 or higher
- Maven or Gradle (for building from source)
- Spring Boot (for the REST API)

## Quick Start

### 1. Add Dependency

Add the following to your `build.gradle`:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    // Other dependencies...
}
```

### 2. Basic Usage

```java
// Create an analyzer
Analyzer analyzer = new StandardAnalyzer();

// Create an index writer
IndexWriter writer = new IndexWriter("path/to/index", analyzer);

// Create a document
Document doc = new Document("doc1");
doc.addField(new Field("title", "Sample Document", true, true));
doc.addField(new Field("content", "This is a sample document for the search index.", true, true));

// Add document to index
writer.addDocument(doc);
writer.commit();

// Search the index
IndexSearcher searcher = new IndexSearcher("path/to/index", analyzer);
List<Map<String, String>> results = searcher.search("sample document", List.of("title", "content"));
```

## Core Components

### Document

A `Document` represents a searchable item with multiple fields. Each document must have a unique ID.

### Field

A `Field` represents a single piece of data within a document. Fields can be:
- **Indexed**: Included in the search index
- **Stored**: Stored for retrieval with search results

### Analyzer

Processes text into searchable terms. The library includes a `StandardAnalyzer` that handles:
- Lowercasing
- Stop word removal
- Simple tokenization

### IndexWriter

Handles creating and updating the search index. Thread-safe for concurrent access.

### IndexSearcher

Searches the index and returns matching documents with relevance scores.

## REST API

The library includes a REST API for easy integration:

- `POST /api/search` - Search for documents
- `POST /api/documents` - Add a document to the index
- `GET /api/documents/{id}` - Retrieve a document by ID
- `GET /api/stats` - Get index statistics

### Example API Request

```http
POST /api/search
Content-Type: application/json

{
  "query": "search terms",
  "fields": ["title", "content"]
}
```

## Extending the Library

### Custom Analyzers

Create a custom analyzer by extending the `Analyzer` class:

```java
public class CustomAnalyzer extends Analyzer {
    @Override
    public List<String> tokenize(String text) {
        // Custom tokenization logic
    }
}
```

### Custom Scorers

Implement a custom scoring algorithm by extending the `Scorer` class.

## Performance Considerations

- For large indexes, consider implementing batch operations
- The index is stored in memory and persisted to disk on commit
- Indexing is not real-time; call `commit()` to persist changes

## Testing

Run the test suite with:

```bash
./gradlew test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by Apache Lucene
- Built with Java and Spring Boot
- Documentation at https://docs.google.com/document/d/1GHbu8769tI_1jr0QqW9swu_7bQ_76XeMp8aeFDeFGQc/
