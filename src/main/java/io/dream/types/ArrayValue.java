package io.dream.types;

import io.dream.config.Messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Array Value representation
 * Stores array elements with their indices
 */
public class ArrayValue implements Value
{
    private final ArrayType arrayType;
    private final Map<Integer, Value> elements;

    public ArrayValue(ArrayType arrayType)
    {
        this.arrayType = arrayType;
        this.elements = new HashMap<>();

        // Initialize all elements to their zero values
        Type elementType = arrayType.getElementType();
        Value zeroValue = elementType.zeroValue();

        for (int i = arrayType.getLowerBound(); i <= arrayType.getUpperBound(); i++)
        {
            elements.put(i, zeroValue);
        }
    }

    public ArrayType getArrayType()
    {
        return arrayType;
    }

    /**
     * Get element at index
     */
    public Value get(int index)
    {
        if (!arrayType.isValidIndex(index))
        {
            throw new RuntimeException(
                    Messages.arrayIndexOutOfBounds(index, arrayType.getLowerBound(), arrayType.getUpperBound())
            );
        }
        return elements.get(index);
    }

    /**
     * Set element at index
     */
    public void set(int index, Value value)
    {
        if (!arrayType.isValidIndex(index))
        {
            throw new RuntimeException(
                    Messages.arrayIndexOutOfBounds(index, arrayType.getLowerBound(), arrayType.getUpperBound())
            );
        }
        elements.put(index, value);
    }

    /**
     * Get all elements (for iteration)
     */
    public Map<Integer, Value> getElements()
    {
        return new HashMap<>(elements);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = arrayType.getLowerBound(); i <= arrayType.getUpperBound(); i++)
        {
            if (i > arrayType.getLowerBound())
            {
                sb.append(", ");
            }
            Value element = elements.get(i);
            if (element instanceof AtomicValue)
            {
                sb.append(((AtomicValue<?>) element).getValue());
            }
            else
            {
                sb.append(element.toString());
            }
        }

        sb.append("]");
        return sb.toString();
    }
}
