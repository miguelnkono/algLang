package io.dream.types;

public class TypeFactory
{

  // Pre-defined type constants for easy access
  public static final AtomicType INTEGER = (AtomicType) getAtomicType(AtomicTypes.INTEGER);
  public static final AtomicType FLOATING = (AtomicType) getAtomicType(AtomicTypes.FLOATING);
  public static final AtomicType STRING = (AtomicType) getAtomicType(AtomicTypes.STRING);
  public static final AtomicType CHAR = (AtomicType) getAtomicType(AtomicTypes.CHAR);
  public static final AtomicType BOOLEAN = (AtomicType) getAtomicType(AtomicTypes.BOOLEAN);
  public static final AtomicType VOID = (AtomicType) getAtomicType(AtomicTypes.VOID);

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

  // Helper to check if a type is numeric
  public static boolean isNumeric(Type type)
  {
    return type.equals(INTEGER) || type.equals(FLOATING);
  }
}