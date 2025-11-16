package com.example.lucenePrototype.document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * this represents a field in a document that can be indexed and searched.
 * The fields that have stored = true will be stored in the index and can be retrieved later
 * Fields that have indexed = true will be indexed and can be searched - tokenization etc will happen
 * on a field only if index is true
 */
public final class Field implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String value;

    private final boolean stored;
    private final boolean indexed;

    public Field(String name, String value, boolean stored, boolean indexed) {
        this.name = name;
        this.value = value;
        this.stored = stored;
        this.indexed = indexed;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isStored() {
        return stored;
    }

    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return stored == field.stored &&
               indexed == field.indexed &&
               Objects.equals(name, field.name) &&
               Objects.equals(value, field.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, stored, indexed);
    }
}
