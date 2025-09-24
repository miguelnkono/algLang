package io.dream.parser;

import io.dream.ast.Expression;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;

import java.util.List;

import static io.dream.scanner.TokenType.*;

/**
 * The type Parser.
 */
public class Parser
{
    // the list of all the tokens
    private final List<Token> tokens;
    private int current = 0;

    /**
     * Instantiates a new Parser.
     *
     * @param tokens the tokens
     */
    public Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    /**
     * This function parse an expression node in the ast.
     *
     * @return An expression node.
     * */
    private Expression expression()
    {
        return this.equality();
    }

    /**
     * This function parse an equality node in the ast.
     *
     * @return An expression node.
     * */
    private Expression equality()
    {
        Expression expression = this.comparison();

        while (this.match(DIFF, EQUAL_EQUAL))
        {
            Token token = this.previous();
            Expression right = this.comparison();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * This function parse a comparison node in the ast.
     *
     * @return An expression node.
     * */
    private Expression comparison()
    {
        Expression expression = this.term();

        while (this.match(GREATER, GREATER_OR_EQUAL, LESS,  LESS_OR_EQUAL))
        {
            Token token = this.previous();
            Expression right = this.term();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * This function parses the term node in the ast.
     *
     * @return An expression node.*/
    private Expression term()
    {
        Expression expression = this.factor();

        while (this.match(MINUS, PLUS))
        {
            Token token = this.previous();
            Expression right = this.factor();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * This function parses a factory node in the ast.
     *
     * @return An expression node.
     * */
    private Expression factor()
    {
        Expression expression = this.unary();

        while (this.match(SLASH, STAR))
        {
            Token token = this.previous();
            Expression right = this.unary();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * This function parses a unary node in the ast.
     *
     * @return An expression node.
     * */
    private Expression unary()
    {
        if (this.match(BANG, MINUS))
        {
            Token token = this.previous();
            Expression right = this.unary();
            return new Expression.Unary(token, right);
        }

        return this.primary();
    }

    private Expression primary()
    {
        if (this.match(FALSE)) return new Expression.Literal(false);
        if (this.match(TRUE)) return new Expression.Literal(true);
        if (this.match(NIL)) return new Expression.Literal(null);

        // for numbers and strings.
        if (this.match(STRING, INTEGER, DOUBLE))
        {
            return new Expression.Literal(this.previous().type());
        }

        if (this.match(LEFT_PAREN))
        {
            Expression expression = this.expression();
            consume(RIGHT_PAREN, "Attend ')' après l'expression.");
            return new Expression.Grouping(expression);
        }
    }

    /**
     * This helper function is used to check if the current token matches the provided one.
     *
     * @param types the different type we are going to check on.
     * @return a boolean value that represent if the token match.
     * */
    private boolean match(TokenType ...types)
    {
        for (TokenType type : types )
        {
            if (this.check(type))
            {
                // we consume the current token and return true.
                this.advance();
                return true;
            }
        }

        return false;
    }

    /**
     * This function will check to see if the provided token matches the current token.
     *
     * @param type represent the token for which we want to check the type with.
     * @return a boolean value representing the result of the checking.
     * */
    private boolean check(TokenType type)
    {
        if (this.isAtEnd()) return false;
        return this.peek().type() == type;
    }

    /**
     * This function consume the current token and return it.
     *
     * @return the token that just get consumed.
     * */
    private Token advance()
    {
        if (!this.isAtEnd()) this.current++;
        return this.previous();
    }

    /**
     * This helper function will tell us if we are at the end of the tokens list.
     *
     * @return a boolean value indication if we are at the end of the tokens list.
     * */
    private boolean isAtEnd()
    {
        return this.peek().type() == EOF;
    }

    /**
     * This function will return the current token being processed.
     *
     * @return the current token in the list of all the tokens.
     * */
    private Token peek()
    {
        return this.tokens.get(this.current);
    }

    /**
     * This function will return the previous token in the list of all the tokens.
     *
     * @return the previous token being consumed by the parser.
     * */
    private Token previous()
    {
        return this.tokens.get(this.current - 1);
    }
}
