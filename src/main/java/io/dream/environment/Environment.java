package io.dream.environment;

import io.dream.config.Messages;
import io.dream.types.Type;
import io.dream.types.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Environment with scope support
 * Supports parent scopes for functions and blocks
 */
public class Environment
{
    private final Map<String, Value> values = new HashMap<>();
    private final Map<String, Type> types = new HashMap<>();
    private final Environment parent;

    /**
     * Create global environment (no parent)
     */
    public Environment()
    {
        this.parent = null;
    }

    /**
     * Create nested environment with parent scope
     */
    public Environment(Environment parent)
    {
        this.parent = parent;
    }

    /**
     * Define a new variable
     */
    public void define(String name, Type type, Value value)
    {
        // We allow redefinition in local scopes (shadowing)
        // but not in the same scope
        if (values.containsKey(name) && parent == null)
        {
            throw new RuntimeException(Messages.variableAlreadyDefined(name));
        }

        types.put(name, type);
        values.put(name, value);
    }

    /**
     * Get variable value (checks parent scopes)
     */
    public Value get_value(String name)
    {
        if (values.containsKey(name))
        {
            return values.get(name);
        }

        // Check parent scope
        if (parent != null)
        {
            return parent.get_value(name);
        }

        throw new RuntimeException(Messages.variableNotDefined(name));
    }

    /**
     * Get variable type (checks parent scopes)
     */
    public Type get_type(String name)
    {
        if (types.containsKey(name))
        {
            return types.get(name);
        }

        // Check parent scope
        if (parent != null)
        {
            return parent.get_type(name);
        }

        throw new RuntimeException(Messages.variableNotDefined(name));
    }

    /**
     * Update variable value (checks parent scopes)
     */
    public void update_value(String name, Value new_value)
    {
        if (values.containsKey(name))
        {
            values.put(name, new_value);
            return;
        }

        // Check parent scope
        if (parent != null)
        {
            parent.update_value(name, new_value);
            return;
        }

        throw new RuntimeException(Messages.variableNotDefined(name));
    }

    /**
     * Check if variable is defined (checks parent scopes)
     */
    public boolean isDefined(String name)
    {
        if (values.containsKey(name))
        {
            return true;
        }

        if (parent != null)
        {
            return parent.isDefined(name);
        }

        return false;
    }

    /**
     * Get parent environment
     */
    public Environment getParent()
    {
        return parent;
    }
}
