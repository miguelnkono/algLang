package io.dream;

import io.dream.scanner.Token;
import io.dream.scanner.TokenType;

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
     * @param source the source
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
            this.start = this.current;

            this.scanToken();
        }

        this.tokens.add(new Token(OEF, "", null, this.line));
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
            case ',': addToken(COMMA); break;
            case ';': addToken(SEMICOLON); break;
            case ':': addToken(COLON); break;
            case '(':  addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ']': addToken(RIGHT_BRACKET); break;

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
}
