package io.dream.parser;

import io.dream.Main;
import io.dream.ast.Expr;
import io.dream.ast.Stmt;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;

import java.util.ArrayList;
import java.util.List;

import static io.dream.scanner.TokenType.*;

/**
 * The type Parser.
 */
public class Parser
{
    private static class ParseError extends RuntimeException {}

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
     * This is the function that will parse all the code of user of the interpreter.
     *
     * @return the head of the ast.
     * */
    public List<Stmt> parse()
    {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd())
        {
            statements.add(statement());
        }

        return statements;
    }

    private Stmt statement()
    {
        if (match(ECRIRE)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement()
    {
        consume(LEFT_PAREN, "Vous avez oublié la parenthèse ouvrante!");
        Expr value = expression();
        consume(RIGHT_PAREN, "Vous avez oublié la parenthèse fermante!");
        consume(SEMICOLON, "Attente d'un point virgule à la fin!");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement()
    {
        Expr expression = expression();
        consume(SEMICOLON, "Attente d'un point virgule à la fin!");
        return new Stmt.Expression(expression);
    }

    /**
     * This function parse an expression node in the ast.
     *
     * @return An expression node.
     * */
    private Expr expression()
    {
        return this.equality();
    }

    /**
     * This function parse an equality node in the ast.
     *
     * @return An expression node.
     * */
    private Expr equality()
    {
        Expr expr = this.comparison();

        while (this.match(DIFF, EQUAL_EQUAL))
        {
            Token token = this.previous();
            Expr right = this.comparison();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    /**
     * This function parse a comparison node in the ast.
     *
     * @return An expression node.
     * */
    private Expr comparison()
    {
        Expr expr = this.term();

        while (this.match(GREATER, GREATER_OR_EQUAL, LESS,  LESS_OR_EQUAL))
        {
            Token token = this.previous();
            Expr right = this.term();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    /**
     * This function parses the term node in the ast.
     *
     * @return An expression node.*/
    private Expr term()
    {
        Expr expr = this.factor();

        while (this.match(MINUS, PLUS))
        {
            Token token = this.previous();
            Expr right = this.factor();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    /**
     * This function parses a factory node in the ast.
     *
     * @return An expression node.
     * */
    private Expr factor()
    {
        Expr expr = this.unary();

        while (this.match(SLASH, STAR))
        {
            Token token = this.previous();
            Expr right = this.unary();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    /**
     * This function parses a unary node in the ast.
     *
     * @return An expression node.
     * */
    private Expr unary()
    {
        if (this.match(BANG, MINUS))
        {
            Token token = this.previous();
            Expr right = this.unary();
            return new Expr.Unary(token, right);
        }

        return this.primary();
    }

    private Expr primary()
    {
        if (this.match(FALSE)) return new Expr.Literal(new AtomicValue<Boolean>(false, AtomicTypes.BOOLEAN));
        if (this.match(TRUE)) return new Expr.Literal(new AtomicValue<Boolean>(true, AtomicTypes.BOOLEAN));
        if (this.match(NIL)) return new Expr.Literal(new AtomicValue<Void>(null, AtomicTypes.VOID));

        // for numbers and strings.
        if (this.match(STRING_LITERAL, INTEGER_LITERAL, DOUBLE_LITERAL))
        {
            return new Expr.Literal(this.previous().literal());
        }

        if (this.match(LEFT_PAREN))
        {
            Expr expr = this.expression();
            consume(RIGHT_PAREN, "Attend ')' après l'expression.");
            return new Expr.Grouping(expr);
        }

        throw error(this.peek(), "Attends d'une expression.");
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
     * This is function is going to consume a token, check to see if the token correspond to the
     * one passed as parameter, and it is, it will consume that otherwise it will report an error
     * to the user.
     *
     * @param tokenType the token type we want to consume.
     * @param message the message to print to the user if an error occurred.
     * */
    private Token consume(TokenType tokenType, String message)
    {
        if (this.check(tokenType)) return advance();
        throw error(this.peek(), message);
    }

    /**
     * This function throws an exception to synchronize the error recovering.
     *
     * @throws ParseError throws a parseError exception.
     * */
    private ParseError error(Token token, String message)
    {
        Main.error(token, message);
        return new ParseError();
    }

    /**
     * This function is used to synchronize the error recovery of our interpreter in the compiler
     * phase of the user's code.
     * */
    private void synchronize()
    {
        this.advance();

        while (!this.isAtEnd())
        {
            if (this.previous().type() == SEMICOLON) return;

            switch (this.peek().type())
            {
                case CLASS:
                case METHOD:
                case VARIABLE:
                case ALGORITHM:
                case BEGIN:
                case IF:
                case WHILE:
                case FOR:
                    return;
            }

            this.advance();
        }
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
