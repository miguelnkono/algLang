package io.dream.types;

import io.dream.config.Messages;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structure Value representation
 * Stores structure field values
 */
public class StructValue implements Value
{
    private final StructType structType;
    private final Map<String, Value> fieldValues;

    public StructValue(StructType structType)
    {
        this.structType = structType;
        this.fieldValues = new LinkedHashMap<>();

        // Initialize all fields to their zero values
        for (Map.Entry<String, Type> entry : structType.getFields().entrySet())
        {
            String fieldName = entry.getKey();
            Type fieldType = entry.getValue();
            fieldValues.put(fieldName, fieldType.zeroValue());
        }
    }

    public StructType getStructType()
    {
        return structType;
    }

    /**
     * Get field value
     */
    public Value getField(String fieldName)
    {
        if (!structType.hasField(fieldName))
        {
            throw new RuntimeException(
                    Messages.fieldNotFound(structType.getName(), fieldName)
            );
        }
        return fieldValues.get(fieldName);
    }

    /**
     * Set field value
     */
    public void setField(String fieldName, Value value)
    {
        if (!structType.hasField(fieldName))
        {
            throw new RuntimeException(
                    Messages.fieldNotFound(structType.getName(), fieldName)
            );
        }
        fieldValues.put(fieldName, value);
    }

    /**
     * Get all field values (for iteration)
     */
    public Map<String, Value> getFieldValues()
    {
        return new LinkedHashMap<>(fieldValues);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(structType.getName()).append("{");

        boolean first = true;
        for (Map.Entry<String, Value> entry : fieldValues.entrySet())
        {
            if (!first)
            {
                sb.append(", ");
            }
            first = false;

            sb.append(entry.getKey()).append(": ");
            Value value = entry.getValue();

            if (value instanceof AtomicValue)
            {
                sb.append(((AtomicValue<?>) value).getValue());
            }
            else
            {
                sb.append(value.toString());
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
