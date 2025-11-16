package com.example.lucenePrototype.api.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DocumentDto {
    private String id;
    private List<FieldDto> fields = new ArrayList<>();
    
    public void addField(FieldDto field) {
        this.fields.add(field);
    }
}
