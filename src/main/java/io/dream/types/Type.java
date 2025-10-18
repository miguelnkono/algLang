package io.dream.types;

public interface Type
{
  // this interface represent the type of our language.

  /**
   * This function returns <strong>true</strong> if the two types are match, otherwise it returns <strong>false</strong>.
   * @param other the other type to match with.
   * @return true | false.
   * */
  public abstract boolean equals(Type other);

  /**
   * ZeroValue returns the default value to be returned when a variable of this type is declared without explicit initialization.
   * @return Value.
   * */
  public abstract Value zeroValue();

  /**
   * This function will describe the type.
   * @return description.
   * */
  public abstract String toString();
}
