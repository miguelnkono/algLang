package io.dream.scanner;

import io.dream.types.Value;

/**
 * The type Token.
 *
 * @param type the type of the token
 * @param lexeme the lexeme of the token
 * @param literal the runtime representation of the token if there is any
 * @param line the line where the token was found
 */
public record Token (TokenType type, String lexeme, Value literal, int line)
{

    @Override
    public String toString()
    {
        return this.type + " " + this.lexeme + " " + this.literal;
    }
}
