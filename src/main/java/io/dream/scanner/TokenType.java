package io.dream.scanner;

/**
 * The enum Token type.
 */
public enum TokenType
{
    // single character tokens
    COMMA, SEMICOLON, COLON, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,

    // one or two character tokens
    LESS, LESS_OR_EQUAL,
    GREATER, GREATER_OR_EQUAL,
    BANG, LESS_GREATER,
    MINUS, PLUS, SLASH, STAR,

    // literals
    IDENTIFIER, INTEGER, DOUBLE, STRING, CHARACTER,

    // keywords
    ALGORITHM, VARIABLE, BEGIN, END,
    METHOD, CLASS,
    IF, ELSE,
    FOR, WHILE, DO_WHILE,
    TRUE, FALSE,
    NIL,
    TABLE,

    OEF
}
