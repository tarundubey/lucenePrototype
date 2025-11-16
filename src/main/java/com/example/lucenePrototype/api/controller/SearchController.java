package com.example.lucenePrototype.api.controller;

import com.example.lucenePrototype.api.dto.ApiResponse;
import com.example.lucenePrototype.api.dto.DocumentDto;
import com.example.lucenePrototype.api.dto.SearchRequest;
import com.example.lucenePrototype.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles search-related operations.
 * Also has analytics endpoints to get current index status
 * Maybe we can add swagger documentation but not present at the moment
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Indexes a new document in the search index.
     *
     * @param documentDto The document to be indexed
     * @return ResponseEntity with success/error message
     */
    @PostMapping("/index")
    public ResponseEntity<ApiResponse<Void>> indexDocument(@RequestBody DocumentDto documentDto) {
        try {
            searchService.indexDocument(documentDto);
            return ResponseEntity.ok(ApiResponse.success("Document indexed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to index document: " + e.getMessage()));
        }
    }

    /**
     * Searches for documents using the provided search request.
     *
     * @param request The search request containing the query
     * @return ResponseEntity containing search results
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<DocumentDto>>> search(@RequestBody SearchRequest request) {
        try {
            // Pass null as fields to search in all fields
            List<DocumentDto> results = searchService.search(request.getQuery(), null);
            return ResponseEntity.ok(ApiResponse.success("Search completed", results));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Searches for documents using query parameters.
     *
     * @param q The search query string
     * @return ResponseEntity containing search results
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentDto>>> searchByQueryParam(
            @RequestParam String q,
            @RequestParam(required = false) List<String> fields) {
        try {
            List<DocumentDto> results = searchService.search(q, fields);
            return ResponseEntity.ok(ApiResponse.success("Search completed", results));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Clears all documents from the search index.
     *
     * @return ResponseEntity with operation status
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearIndex() {
        try {
            searchService.deleteAll();
            return ResponseEntity.ok(ApiResponse.success("Index cleared successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to clear index: " + e.getMessage()));
        }
    }

    /**
     * Gets statistics about the search index.
     * 
     * @return Statistics including total terms and document count
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIndexStats() {
        try {
            Map<String, Object> stats = searchService.getIndexStats();
            return ResponseEntity.ok(ApiResponse.success("Index statistics retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get index stats: " + e.getMessage()));
        }
    }

    /**
     * Gets a document by its ID.
     *
     * @param id The ID of the document to retrieve
     * @return The document if found
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<DocumentDto>> getDocument(@PathVariable String id) {
        try {
            DocumentDto doc = searchService.getDocumentById(id);
            if (doc == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Document not found with id: " + id));
            }
            return ResponseEntity.ok(ApiResponse.success("Document retrieved", doc));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve document: " + e.getMessage()));
        }
    }
}
