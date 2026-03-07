package io.dream.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Function Type representation
 * Represents function signatures with parameter types and return type
 */
public class FunctionType implements Type
{
    private final String name;
    private final List<Type> parameterTypes;
    private final Type returnType;

    public FunctionType(String name, List<Type> parameterTypes, Type returnType)
    {
        this.name = name;
        this.parameterTypes = new ArrayList<>(parameterTypes);
        this.returnType = returnType;
    }

    public String getName()
    {
        return name;
    }

    public List<Type> getParameterTypes()
    {
        return new ArrayList<>(parameterTypes);
    }

    public Type getReturnType()
    {
        return returnType;
    }

    public int getArity()
    {
        return parameterTypes.size();
    }

    @Override
    public boolean equals(Type other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || !(other instanceof FunctionType))
        {
            return false;
        }

        FunctionType that = (FunctionType) other;

        // Two function types are equal if they have the same signature
        if (!this.returnType.equals(that.returnType))
        {
            return false;
        }

        if (this.parameterTypes.size() != that.parameterTypes.size())
        {
            return false;
        }

        for (int i = 0; i < this.parameterTypes.size(); i++)
        {
            if (!this.parameterTypes.get(i).equals(that.parameterTypes.get(i)))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Value zeroValue()
    {
        // Functions don't have a zero value
        return null;
    }

    @Override
    public String toString()
    {
        String params = parameterTypes.stream()
                .map(Type::toString)
                .collect(Collectors.joining(", "));
        return name + "(" + params + "): " + returnType.toString();
    }
}
