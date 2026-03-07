package io.dream.scanner;

import static io.dream.scanner.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.dream.Main;
import io.dream.config.Config;
import io.dream.config.Messages;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;
import io.dream.types.Value;

/**
 * Scanner/Lexer for AlgoLang
 * Supports all language features including loops, functions, methods, structures, arrays
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
     */
    protected void scanToken()
    {
        char c = advance();
        switch (c)
        {
            // Comma - decimal separator in French
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

            // Dot - decimal separator in English, also field access
            case '.':
            {
                // Check for .. (range operator for arrays)
                if (match('.'))
                {
                    addToken(DOT_DOT);
                    break;
                }

                // Check if next character is a digit (decimal number)
                if (!Config.getLanguage() && isDigit(peek()))
                {
                    // This is part of a number, back up and let number() handle it
                    current--;
                    number();
                    break;
                }

                // Otherwise it's a dot token (field access)
                addToken(DOT);
                break;
            }

            // Single character tokens
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
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;

            // Double character tokens
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
                    // Single line comment - consume until end of line
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                else if (match('*'))
                {
                    // Multi-line comment
                    multiLineComment();
                }
                else
                {
                    // Division operator
                    addToken(SLASH);
                }
                break;

            case '*':
                addToken(match('*') ? STAR_STAR : STAR);
                break;

            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;

            // Indentation (for block detection)
            case '\t':
                if (this.peek() != '\n' && this.peek() != ' ' && this.peek() != '\r')
                {
                    addToken(INDENT);
                }
                break;

            // Whitespace
            case ' ':
            case '\r':
            case '\\':
                break;

            case '\n':
                this.line++;
                break;

            // String literals
            case '"':
                this.string();
                break;

            // Character literals
            case '\'':
                this.character();
                break;

            default:
                if (this.isDigit(c))
                {
                    this.number();
                }
                else if (this.isAlpha(c))
                {
                    this.identifier();
                }
                else
                {
                    Main.error(line, Messages.unsupportedCharacter());
                    break;
                }
        }
    }

    /**
     * Handle multi-line comments
     */
    protected void multiLineComment()
    {
        while (!isAtEnd())
        {
            if (peek() == '*' && peekNext() == '/')
            {
                // Consume the */
                advance();
                advance();
                return;
            }

            if (peek() == '\n')
            {
                line++;
            }

            advance();
        }

        // If we reach here, comment was not closed
        Main.error(line, Messages.unterminatedComment());
    }

    /**
     * Scan an identifier or keyword
     */
    protected void identifier()
    {
        while (this.isAlphaNumeric(this.peek())) advance();

        String text = this.source.substring(this.start, this.current);
        Map<String, TokenType> keywords = Config.getLanguage() ? Config.keywordsFrench() : Config.keywordsEnglish();

        // Check for multi-word keywords like "sinon si" or "else if"
        // Peek ahead to see if this could be part of a multi-word keyword
        if (keywords.containsKey(text))
        {
            // Check if this might be the start of a multi-word keyword
            String potentialMultiWord = text + " " + peekWord();
            if (keywords.containsKey(potentialMultiWord))
            {
                // Consume the whitespace
                while (peek() == ' ' || peek() == '\t') advance();
                // Consume the next word
                while (isAlphaNumeric(peek())) advance();
                TokenType type = keywords.get(potentialMultiWord);
                this.addToken(type);
                return;
            }
        }

        TokenType relatedTokenTypeToText = keywords.get(text);
        if (relatedTokenTypeToText == null) relatedTokenTypeToText = IDENTIFIER;

        this.addToken(relatedTokenTypeToText);
    }

    /**
     * Peek at the next word without consuming it
     */
    protected String peekWord()
    {
        int savedCurrent = this.current;

        // Skip whitespace
        while (savedCurrent < source.length() &&
                (source.charAt(savedCurrent) == ' ' || source.charAt(savedCurrent) == '\t'))
        {
            savedCurrent++;
        }

        // Get the word
        int wordStart = savedCurrent;
        while (savedCurrent < source.length() && isAlphaNumeric(source.charAt(savedCurrent)))
        {
            savedCurrent++;
        }

        if (savedCurrent > wordStart)
        {
            return source.substring(wordStart, savedCurrent);
        }

        return "";
    }

    /**
     * Scan an integer or floating-point number
     */
    protected void number()
    {
        boolean isDecimal = false;

        while (this.isDigit(this.peek())) advance();

        char decimalChar = (Config.getLanguage() ? ',' : '.');

        // Check for decimal point
        if (this.peek() == decimalChar && this.isDigit(this.peekNext()))
        {
            isDecimal = true;
            advance(); // Consume the decimal separator
            while (this.isDigit(this.peek())) advance();
        }

        if (isDecimal)
        {
            String numberStr = Config.getLanguage() ?
                    this.source.substring(this.start, this.current).replace(',', '.') :
                    this.source.substring(this.start, this.current);
            double doubleValue = Double.parseDouble(numberStr);
            this.addToken(DOUBLE_LITERAL, new AtomicValue<Double>(doubleValue, AtomicTypes.FLOATING));
        }
        else
        {
            int integerValue = Integer.parseInt(this.source.substring(this.start, this.current));
            this.addToken(INTEGER_LITERAL, new AtomicValue<Integer>(integerValue, AtomicTypes.INTEGER));
        }
    }

    /**
     * Scan a string literal
     */
    protected void string()
    {
        while (this.peek() != '"' && !this.isAtEnd())
        {
            if (this.peek() == '\n')
            {
                this.line++;
            }
            this.advance();
        }

        if (this.isAtEnd())
        {
            Main.error(line, Messages.unterminatedString());
            return;
        }

        // Consume the closing "
        this.advance();
        String string = this.source.substring(start + 1, current - 1);
        this.addToken(STRING_LITERAL, new AtomicValue<>(string, AtomicTypes.STRING));
    }

    /**
     * Scan a character literal
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
     * Peek at current character without consuming
     */
    protected char peek()
    {
        if (this.isAtEnd()) return '\0';
        return this.source.charAt(this.current);
    }

    /**
     * Peek at next character
     */
    protected char peekNext()
    {
        if (this.current + 1 >= this.source.length()) return '\0';
        return this.source.charAt(this.current + 1);
    }

    /**
     * Check if we're at the end of the source
     */
    protected boolean isAtEnd()
    {
        return this.current >= this.source.length();
    }

    /**
     * Add a token with a literal value
     */
    protected void addToken(TokenType type, Value literal)
    {
        String text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(type, text, literal, this.line));
    }

    /**
     * Add a token without a literal value
     */
    protected void addToken(TokenType type)
    {
        this.addToken(type, null);
    }

    /**
     * Consume and return current character
     */
    protected char advance()
    {
        return this.source.charAt(this.current++);
    }

    /**
     * Check if next character matches expected, and consume if so
     */
    protected boolean match(char ch)
    {
        if (isAtEnd()) return false;
        if (this.source.charAt(this.current) != ch) return false;

        this.current++;
        return true;
    }

    /**
     * Check if character is a digit
     */
    protected boolean isDigit(char ch)
    {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Check if character is alphabetic or underscore
     */
    protected boolean isAlpha(char ch)
    {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    /**
     * Check if character is alphanumeric
     */
    protected boolean isAlphaNumeric(char ch)
    {
        return isDigit(ch) || isAlpha(ch);
    }
}
