package io.dream.types;

public class AtomicType implements Type
{
  private final String name;
  private final Value zeroValue;  // the value to return in case we define a variable without assigning it an initial value.

  public AtomicType(String name , Value zeroValue)
  {
    this.name = name;
    this.zeroValue = zeroValue;
  }

  @Override
  public boolean equals(Type other)
  {
    if (this == other)
    {
      return true;
    }

//    if (!(other instanceof  AtomicTypes)) return false;

    if (other == null)
    {
      return false;
    }

    AtomicType that = (AtomicType) other;
    return this.name.equals(that.name);
  }

  @Override
  public Value zeroValue()
  {
    return this.zeroValue;
  }

  public String getName()
  {
    return name;
  }

  public Value getZeroValue()
  {
    return zeroValue;
  }

  @Override
  public String toString()
  {
    return this.name;
  }
}
