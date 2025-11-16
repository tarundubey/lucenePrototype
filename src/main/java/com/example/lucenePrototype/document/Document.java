package com.example.lucenePrototype.document;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A document is a unit of indexing and search.
 * A document is a set of fields, each of which has a name and a value.
 * Implements Serializable to support persistence to disk.
 */

public final class Document implements Serializable {
    //Explicit version id helps to control serialization and prevent issues if new fields are added
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final Map<String, Field> fields = new HashMap<>();
    private final String id;

//    kept string for simplicity
    public Document(String id) {
        this.id = id;
    }

    public void addField(Field field) {
        fields.put(field.getName(), field);
    }


    public String getId() {
        return id;
    }

    public Map<String, Field> getFields() {
        return new HashMap<>(fields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) && 
               Objects.equals(fields, document.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fields);
    }
}
