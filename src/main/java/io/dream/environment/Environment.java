package io.dream.environment;

import io.dream.config.Messages;
import io.dream.types.Type;
import io.dream.types.Value;

import java.util.HashMap;
import java.util.Map;

public class Environment
{
    private final Map<String, Value> values = new HashMap<>();
    private final Map<String, Type> types = new HashMap<>();

    /**
     * This function create a new variable.
     * @param name the name of the variable.
     * @param type the type of the variable.
     * @param value the initial value of the variable.
     * */
    public void define(String name, Type type, Value value) {
        // we don't allow the redefinition of a variable
        if (values.containsKey(name)) {
            throw new RuntimeException(Messages.variableAlreadyDefined(name));
        }

        types.put(name, type);
        values.put(name, value);
    }

    /**
     * This function return the value of a variable.
     * @param name the name of the variable.
     * @return the value of the variable.
     * */
    public Value get_value(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        } else {
            throw new RuntimeException(Messages.variableNotDefined(name));
        }
    }

    /**
     * This function return the type of a variable.
     * @param name the name of the variable.
     * @return the type of the variable.
     * */
    public Type get_type(String name) {
        if (types.containsKey(name)) {
            return types.get(name);
        } else {
            throw new RuntimeException(Messages.variableNotDefined(name));
        }
    }

    /**
     * This function updates the value of an existing variable.
     * @param name the name of the variable.
     * @param new_value the new value to assign.
     * */
    public void update_value(String name, Value new_value) {
        if (!values.containsKey(name)) {
            throw new RuntimeException(Messages.variableNotDefined(name));
        }
        values.put(name, new_value);
    }

    /**
     * Check if a variable is defined.
     * @param name the name of the variable.
     * @return true if the variable exists.
     * */
    public boolean isDefined(String name) {
        return values.containsKey(name);
    }
}
