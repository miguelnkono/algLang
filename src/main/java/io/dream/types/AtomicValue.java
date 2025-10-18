package io.dream.types;

public class AtomicValue<T> implements Value
{
  private final T value;
  private final AtomicTypes type;

  public AtomicValue(T value, AtomicTypes type) {
    this.value = value;
    this.type = type;
  }

  public T getValue() {
    return value;
  }

  public AtomicTypes getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", this.value, this.type);
  }

  public static void main(String[] args)
  {
    AtomicValue<Void> atomic = new AtomicValue<>(null, AtomicTypes.VOID);
    System.out.println(atomic.getType());
  }
}
