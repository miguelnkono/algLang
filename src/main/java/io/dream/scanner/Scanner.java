package io.dream.scanner;

import static io.dream.scanner.TokenType.ASSIGN;
import static io.dream.scanner.TokenType.BANG;
import static io.dream.scanner.TokenType.CHARACTER_LITERAL;
import static io.dream.scanner.TokenType.COLON;
import static io.dream.scanner.TokenType.COMMA;
import static io.dream.scanner.TokenType.DIFF;
import static io.dream.scanner.TokenType.DOUBLE_LITERAL;
import static io.dream.scanner.TokenType.EOF;
import static io.dream.scanner.TokenType.EQUAL;
import static io.dream.scanner.TokenType.EQUAL_EQUAL;
import static io.dream.scanner.TokenType.GREATER;
import static io.dream.scanner.TokenType.GREATER_OR_EQUAL;
import static io.dream.scanner.TokenType.IDENTIFIER;
import static io.dream.scanner.TokenType.INDENT;
import static io.dream.scanner.TokenType.INTEGER_LITERAL;
import static io.dream.scanner.TokenType.LEFT_BRACKET;
import static io.dream.scanner.TokenType.LEFT_PAREN;
import static io.dream.scanner.TokenType.LESS;
import static io.dream.scanner.TokenType.LESS_OR_EQUAL;
import static io.dream.scanner.TokenType.MINUS;
import static io.dream.scanner.TokenType.MINUS_MINUS;
import static io.dream.scanner.TokenType.PLUS;
import static io.dream.scanner.TokenType.PLUS_PLUS;
import static io.dream.scanner.TokenType.RIGHT_BRACKET;
import static io.dream.scanner.TokenType.RIGHT_PAREN;
import static io.dream.scanner.TokenType.SEMICOLON;
import static io.dream.scanner.TokenType.SLASH;
import static io.dream.scanner.TokenType.STAR;
import static io.dream.scanner.TokenType.STAR_STAR;
import static io.dream.scanner.TokenType.STRING_LITERAL;

import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dream.Main;
import io.dream.config.Config;
import io.dream.config.Messages;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;
import io.dream.types.Value;

/**
 * The type Scanner.
 */
public class Scanner
{
    protected final String source;
    protected final List<Token> tokens;

    protected int start = 0;
    protected int current = 0;
    protected int line = 1;

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
        this.tokens.add(new Token(EOF, "nil", null, this.line));
        return this.tokens;
    }

    /**
     * This function scan a single token and adds it to the tokens list
     * return void
     */
    protected void scanToken()
    {
        char c = advance();
        switch (c)
        {
            // single character token
            // todo: I should find a way to switch accordingly to the --language flag on the case switch.
            // So for now please when using the interpreter use floating number according to the language level you set.
            case ',':
            {
                if (Config.getLanguage())
                {
                    addToken(COMMA);
                    break;
                } else
                {
                    Main.error(line, Messages.wrongDecimalSeparatorEnglish());
                    break;
                }
            }
            case '.':
            {
                if (!Config.getLanguage())
                {
                    addToken(COMMA);
                    break;
                } else
                {
                    Main.error(this.line, Messages.wrongDecimalSeparatorFrench());
                    break;
                }
            }

            case ';':
                addToken(SEMICOLON);
                break;
            case ':':
                addToken(COLON);
                break;
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '[':
                addToken(LEFT_BRACKET);
                break;
            case ']':
                addToken(RIGHT_BRACKET);
                break;

            // double character tokens
            case '<':
                if (match('='))
                {
                    addToken(LESS_OR_EQUAL);
                } else if (match('-'))
                {
                    addToken(ASSIGN);
                } else
                {
                    addToken(LESS);
                }
                break;
            case '>':
                addToken(match('=') ? GREATER_OR_EQUAL : GREATER);
                break;
            case '!':
                addToken(match('=') ? DIFF : BANG);
                break;
            case '-':
                addToken(match('-') ? MINUS_MINUS : MINUS);
                break;
            case '+':
                addToken(match('+') ? PLUS_PLUS : PLUS);
                break;
            case '/':
                if (match('/'))
                {
                    // this is the case when it is a comment.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else
                {
                    // in this case it is a division operator.
                    addToken(SLASH);
                }
                break;
            case '*':
                addToken(match('*') ? STAR_STAR : STAR);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;

            // This indent token is used to delimit a block.
            case '\t':
                // produce an INDENT token when the indentation is not followed by a new line.
                if (this.peek() != '\n' && this.peek() != ' ' && this.peek() != '\r')
                {
                    addToken(INDENT);
                }
                break;

            // meaningless characters and new line character.
            case ' ':
            case '\r':
            case '\\':
                break;
            case '\n':
                this.line++;
                break;

            // scan strings.
            case '"':
                this.string();
                break;

            // scan character literals
            case '\'':
                this.character();
                break;

            default:
                if (this.isDigit(c))
                {
                    this.number();
                } else if (this.isAlpha(c))
                {
                    this.identifier();
                } else
                {
                    Main.error(line, Messages.unsupportedCharacter());
                    break;
                }
        }
    }

    /**
     * this function scan an identifier.
     *
     */
    protected void identifier()
    {
        while (this.isAlphaNumeric(this.peek())) advance();

        String text = this.source.substring(this.start, this.current);
        Map<String, TokenType> keywords = Config.getLanguage() ? Config.keywordsFrench() : Config.keywordsEnglish();
        TokenType relatedTokenTypeToText = keywords.get(text);
        if (relatedTokenTypeToText == null) relatedTokenTypeToText = IDENTIFIER;

        this.addToken(relatedTokenTypeToText);
    }

    /**
     * This function scan an integer or a decimal depending on whether the number contains the
     * comma or dot symbol in it or not.
     *
     */
    protected void number()
    {
        boolean isDecimal = false;

        while (this.isDigit(this.peek())) advance();
        char number_char = (Config.getLanguage() ? ',' : '.');

        // if decimal is found.
        if (this.peek() == number_char && this.isDigit(this.peekNext()))
        {
            isDecimal = true;
            advance();
            while (this.isDigit(this.peek())) advance();
        }

        if (isDecimal)
        {
            String number_digit = Config.getLanguage() ?
                    this.source.substring(this.start, this.current).replace(',', '.') :
                    this.source.substring(this.start, this.current);
            double doubleValue = Double.parseDouble(number_digit);
            this.addToken(DOUBLE_LITERAL, new AtomicValue<Double>(doubleValue, AtomicTypes.FLOATING));
        } else
        {

            int integerValue = Integer.parseInt(this.source.substring(this.start,
                    this.current));
            this.addToken(INTEGER_LITERAL, new AtomicValue<Integer>(integerValue, AtomicTypes.INTEGER));
        }
    }

    /**
     * This function scan through a string and construct one.
     *
     */
    protected void string()
    {
        while (this.peek() != '"' && !this.isAtEnd())
        {
            // found a new line.
            if (this.peek() == '\n')
            {
                this.line++;
            }
            this.advance();
        }

        // if we reach the end of the line but did not finish the string.
        if (this.isAtEnd())
        {
            Main.error(line, Messages.unterminatedString());
            return;
        }

        // advance to consume the end '"' character of the string.
        this.advance();
        String string = this.source.substring(start + 1, current - 1);
        this.addToken(STRING_LITERAL, new AtomicValue<>(string, AtomicTypes.STRING));
    }

    /**
     * This function scan through a character literal and construct one.
     *
     */
    protected void character()
    {
        if (this.isAtEnd())
        {
            Main.error(line, Messages.unterminatedCharacter());
            return;
        }

        // Get the character
        char ch = this.advance();

        // Check for closing quote
        if (this.peek() != '\'')
        {
            Main.error(line, Messages.characterMustBeOne());
            return;
        }

        // Consume the closing quote
        this.advance();

        AtomicValue<Character> charAtomicValue = new AtomicValue<>(ch, AtomicTypes.CHAR);
        this.addToken(CHARACTER_LITERAL, charAtomicValue);
    }

    /**
     * This function return the current in the source code of the user.
     *
     */
    protected char peek()
    {
        if (this.isAtEnd()) return '\0';
        return this.source.charAt(this.current);
    }

    protected char peekNext()
    {
        if (this.current + 1 >= this.source.length()) return '\0';
        return this.source.charAt(this.current + 1);
    }

    /**
     * This function help us know if we are at the end of the file or not.
     *
     * @return returns whether we are at the end of the file or not
     *
     */
    protected boolean isAtEnd()
    {
        return this.current >= this.source.length();
    }

    /**
     * This function adds a new token in the tokens list.
     * The token it adds has a literal set.
     *
     */
    protected void addToken(TokenType type, Value literal)
    {
        String text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(type, text, literal, this.line));
    }

    /**
     * This function adds a new token that does not have a literal part in the tokens list.
     *
     */
    protected void addToken(TokenType type)
    {
        this.addToken(type, null);
    }

    /**
     * This function read the current character in the source code, returns it and advance the
     * current cursor in the source code.
     *
     */
    protected char advance()
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
     *
     */
    protected boolean match(char ch)
    {
        if (isAtEnd()) return false;
        if (this.source.charAt(this.current) != ch) return false;

        this.current++;
        return true;
    }

    /**
     * This helper function check to see if we are in present of a number digit.
     *
     * @param ch represent the character to check if it is a number or not.
     *
     */
    protected boolean isDigit(char ch)
    {
        return ch >= '0' && ch <= '9';
    }

    /**
     * This helper function check to see if the character is an alpha character or not.
     * An identifier start whether with a lowercase letter or an uppercase letter.
     *
     * @param ch represent the character to check if it is an alpha character.
     *
     */
    protected boolean isAlpha(char ch)
    {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    /**
     * Helper function to check whether a character is a digit or an identifier.
     *
     * @param ch represent the character to check if it is a character.
     *
     */
    protected boolean isAlphaNumeric(char ch)
    {
        return isDigit(ch) || isAlpha(ch);
    }
}
