package io.dream.scanner;

import io.dream.Main;

import java.util.ArrayList;
import java.util.List;

import static io.dream.scanner.TokenType.*;

/**
 * The type Scanner.
 */
public class Scanner
{
    private final String source;
    private final List<Token> tokens;

    private int start = 0;
    private int current = 0;
    private int line = 0;

    /**
     * Instantiates a new Scanner.
     *
     * @param source the source code of the user
     */
    public Scanner(String source)
    {
        this.source = source;
        this.tokens = new ArrayList<>();
    }

    /**
     * Scan the source code to produce a tokens list.
     *
     * @return the list of tokens
     */
    public List<Token> scanTokens()
    {
        while (!this.isAtEnd())
        {
            // start over to the next token
            this.start = this.current;

            this.scanToken();
        }

        // at the end of the token list we add an EOF token to mark it done
        this.tokens.add(new Token(EOF, "", null, this.line));
        return this.tokens;
    }

    /**
     * This function scan a single token and adds it to the tokens list
     * */
    private void scanToken()
    {
        char c = advance();
        switch (c)
        {
            // single character token
            case ',': addToken(COMMA); break;
            case ';': addToken(SEMICOLON); break;
            case ':': addToken(COLON); break;
            case '(':  addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ']': addToken(RIGHT_BRACKET); break;

            // double character tokens
            case '<': addToken(match('=') ?  LESS_OR_EQUAL : LESS); break;
            case '>': addToken(match('=') ?  GREATER_OR_EQUAL : GREATER); break;
            case '!': addToken(match('=') ? DIFF : BANG); break;
            case '-': addToken(match('-') ? MINUS_MINUS : MINUS); break;
            case '+': addToken(match('+') ? PLUS_PLUS : PLUS); break;
            case '/': addToken(match('/') ? SLASH_SLASH : SLASH); break;
            case '*': addToken(match('*') ? STAR_STAR : STAR); break;

            default:
                Main.error(line, "Unsupported character.");
                break;
        }
    }

    /**
     * This function help us know if we are at the end of the file or not.
     *
     * @return returns whether we are at the end of the file or not
     * */
    private boolean isAtEnd()
    {
        return this.current >= this.source.length();
    }

    /**
     * This function adds a new token in the tokens list.
     * The token it adds has a literal set.
     * */
    private void addToken(TokenType type, Object literal)
    {
        String text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(type, text, literal, this.line));
    }

    /**
     * This function adds a new token that does not have a literal part in the tokens list.
     * */
    private void addToken(TokenType type)
    {
        this.addToken(type, null);
    }

    /**
     * This function read the current character in the source code, returns it and advance the
     * current cursor in the source code.
     * */
    private char advance()
    {
        return this.source.charAt(this.current++);
    }

    /**
     * This function will check to see if the next character in the source code of the user match
     * a specific character, if it does, we are going to advance the current position cursor and
     * return returns but if it does not we are simply going to returns.
     *
     * @param ch represent the character we want to check the equality with
     * @return returns whether the ch parameter value match or not
     * */
    private boolean match(char ch)
    {
        if (isAtEnd()) return false;
        if (this.source.charAt(this.current) != ch) return false;

        this.current++;
        return true;
    }
}
