package io.dream.scanner;

import io.dream.Main;
import io.dream.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // set of reserved word by the language.
    private static final Map<String, TokenType> keywords;
    static
    {
        keywords = new HashMap<>();
        keywords.put("Algorithme", ALGORITHM);
        keywords.put("Variables", VARIABLE);
        keywords.put("Debut", BEGIN);
        keywords.put("Fin", END);
        keywords.put("Methode", METHOD);
        keywords.put("Classe", CLASS);
        keywords.put("si", IF);
        keywords.put("sinon", ELSE);
        keywords.put("pour", FOR);
        keywords.put("tant-que", WHILE);
        keywords.put("repeter", DO_WHILE);
        keywords.put("vrai", TRUE);
        keywords.put("faux", FALSE);
        keywords.put("nil", NIL);
        keywords.put("tableau", TABLE);
        keywords.put("entier", INTEGER);
        keywords.put("reel", DOUBLE);
        keywords.put("chaine_character", STRING);   // c : chaine_charactere;
    }

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
//            case '<': addToken(match('=') ?  LESS_OR_EQUAL : LESS); break;
            case '<':
                if (match('='))
                {
                    addToken(LESS_OR_EQUAL);
                }
                else if (match('-'))
                {
                    addToken(ASSIGN);
                }
                else
                {
                    addToken(LESS);
                }
                break;
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
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;

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
            case '"': this.string(); break;

            default:
                if (this.isDigit(c))
                {
                    this.number();
                } else if (this.isAlpha(c))
                {
                    this.identifier();
                } else {
                    Main.error(line, "Unsupported character.");
                    break;
                }
        }
    }

    /**
     * this function scan an identifier.
     * */
    private void identifier()
    {
        while (this.isAlphaNumeric(this.peek())) advance();

        String text = this.source.substring(this.start, this.current);
        TokenType relatedTokenTypeToText = keywords.get(text);
        if (relatedTokenTypeToText == null) relatedTokenTypeToText = IDENTIFIER;

        this.addToken(relatedTokenTypeToText);
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
          double doubleValue = Double.parseDouble(this.source.substring(this.start, this.current).replace(",", "."));
          AtomicValue<Double> doubleAtomicValue = new AtomicValue<>(doubleValue, AtomicTypes.FLOATING);
            this.addToken(DOUBLE_LITERAL, doubleAtomicValue);
        }
        else
        {

          int integerValue = Integer.parseInt(this.source.substring(this.start,
              this.current));
          AtomicValue<Integer> integerAtomicValue = new AtomicValue<>(integerValue, AtomicTypes.INTEGER);
          this.addToken(INTEGER_LITERAL, integerAtomicValue);
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
        AtomicValue<String> stringAtomicValue = new AtomicValue<>(string, AtomicTypes.STRING);
        this.addToken(STRING_LITERAL, stringAtomicValue);
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
    private void addToken(TokenType type, Value literal)
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

    /**
     * This helper function check to see if the character is an alpha character or not.
     * An identifier start whether with a lowercase letter or an uppercase letter.
     * @param ch represent the character to check if it is an alpha character.
     * */
    private boolean isAlpha(char ch)
    {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    /**
     * Helper function to check whether a character is a digit or an identifier.
     * @param ch represent the character to check if it is a character.
     * */
    private boolean isAlphaNumeric(char ch)
    {
        return isDigit(ch)  || isAlpha(ch);
    }
}
