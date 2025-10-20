package io.dream.types;

public class AtomicValue<T> implements Value
{
  private final T value;
  private final AtomicTypes atomicType;

  public AtomicValue(T value, AtomicTypes type)
  {
    this.value = value;
    this.atomicType = type;
  }

  public T getValue() {
    return value;
  }

  public AtomicTypes getAtomicType() {
    return atomicType;
  }

  public Type getType()
  {
    return TypeFactory.getAtomicType(this.atomicType);
  }

  @Override
  public String toString() {
    return String.format("%s", this.value);
  }
}
