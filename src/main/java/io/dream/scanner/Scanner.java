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
            case '/':
                if (match('/'))
                {
                    // this is the case when it is a comment.
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                else
                {
                    // in this case it is a division operator.
                    addToken(SLASH);
                }
                break;
            case '*': addToken(match('*') ? STAR_STAR : STAR); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL);

            // meaningless characters and new line character.
            case ' ':
            case '\t':
            case '\r':
            case '\\':
                break;
            case '\n':
                this.line++;
                break;

            // scan strings.
            case '"': string(); break;

            default:
                if (isDigit(c))
                {
                    number();
                }
                else {
                    Main.error(line, "Unsupported character.");
                    break;
                }
        }
    }

    /**
     * This function scan an integer or a decimal depending on whether the number contains the
     * comma sign in it or not.
     * */
    private void number()
    {
        boolean isDecimal = false;

        while (this.isDigit(this.peek())) advance();

        // if decimal is found.
        if (this.peek() == ',' && this.isDigit(this.peekNext()))
        {
            isDecimal = true;
            advance();
            while (this.isDigit(this.peek())) advance();
        }

        if (isDecimal)
        {
            this.addToken(DOUBLE, Double.parseDouble(this.source.substring(this.start,
                    this.current).replace(",", ".")));
        }
        else
        {
            this.addToken(INTEGER, Integer.parseInt(this.source.substring(this.start, this.current)));
        }
    }

    /**
     * This function scan through a string and construct one.
     * */
    private void string()
    {
        while (this.peek() != '"' && !this.isAtEnd())
        {
            if (this.peek() == '\n')
            {
                this.line++;
            }
            this.advance();
        }

        // if we reach the end of the line but did not finish the string.
        if (this.isAtEnd())
        {
            Main.error(line, "Unterminated string literal.");
            return;
        }

        // advance to consume the end '"' character of the string.
        this.advance();
        String string = this.source.substring(start + 1, current - 1);
        this.addToken(STRING, string);
    }

    /**
     * This function return the current in the source code of the user.
     * */
    private char peek()
    {
        if (this.isAtEnd()) return '\0';
        return this.source.charAt(this.current);
    }

    private char peekNext()
    {
        if (this.current + 1 >= this.source.length()) return '\0';
        return this.source.charAt(this.current + 1);
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

    /**
     * This helper function check to see if we are in present of a number digit.
     * @param ch represent the character to check if it is a number or not.
     * */
    private boolean isDigit(char ch)
    {
        return ch >= '0' && ch <= '9';
    }
}
