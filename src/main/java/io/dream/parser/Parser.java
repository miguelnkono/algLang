package io.dream.parser;

import static io.dream.scanner.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dream.Main;
import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.config.Messages;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;
import io.dream.types.Type;
import io.dream.types.TypeFactory;

/**
 * The type Parser.
 */
public class Parser
{
    private static class ParseError extends RuntimeException {}

    // the list of all the tokens
    private final List<Token> tokens;
    private int current = 0;

    // Symbol table to store variable declarations (name -> type)
    private final Map<String, Type> symbolTable = new HashMap<>();

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
     * @return the list of statements representing the program.
     */
    public List<Statement> parse()
    {
        try
        {
            return this.program();
        }
        catch (ParseError e)
        {
            return new ArrayList<>();
        }
    }

    /**
     * Parses a complete program.
     * program -> algorithm_header block
     */
    private List<Statement> program()
    {
        algorithmHeader();

        // variable declaration section is optional
        if (match(VARIABLE)) {
            var_list();
        }

        return block();
    }

    private void var_list() {
        consume(COLON, Messages.expectColon("Variables"));

        // we can declare multiple variables, so we loop until we find the beginning of the block.
        while (!check(BEGIN) && !isAtEnd()) {
            var_decl();
        }
    }

    private void var_decl() {
        Token name = consume(IDENTIFIER, Messages.expectVariableName());
        consume(COLON, Messages.expectAfter(":", "variable name"));
        Token type = null;
        Type varType = null;

        if (match(INTEGER)) {
            type = previous();
            varType = TypeFactory.INTEGER;
        } else if (match(DOUBLE)) {
            type = previous();
            varType = TypeFactory.FLOATING;
        } else if (match(STRING)) {
            type = previous();
            varType = TypeFactory.STRING;
        } else if (match(CHARACTER)) {
            type = previous();
            varType = TypeFactory.CHAR;
        } else if (match(BOOLEAN)) {
            type = previous();
            varType = TypeFactory.BOOLEAN;
        } else {
            throw error(this.peek(), Messages.expectVariableType());
        }

        consume(SEMICOLON, Messages.expectSemicolon("variable declaration"));

        // Store the variable declaration in the symbol table
        if (symbolTable.containsKey(name.lexeme())) {
            throw error(name, Messages.variableAlreadyDeclared(name.lexeme()));
        }
        symbolTable.put(name.lexeme(), varType);
    }

    /**
     * Parses the algorithm header.
     * algorithm_header -> "Algorithme" ":" IDENTIFIER ";"
     */
    private void algorithmHeader()
    {
        consume(ALGORITHM, Messages.expectAlgorithmKeyword());
        consume(COLON, Messages.expectColon("Algorithm"));
        consume(IDENTIFIER, Messages.expectAlgorithmName());
        consume(SEMICOLON, Messages.expectSemicolon("algorithm name"));
    }

    /**
     * Parses a block of statements.
     * block -> "Debut" ":" statement* "Fin"
     */
    private List<Statement> block()
    {
        consume(BEGIN, Messages.expectBeginBlock());
        consume(COLON, Messages.expectColon("Begin"));

        List<Statement> statements = new ArrayList<>();
        while (!check(END) && !isAtEnd())
        {
            statements.add(statement());
        }

        consume(END, Messages.expectEndBlock());
        return statements;
    }

    /**
     * Parses a single statement.
     * statement -> expression_stmt | write_stmt | assignment_stmt
     */
    private Statement statement()
    {
        if (match(WRITE))
        {
            return writeStatement();
        }

        // Check if it's an assignment (IDENTIFIER followed by ASSIGN)
        if (check(IDENTIFIER))
        {
            // Look ahead to see if this is an assignment
            if (peekAhead(1) != null && peekAhead(1).type() == ASSIGN)
            {
                return assignmentStatement();
            }
        }

        // check if it's an if statement
        if (match(IF)) {
            return ifStatement();
        }

        throw error(this.peek(), Messages.expectStatement());
    }

    private Statement ifStatement() {

        Expression condition = expression();
        consume(THEN, Messages.expectThen("condition"));
        consume(COLON, Messages.expectColon("then"));
        Statement thenBranch = statement();
        Statement elseBranch = null;

        if (match(ELSE)) {
            if (check(IF)) {
                advance();
                elseBranch = ifStatement();
            } else {
                consume(COLON, Messages.expectColon("else"));
                elseBranch = statement();
            }
        }

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement writeStatement()
    {
        consume(LEFT_PAREN, Messages.expectLeftParen("write"));
        Expression expression = expression();
        consume(RIGHT_PAREN, Messages.expectRightParen("expression to write"));
        consume(SEMICOLON, Messages.expectSemicolon("write statement"));

        return new Statement.Write(expression);
    }

    private Statement assignmentStatement()
    {
        Token name = consume(IDENTIFIER, Messages.expectVariableName());
        consume(ASSIGN, Messages.expectAssignOperator());
        Expression value = expression();
        consume(SEMICOLON, Messages.expectSemicolon("assignment"));

        return new Statement.Assignment(name, value);
    }

    /**
     * Parses an expression statement.
     * expression_stmt -> expression ";"
     */
    private Statement expressionStatement()
    {
        Expression expr = expression();
        consume(SEMICOLON, Messages.expectSemicolon("expression"));
        return new Statement.ExpressionStmt(expr);
    }

    /**
     * This function parse an expression node in the ast.
     *
     * @return An expression node.
     */
    private Expression expression()
    {
        return this.equality();
    }

    /**
     * This function parse an equality node in the ast.
     *
     * @return An expression node.
     */
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
     */
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
     * @return An expression node.
     */
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
     */
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
     */
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
        if (this.match(FALSE)) return new Expression.Literal(new AtomicValue<Boolean>(false, AtomicTypes.BOOLEAN));
        if (this.match(TRUE)) return new Expression.Literal(new AtomicValue<Boolean>(true, AtomicTypes.BOOLEAN));
        if (this.match(NIL)) return new Expression.Literal(new AtomicValue<Void>(null, AtomicTypes.VOID));

        // for numbers and strings only (no variables yet)
        if (this.match(STRING_LITERAL, INTEGER_LITERAL, DOUBLE_LITERAL, CHARACTER_LITERAL))
        {
            return new Expression.Literal(this.previous().literal());
        }

        // Handle variable references
        if (this.match(IDENTIFIER))
        {
            return new Expression.Variable(this.previous());
        }

        if (this.match(LEFT_PAREN))
        {
            Expression expression = this.expression();
            consume(RIGHT_PAREN, Messages.expectRightParen("expression"));
            return new Expression.Grouping(expression);
        }

        throw error(this.peek(), Messages.expectExpression());
    }

    /**
     * This helper function is used to check if the current token matches the provided one.
     *
     * @param types the different type we are going to check on.
     * @return a boolean value that represent if the token match.
     */
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
     */
    private Token consume(TokenType tokenType, String message)
    {
        if (this.check(tokenType)) return advance();
        throw error(this.peek(), message);
    }

    /**
     * This function throws an exception to synchronize the error recovering.
     *
     * @throws ParseError throws a parseError exception.
     */
    private ParseError error(Token token, String message)
    {
        Main.error(token, message);
        return new ParseError();
    }

    /**
     * This function is used to synchronize the error recovery of our interpreter in the compiler
     * phase of the user's code.
     */
    private void synchronize()
    {
        this.advance();

        while (!this.isAtEnd())
        {
            if (this.previous().type() == SEMICOLON) return;

            switch (this.peek().type())
            {
                case ALGORITHM:
                case BEGIN:
                case END:
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
     */
    private boolean check(TokenType type)
    {
        if (this.isAtEnd()) return false;
        return this.peek().type() == type;
    }

    /**
     * This function consume the current token and return it.
     *
     * @return the token that just get consumed.
     */
    private Token advance()
    {
        if (!this.isAtEnd()) this.current++;
        return this.previous();
    }

    /**
     * This helper function will tell us if we are at the end of the tokens list.
     *
     * @return a boolean value indication if we are at the end of the tokens list.
     */
    private boolean isAtEnd()
    {
        return this.peek().type() == EOF;
    }

    /**
     * This function will return the current token being processed.
     *
     * @return the current token in the list of all the tokens.
     */
    private Token peek()
    {
        return this.tokens.get(this.current);
    }

    /**
     * This function will return the previous token in the list of all the tokens.
     *
     * @return the previous token being consumed by the parser.
     */
    private Token previous()
    {
        return this.tokens.get(this.current - 1);
    }

    /**
     * This function peeks ahead n tokens without consuming them.
     *
     * @param n the number of tokens to look ahead
     * @return the token n positions ahead, or null if out of bounds
     */
    private Token peekAhead(int n)
    {
        int index = this.current + n;
        if (index >= this.tokens.size()) return null;
        return this.tokens.get(index);
    }

    /**
     * Get the symbol table containing variable declarations.
     *
     * @return the symbol table map
     */
    public Map<String, Type> getSymbolTable()
    {
        return symbolTable;
    }
}
