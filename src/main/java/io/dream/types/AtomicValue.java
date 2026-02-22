package io.dream.types;

import java.util.Objects;

public class AtomicValue<T> implements Value
{
    private final T value;
    private final AtomicTypes atomicType;

    public AtomicValue(T value, AtomicTypes type)
    {
        this.value = value;
        this.atomicType = type;
    }

    public T getValue()
    {
        return value;
    }

    public AtomicTypes getAtomicType()
    {
        return atomicType;
    }

    public Type getType()
    {
        return TypeFactory.getAtomicType(this.atomicType);
    }

    @Override
    public String toString()
    {
        return String.format("%s", this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicValue<?> that = (AtomicValue<?>) o;
        return Objects.equals(value, that.value) &&
                atomicType == that.atomicType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, atomicType);
    }
}
