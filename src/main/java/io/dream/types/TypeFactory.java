package io.dream.types;

public class TypeFactory
{
  /***
   * This function will return the AtomicType corresponding to the given AtomicTypes enum.
   * @param atomicType AtomicTypes enum.
   * @return AtomicType.
   * */
  public static Type getAtomicType(AtomicTypes atomicType)
  {
    return switch (atomicType)
    {
      case INTEGER -> new AtomicType("entier", new AtomicValue<Integer>(0, AtomicTypes.INTEGER));
      case FLOATING -> new AtomicType("reel", new AtomicValue<Double>(0.0, AtomicTypes.FLOATING));
      case STRING -> new AtomicType("chaine_character", new AtomicValue<String>("", AtomicTypes.STRING));
      case CHAR -> new AtomicType("char", new AtomicValue<Character>('\u0000', AtomicTypes.CHAR));
      case BOOLEAN -> new AtomicType("booleen", new AtomicValue<Boolean>(false, AtomicTypes.BOOLEAN));
      case VOID -> new AtomicType("nil", null);
      default -> throw new IllegalArgumentException("Type basique non reconnu: " + atomicType);
    };
  }
}
