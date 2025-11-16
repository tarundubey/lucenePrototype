package com.example.lucenePrototype.api.dto;

import lombok.Data;

@Data
public class FieldDto {
    public FieldDto() {
    }
    
    public FieldDto(String name, String value, boolean stored, boolean indexed) {
        this.name = name;
        this.value = value;
        this.stored = stored;
        this.indexed = indexed;
    }
    
    private String name;
    private String value;
    private boolean stored = true;
    private boolean indexed = true;
}
