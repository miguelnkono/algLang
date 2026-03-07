package io.dream.types;

/**
 * Array Type representation
 * Represents arrays with a base type and range (e.g., tableau[1..10] de entier)
 */
public class ArrayType implements Type
{
    private final Type elementType;
    private final int lowerBound;
    private final int upperBound;
    private final String name;

    public ArrayType(Type elementType, int lowerBound, int upperBound)
    {
        this.elementType = elementType;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.name = "tableau[" + lowerBound + ".." + upperBound + "] de " + elementType.toString();
    }

    public Type getElementType()
    {
        return elementType;
    }

    public int getLowerBound()
    {
        return lowerBound;
    }

    public int getUpperBound()
    {
        return upperBound;
    }

    public int getSize()
    {
        return upperBound - lowerBound + 1;
    }

    @Override
    public boolean equals(Type other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || !(other instanceof ArrayType))
        {
            return false;
        }

        ArrayType that = (ArrayType) other;
        return this.lowerBound == that.lowerBound &&
                this.upperBound == that.upperBound &&
                this.elementType.equals(that.elementType);
    }

    @Override
    public Value zeroValue()
    {
        // Create an array value with all elements initialized to their zero values
        return new ArrayValue(this);
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * Check if an index is within the array bounds
     */
    public boolean isValidIndex(int index)
    {
        return index >= lowerBound && index <= upperBound;
    }
}
