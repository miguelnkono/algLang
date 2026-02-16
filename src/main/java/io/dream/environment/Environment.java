package io.dream.environment;

import io.dream.types.Type;
import io.dream.types.Value;

import java.util.HashMap;
import java.util.Map;

public class Environment
{
    private final Map<String, Value> values = new HashMap<>();

    /**
     * This function create a new variable.
     * @param name the name of the variable.
     * @param value the type of the variable.
     * */
    public void define(String name, Value value) {
        // we don't allow the redefinition of a variable
        if (values.containsKey(name)) {
            throw new RuntimeException("Variable '" + name + "' is already defined.");
        }

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
            throw new RuntimeException("Variable '" + name + "' is not defined.");
        }
    }

    public void update_value(String name, Value new_value) {
        if (values.putIfAbsent(name, new_value) != null) {
            throw new RuntimeException("Variable '" + name + "' is already defined.");
        }
    }
}
