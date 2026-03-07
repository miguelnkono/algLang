package io.dream.scanner;

/**
 * Complete Token Types for AlgoLang
 */
public enum TokenType
{
    // Single character tokens
    COMMA, SEMICOLON, COLON, DOT,
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACKET, RIGHT_BRACKET,
    LEFT_BRACE, RIGHT_BRACE,
    BANG,

    // One or two character tokens
    LESS, LESS_OR_EQUAL, DIFF, ASSIGN,
    GREATER, GREATER_OR_EQUAL,
    MINUS, MINUS_MINUS, PLUS,
    PLUS_PLUS, SLASH, SLASH_SLASH,
    STAR, STAR_STAR,
    EQUAL, EQUAL_EQUAL,
    DOT_DOT,  // .. for array ranges

    // Literals
    IDENTIFIER,
    INTEGER_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,
    CHARACTER_LITERAL,

    // Type keywords
    INTEGER, DOUBLE, STRING, CHARACTER, BOOLEAN,
    NUMBER,  // Generic number type
    TABLE,   // Array/Table

    // Program structure keywords
    ALGORITHM,
    VARIABLE,
    CONSTANT,
    TYPE,
    BEGIN, END,

    // Function/Method keywords
    FUNCTION, END_FUNCTION,
    METHOD, END_METHOD,
    RETURN,

    // Structure keywords
    STRUCTURE, END_STRUCT,

    // Control flow keywords
    IF, THEN, ELSE, ELSEIF, ENDIF,
    FOR, TO, STEP, ENDFOR,
    WHILE, DO, ENDWHILE,
    REPEAT, UNTIL,

    // Logical operators
    AND, OR, NOT,

    // I/O keywords
    WRITE, READ,

    // Boolean literals
    TRUE, FALSE,

    // Other keywords
    NIL,
    CLASS,
    MOD,  // Modulo operator
    OF,   // For array declarations: array[1..10] of integer

    // Ambiguous/Special
    INDENT,
    EOF
}
