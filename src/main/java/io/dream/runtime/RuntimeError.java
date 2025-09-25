package io.dream.runtime;

import io.dream.scanner.Token;

public class RuntimeError extends RuntimeException
{
    private final Token token;

    public RuntimeError(Token token, String message)
    {
        super(message);
        this.token = token;
    }

    public Token token() { return this.token; }
}
