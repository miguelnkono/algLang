package io.dream.scanner;

/**
 * The enum Token type.
 */
public enum TokenType
{
    // single character tokens
    COMMA, SEMICOLON, COLON,
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACKET, RIGHT_BRACKET,
    BANG,

    // one or two character tokens
    LESS, LESS_OR_EQUAL, DIFF, ASSIGN,
    GREATER, GREATER_OR_EQUAL,
    MINUS, MINUS_MINUS, PLUS,
    PLUS_PLUS, SLASH, SLASH_SLASH,
    STAR, STAR_STAR,
    EQUAL, EQUAL_EQUAL,

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
    INTEGER_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,
    ECRIRE,

  // ambiguous
  INDENT,
    EOF
}
