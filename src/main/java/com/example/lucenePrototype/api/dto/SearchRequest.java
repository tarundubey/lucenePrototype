package com.example.lucenePrototype.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SearchRequest {
    @NotBlank(message = "Query cannot be empty")
    private String query;
    
    /**
     * Fields to search in. If not specified, searches in all fields.
     * Example: ["title", "content"]
     */
    private List<String> searchFields;
    
    /**
     * Fields to return in the results. If not specified, returns all stored fields.
     * Example: ["title", "snippet"]
     */
    private List<String> returnFields;
    
    /**
     * Maximum number of results to return. Default is 10.
     */
    private int limit = 10;
    
    /**
     * Offset for pagination. Default is 0.
     */
    private int offset = 0;
    
    /**
     * Whether to highlight matching terms in the results.
     */
    private boolean highlight = true;
}
