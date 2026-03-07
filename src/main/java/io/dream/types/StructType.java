package io.dream.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structure Type representation
 * Represents user-defined structures with named fields
 */
public class StructType implements Type
{
    private final String name;
    private final Map<String, Type> fields; // LinkedHashMap to preserve field order

    public StructType(String name, Map<String, Type> fields)
    {
        this.name = name;
        this.fields = new LinkedHashMap<>(fields); // Preserve insertion order
    }

    public String getName()
    {
        return name;
    }

    public Map<String, Type> getFields()
    {
        return new LinkedHashMap<>(fields);
    }

    public Type getFieldType(String fieldName)
    {
        return fields.get(fieldName);
    }

    public boolean hasField(String fieldName)
    {
        return fields.containsKey(fieldName);
    }

    @Override
    public boolean equals(Type other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || !(other instanceof StructType))
        {
            return false;
        }

        StructType that = (StructType) other;

        // Two structure types are equal if they have the same name
        // (assuming structures with the same name have the same definition)
        return this.name.equals(that.name);
    }

    @Override
    public Value zeroValue()
    {
        // Create a structure value with all fields initialized to their zero values
        return new StructValue(this);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
