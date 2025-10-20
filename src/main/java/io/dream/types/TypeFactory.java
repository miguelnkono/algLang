package io.dream.types;

import java.util.HashMap;
import java.util.Map;

public class TypeFactory
{

    private static final Map<AtomicTypes, AtomicType> types = new HashMap<>();

    static
    {
        types.put(AtomicTypes.INTEGER, new AtomicType("entier", new AtomicValue<Integer>(0, AtomicTypes.INTEGER)));
        types.put(AtomicTypes.FLOATING, new AtomicType("reel", new AtomicValue<Double>(0.0, AtomicTypes.FLOATING)));
        types.put(AtomicTypes.STRING, new AtomicType("chaîne", new AtomicValue<String>("", AtomicTypes.STRING)));
        types.put(AtomicTypes.CHAR, new AtomicType("char", new AtomicValue<Character>('\u0000', AtomicTypes.CHAR)));
        types.put(AtomicTypes.BOOLEAN, new AtomicType("booléen", new AtomicValue<Boolean>(false, AtomicTypes.BOOLEAN)));
        types.put(AtomicTypes.VOID, new AtomicType("nil", null));
    }

    public static final AtomicType INTEGER = types.get(AtomicTypes.INTEGER);
    public static final AtomicType FLOATING = types.get(AtomicTypes.FLOATING);
    public static final AtomicType STRING = types.get(AtomicTypes.STRING);
    public static final AtomicType CHAR = types.get(AtomicTypes.CHAR);
    public static final AtomicType BOOLEAN = types.get(AtomicTypes.BOOLEAN);
    public static final AtomicType VOID = types.get(AtomicTypes.VOID);

    public static Type getAtomicType(AtomicTypes atomicType)
    {
        Type type = types.get(atomicType);
        if (type == null)
        {
            throw new IllegalArgumentException("Type basique non reconnu: " + atomicType);
        }
        return type;
    }

    // Helper to check if a type is numeric
    public static boolean isNumeric(Type type)
    {
        return type.equals(INTEGER) || type.equals(FLOATING);
    }
}