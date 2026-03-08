package io.dream.parser;

import static io.dream.scanner.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.dream.Main;
import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.config.Messages;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.*;

/**
 * Complete Parser for AlgoLang
 * Supports all language features: loops, functions, methods, arrays, structures, etc.
 */
public class Parser
{
    private static class ParseError extends RuntimeException {}

    // Token list and current position
    private final List<Token> tokens;
    private int current = 0;

    // Symbol tables for different scopes
    private final Map<String, Type> globalSymbolTable = new HashMap<>();
    private final Map<String, FunctionType> functionTable = new HashMap<>();
    private final Map<String, List<Statement.Parameter>> methodTable = new HashMap<>();
    private final Map<String, StructType> structTable = new HashMap<>();
    private final Map<String, Value> constantTable = new HashMap<>();

    // Current scope for variables (for nested scopes in functions)
    private Map<String, Type> currentScope;

    /**
     * Instantiates a new Parser.
     */
    public Parser(List<Token> tokens)
    {
        this.tokens = tokens;
        this.currentScope = globalSymbolTable;
    }

    /**
     * Parse the complete program
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
     * program -> algorithm_header type_section? constant_section?
     *            function_section* method_section* var_section? block
     */
    private List<Statement> program()
    {
        List<Statement> allStatements = new ArrayList<>();

        // Parse algorithm header
        algorithmHeader();

        // Parse optional type section (structures)
        if (match(TYPE))
        {
            consume(COLON, Messages.expectColon("Type"));
            while (!check(CONSTANT) && !check(FUNCTION) && !check(METHOD) &&
                    !check(VARIABLE) && !check(BEGIN) && !isAtEnd())
            {
                allStatements.add(structDeclaration());
            }
        }

        // Parse optional constant section
        if (match(CONSTANT))
        {
            consume(COLON, Messages.expectColon("Constant"));
            while (!check(FUNCTION) && !check(METHOD) && !check(VARIABLE) &&
                    !check(BEGIN) && !isAtEnd())
            {
                allStatements.add(constantDeclaration());
            }
        }

        // Parse functions
        while (match(FUNCTION))
        {
            allStatements.add(functionDeclaration());
        }

        // Parse methods
        while (match(METHOD))
        {
            allStatements.add(methodDeclaration());
        }

        // Parse optional variable section
        if (match(VARIABLE))
        {
            varSection();
        }

        // Parse main block
        List<Statement> blockStatements = block();
        allStatements.addAll(blockStatements);

        return allStatements;
    }

    /**
     * algorithm_header -> ("Algorithme" | "Algorithm") ":" IDENTIFIER ";"
     */
    private void algorithmHeader()
    {
        consume(ALGORITHM, Messages.expectAlgorithmKeyword());
        consume(COLON, Messages.expectColon("Algorithm"));
        consume(IDENTIFIER, Messages.expectAlgorithmName());
        consume(SEMICOLON, Messages.expectSemicolon("algorithm name"));
    }

    /**
     * Structure Personne
     *     nom : chaine;
     *     age : entier;
     * FinStruct
     */
    private Statement structDeclaration()
    {
        consume(STRUCTURE, Messages.expectAfter("Structure", "Type:"));
        Token name = consume(IDENTIFIER, Messages.expectStructName());

        // Check for duplicate structure declaration
        if (structTable.containsKey(name.lexeme()))
        {
            throw error(name, Messages.structureAlreadyDeclared(name.lexeme()));
        }

        List<Statement.Field> fields = new ArrayList<>();
        Map<String, Type> fieldTypes = new LinkedHashMap<>();

        // Parse fields
        while (!check(END_STRUCT) && !isAtEnd())
        {
            Token fieldName = consume(IDENTIFIER, Messages.expectFieldName());
            consume(COLON, Messages.expectAfter(":", "field name"));
            Type fieldType = parseType();
            consume(SEMICOLON, Messages.expectSemicolon("field declaration"));

            fields.add(new Statement.Field(fieldName, fieldType));
            fieldTypes.put(fieldName.lexeme(), fieldType);
        }

        consume(END_STRUCT, Messages.expectEndStructBlock());

        // Create and register the structure type
        StructType structType = new StructType(name.lexeme(), fieldTypes);
        structTable.put(name.lexeme(), structType);

        return new Statement.StructDeclaration(name, fields);
    }

    /**
     * PI = 3.14;
     */
    private Statement constantDeclaration()
    {
        Token name = consume(IDENTIFIER, Messages.expectVariableName());
        consume(EQUAL, Messages.expectAfter("=", "constant name"));
        Expression value = expression();
        consume(SEMICOLON, Messages.expectSemicolon("constant declaration"));

        // Store constant in constant table
        // Type will be inferred from the expression
        return new Statement.ConstantDeclaration(name, value, null);
    }

    /**
     * Fonction: carre(x: entier): entier;
     * Variables:
     *     ...
     * Debut:
     *     ...
     *     retourne x * x;
     * Fin
     * FinFonction;
     */
    private Statement functionDeclaration()
    {
        consume(COLON, Messages.expectColon("Function"));
        Token name = consume(IDENTIFIER, Messages.expectFunctionName());

        // Check for duplicate function
        if (functionTable.containsKey(name.lexeme()))
        {
            throw error(name, Messages.functionAlreadyDeclared(name.lexeme()));
        }

        // Parse parameters
        consume(LEFT_PAREN, Messages.expectLeftParen("function name"));
        List<Statement.Parameter> parameters = new ArrayList<>();
        List<Type> paramTypes = new ArrayList<>();

        if (!check(RIGHT_PAREN))
        {
            do {
                Token paramName = consume(IDENTIFIER, Messages.expectParameterName());
                consume(COLON, Messages.expectAfter(":", "parameter name"));
                Type paramType = parseType();

                parameters.add(new Statement.Parameter(paramName, paramType));
                paramTypes.add(paramType);
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, Messages.expectRightParen("parameters"));
        consume(COLON, Messages.expectColon("parameter list"));

        // Parse return type
        Type returnType = parseType();
        consume(SEMICOLON, Messages.expectSemicolon("return type"));

        // Register function type
        FunctionType funcType = new FunctionType(name.lexeme(), paramTypes, returnType);
        functionTable.put(name.lexeme(), funcType);

        // Create new scope for function
        Map<String, Type> previousScope = currentScope;
        currentScope = new HashMap<>();

        // Add parameters to function scope
        for (Statement.Parameter param : parameters)
        {
            currentScope.put(param.name.lexeme(), param.type);
        }

        // Parse optional local variables
        if (match(VARIABLE))
        {
            varSection();
        }

        // Parse function body
        List<Statement> body = block();

        consume(END_FUNCTION, Messages.expectEndFunctionBlock());
        consume(SEMICOLON, Messages.expectSemicolon("function declaration"));

        // Capture local variables (excluding parameters)
        Map<String, Type> localVars = new HashMap<>(currentScope);
        for (Statement.Parameter param : parameters)
        {
            localVars.remove(param.name.lexeme());
        }

        // Restore previous scope
        currentScope = previousScope;

        return new Statement.FunctionDeclaration(name, parameters, returnType, body, localVars);
    }

    /**
     * Methode: afficher(x: entier):
     * Debut:
     *     ecrire(x);
     * Fin
     * FinMethode;
     */
    private Statement methodDeclaration()
    {
        consume(COLON, Messages.expectColon("Method"));
        Token name = consume(IDENTIFIER, Messages.expectMethodName());

        // Check for duplicate method
        if (methodTable.containsKey(name.lexeme()))
        {
            throw error(name, Messages.functionAlreadyDeclared(name.lexeme()));
        }

        // Parse parameters
        consume(LEFT_PAREN, Messages.expectLeftParen("method name"));
        List<Statement.Parameter> parameters = new ArrayList<>();

        if (!check(RIGHT_PAREN))
        {
            do {
                Token paramName = consume(IDENTIFIER, Messages.expectParameterName());
                consume(COLON, Messages.expectAfter(":", "parameter name"));
                Type paramType = parseType();

                parameters.add(new Statement.Parameter(paramName, paramType));
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, Messages.expectRightParen("parameters"));
        consume(COLON, Messages.expectColon("parameter list"));

        // Register method
        methodTable.put(name.lexeme(), parameters);

        // Create new scope for method
        Map<String, Type> previousScope = currentScope;
        currentScope = new HashMap<>();

        // Add parameters to method scope
        for (Statement.Parameter param : parameters)
        {
            currentScope.put(param.name.lexeme(), param.type);
        }

        // Parse optional local variables
        if (match(VARIABLE))
        {
            varSection();
        }

        // Parse method body
        List<Statement> body = block();

        consume(END_METHOD, Messages.expectEndMethodBlock());
        consume(SEMICOLON, Messages.expectSemicolon("method declaration"));

        // Capture local variables (excluding parameters)
        Map<String, Type> localVars = new HashMap<>(currentScope);
        for (Statement.Parameter param : parameters)
        {
            localVars.remove(param.name.lexeme());
        }

        // Restore previous scope
        currentScope = previousScope;

        return new Statement.MethodDeclaration(name, parameters, body, localVars);
    }

    /**
     * Variables:
     *     x, y : entier;
     *     nom : chaine;
     */
    private void varSection()
    {
        consume(COLON, Messages.expectColon("Variables"));

        while (!check(BEGIN) && !check(END) && !isAtEnd())
        {
            varDeclaration();
        }
    }

    /**
     * Parse a single variable declaration
     */
    private void varDeclaration()
    {
        // Parse variable names (can be comma-separated)
        List<Token> names = new ArrayList<>();
        names.add(consume(IDENTIFIER, Messages.expectVariableName()));

        while (match(COMMA))
        {
            names.add(consume(IDENTIFIER, Messages.expectVariableName()));
        }

        consume(COLON, Messages.expectAfter(":", "variable name"));
        Type varType = parseType();
        consume(SEMICOLON, Messages.expectSemicolon("variable declaration"));

        // Store all variables in current scope
        for (Token name : names)
        {
            if (currentScope.containsKey(name.lexeme()))
            {
                throw error(name, Messages.variableAlreadyDeclared(name.lexeme()));
            }
            currentScope.put(name.lexeme(), varType);
        }
    }

    /**
     * Parse a type specification
     * type -> primitive_type | array_type | struct_type
     */
    private Type parseType()
    {
        // Check for array type
        if (match(TABLE))
        {
            return parseArrayType();
        }

        // Check for primitive types
        if (match(INTEGER))
        {
            return TypeFactory.INTEGER;
        }
        else if (match(DOUBLE))
        {
            return TypeFactory.FLOATING;
        }
        else if (match(STRING))
        {
            return TypeFactory.STRING;
        }
        else if (match(CHARACTER))
        {
            return TypeFactory.CHAR;
        }
        else if (match(BOOLEAN))
        {
            return TypeFactory.BOOLEAN;
        }
        else if (match(NUMBER))
        {
            return TypeFactory.FLOATING; // Generic number type -> real
        }
        else if (check(IDENTIFIER))
        {
            // Could be a structure type
            Token typeName = advance();
            if (structTable.containsKey(typeName.lexeme()))
            {
                return structTable.get(typeName.lexeme());
            }
            else
            {
                throw error(typeName, Messages.structureNotDefined(typeName.lexeme()));
            }
        }

        throw error(peek(), Messages.expectVariableType());
    }

    /**
     * Parse array type: tableau[1..10] de entier
     */
    private Type parseArrayType()
    {
        consume(LEFT_BRACKET, Messages.expectAfter("[", "tableau"));

        // Parse lower bound
        Token lowerToken = consume(INTEGER_LITERAL, Messages.expectArraySize());
        AtomicValue<?> lowerValue = (AtomicValue<?>) lowerToken.literal();
        int lowerBound = (Integer) lowerValue.getValue();

        consume(DOT_DOT, Messages.expectAfter("..", "lower bound"));

        // Parse upper bound
        Token upperToken = consume(INTEGER_LITERAL, Messages.expectArraySize());
        AtomicValue<?> upperValue = (AtomicValue<?>) upperToken.literal();
        int upperBound = (Integer) upperValue.getValue();

        consume(RIGHT_BRACKET, Messages.expectRightBracket("upper bound"));
        consume(OF, Messages.expectOf());

        // Parse element type
        Type elementType = parseType();

        return new ArrayType(elementType, lowerBound, upperBound);
    }

    /**
     * block -> ("Debut" | "Begin") ":" statement* ("Fin" | "End")
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
     * Parse a single statement
     */
    private Statement statement()
    {
        // Write statement
        if (match(WRITE))
        {
            return writeStatement();
        }

        // Read statement
        if (match(READ))
        {
            return readStatement();
        }

        // Return statement
        if (match(RETURN))
        {
            return returnStatement();
        }

        // If statement
        if (match(IF))
        {
            return ifStatement();
        }

        // While loop
        if (match(WHILE))
        {
            return whileStatement();
        }

        // Do-while loop
        if (match(REPEAT))
        {
            return doWhileStatement();
        }

        // For loop
        if (match(FOR))
        {
            return forStatement();
        }

        // Check if it's an assignment or method call
        if (check(IDENTIFIER))
        {
            // Look ahead to determine if it's assignment, method call, or array/field access
            Token identifier = peek();
            Token next = peekAhead(1);

            if (next != null)
            {
                // Method call: identifier(...)
                if (next.type() == LEFT_PAREN)
                {
                    // Could be function call or method call
                    // Check if it's a method (methods are statements, functions are expressions)
                    if (methodTable.containsKey(identifier.lexeme()))
                    {
                        return methodCallStatement();
                    }
                }

                // Assignment: identifier <- ...
                // Or array assignment: identifier[...] <- ...
                // Or field assignment: identifier.field <- ...
                if (next.type() == ASSIGN || next.type() == LEFT_BRACKET || next.type() == DOT)
                {
                    return assignmentStatement();
                }
            }
        }

        throw error(peek(), Messages.expectStatement());
    }

    /**
     * write_stmt -> ("ecrire" | "write") "(" expression ")" ";"
     */
    private Statement writeStatement()
    {
        consume(LEFT_PAREN, Messages.expectLeftParen("write"));
        Expression expression = expression();
        consume(RIGHT_PAREN, Messages.expectRightParen("expression"));
        consume(SEMICOLON, Messages.expectSemicolon("write statement"));

        return new Statement.Write(expression);
    }

    /**
     * read_stmt -> ("lire" | "read") "(" IDENTIFIER ")" ";"
     */
    private Statement readStatement()
    {
        consume(LEFT_PAREN, Messages.expectLeftParen("read"));
        Token variable = consume(IDENTIFIER, Messages.expectVariableName());
        consume(RIGHT_PAREN, Messages.expectRightParen("variable name"));
        consume(SEMICOLON, Messages.expectSemicolon("read statement"));

        return new Statement.Read(variable);
    }

    /**
     * return_stmt -> ("retourne" | "return") expression ";"
     */
    private Statement returnStatement()
    {
        Token keyword = previous();
        Expression value = expression();
        consume(SEMICOLON, Messages.expectSemicolon("return statement"));

        return new Statement.Return(keyword, value);
    }

    /**
     * assignment_stmt -> lvalue "<-" expression ";"
     * where lvalue can be:
     *   - identifier
     *   - identifier[index]
     *   - identifier.field
     *   - identifier.field[index]  (NESTED)
     */
    private Statement assignmentStatement()
    {
        Token name = consume(IDENTIFIER, Messages.expectVariableName());

        // Check for field access first: obj.field
        if (check(DOT))
        {
            consume(DOT, null);
            Token field = consume(IDENTIFIER, Messages.expectFieldName());

            // Check for nested array access: obj.field[index]
            if (check(LEFT_BRACKET))
            {
                consume(LEFT_BRACKET, null);
                Expression index = expression();
                consume(RIGHT_BRACKET, Messages.expectRightBracket("index"));
                consume(ASSIGN, Messages.expectAssignOperator());
                Expression value = expression();
                consume(SEMICOLON, Messages.expectSemicolon("assignment"));

                // This is a nested field+array assignment
                // We need a new statement type for this
                return new Statement.NestedFieldArrayAssignment(name, field, index, value);
            }
            else
            {
                // Simple field assignment: obj.field <- value
                consume(ASSIGN, Messages.expectAssignOperator());
                Expression value = expression();
                consume(SEMICOLON, Messages.expectSemicolon("assignment"));

                return new Statement.FieldAssignment(name, field, value);
            }
        }
        // Check for array assignment: arr[index] <- value
        else if (check(LEFT_BRACKET))
        {
            consume(LEFT_BRACKET, null);
            Expression index = expression();
            consume(RIGHT_BRACKET, Messages.expectRightBracket("index"));
            consume(ASSIGN, Messages.expectAssignOperator());
            Expression value = expression();
            consume(SEMICOLON, Messages.expectSemicolon("assignment"));

            return new Statement.ArrayAssignment(name, index, value);
        }
        // Simple variable assignment: x <- value
        else
        {
            consume(ASSIGN, Messages.expectAssignOperator());
            Expression value = expression();
            consume(SEMICOLON, Messages.expectSemicolon("assignment"));

            return new Statement.Assignment(name, value);
        }
    }

    /**
     * method_call_stmt -> IDENTIFIER "(" arguments? ")" ";"
     */
    private Statement methodCallStatement()
    {
        Token name = consume(IDENTIFIER, Messages.expectMethodName());
        consume(LEFT_PAREN, Messages.expectLeftParen("method name"));

        List<Expression> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN))
        {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, Messages.expectRightParen("arguments"));
        consume(SEMICOLON, Messages.expectSemicolon("method call"));

        return new Statement.MethodCall(name, arguments);
    }

    // ========================================================================
    // CONTROL FLOW STATEMENTS
    // ========================================================================

    /**
     * if_stmt -> ("si" | "if") expression ("alors" | "then") ":"
     *            statement*
     *            ("sinon si" | "else if" expression ("alors" | "then") ":" statement*)*
     *            ("sinon" | "else" ":" statement*)?
     *            ("finsi" | "endif")
     */
    private Statement ifStatement()
    {
        return parseIfStatement();
    }

    private Statement parseIfStatement()
    {
        Expression condition = expression();
        consume(THEN, Messages.expectThen("condition"));
        consume(COLON, Messages.expectColon("then"));

        List<Statement> thenBranch = new ArrayList<>();

        // Parse then branch
        while (!check(ENDIF) && !check(ELSEIF) && !check(ELSE) && !isAtEnd())
        {
            thenBranch.add(statement());
        }

        List<Statement> elseBranch = new ArrayList<>();

        // Handle else-if: recursively parse as nested if
        if (match(ELSEIF))
        {
            // Recursively build the else-if chain
            Statement nestedIf = parseElseIfChain();
            elseBranch.add(nestedIf);
        }
        // Handle else clause
        else if (match(ELSE))
        {
            consume(COLON, Messages.expectColon("else"));

            while (!check(ENDIF) && !isAtEnd())
            {
                elseBranch.add(statement());
            }
        }

        consume(ENDIF, Messages.expectEndIfBlock());

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement parseElseIfChain()
    {
        // This is called after ELSEIF has been matched
        Expression condition = expression();
        consume(THEN, Messages.expectThen("else if condition"));
        consume(COLON, Messages.expectColon("then"));

        List<Statement> thenBranch = new ArrayList<>();

        // Parse this else-if's body
        while (!check(ENDIF) && !check(ELSEIF) && !check(ELSE) && !isAtEnd())
        {
            thenBranch.add(statement());
        }

        List<Statement> elseBranch = new ArrayList<>();

        // Check for another else-if
        if (match(ELSEIF))
        {
            Statement nestedIf = parseElseIfChain();
            elseBranch.add(nestedIf);
        }
        // Or final else
        else if (match(ELSE))
        {
            consume(COLON, Messages.expectColon("else"));

            while (!check(ENDIF) && !isAtEnd())
            {
                elseBranch.add(statement());
            }
        }

        // NOTE: Don't consume ENDIF here - it belongs to the outer if statement

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    /**
     * while_stmt -> ("tant_que" | "while") "(" expression ")" ("faire" | "do") ":"
     *               statement*
     *               ("fintantque" | "endwhile")
     */
    private Statement whileStatement()
    {
        consume(LEFT_PAREN, Messages.expectLeftParen("while"));
        Expression condition = expression();
        consume(RIGHT_PAREN, Messages.expectRightParen("condition"));
        consume(DO, Messages.expectDo());
        consume(COLON, Messages.expectColon("do"));

        List<Statement> body = new ArrayList<>();
        while (!check(ENDWHILE) && !isAtEnd())
        {
            body.add(statement());
        }

        if (isAtEnd())
        {
            throw error(peek(), Messages.whileError());
        }

        consume(ENDWHILE, Messages.expectEndWhileBlock());

        return new Statement.While(condition, body);
    }

    /**
     * do_while_stmt -> ("repeter" | "repeat") ":"
     *                  statement*
     *                  ("jusqu_a" | "until") "(" expression ")" ";"
     */
    private Statement doWhileStatement()
    {
        consume(COLON, Messages.expectColon("repeat"));

        List<Statement> body = new ArrayList<>();
        while (!check(UNTIL) && !isAtEnd())
        {
            body.add(statement());
        }

        consume(UNTIL, Messages.expectUntil());
        consume(LEFT_PAREN, Messages.expectLeftParen("until"));
        Expression condition = expression();
        consume(RIGHT_PAREN, Messages.expectRightParen("condition"));
        consume(SEMICOLON, Messages.expectSemicolon("do-while statement"));

        return new Statement.DoWhile(body, condition);
    }

    /**
     * for_stmt -> ("pour" | "for") IDENTIFIER "<-" expression
     *             ("jusqu_a" | "unti") expression
     *             (("pas" | "step") expression)?
     *             ("faire" | "do") ":"
     *             statement*
     *             ("finpour" | "endfor")
     */
    private Statement forStatement()
    {
        Token variable = consume(IDENTIFIER, Messages.expectVariableName());
        consume(ASSIGN, Messages.expectAssignOperator());
        Expression start = expression();
        consume(UNTIL, Messages.expectTo());
        Expression end = expression();

        Expression step = null;
        if (match(STEP))
        {
            step = expression();
        }

        consume(DO, Messages.expectDo());
        consume(COLON, Messages.expectColon("do"));

        List<Statement> body = new ArrayList<>();
        while (!check(ENDFOR) && !isAtEnd())
        {
            body.add(statement());
        }

        if (isAtEnd())
        {
            throw error(peek(), Messages.forError());
        }

        consume(ENDFOR, Messages.expectEndForBlock());

        return new Statement.For(variable, start, end, step, body);
    }

    /**
     * expression -> logical_or
     */
    private Expression expression()
    {
        return logicalOr();
    }

    /**
     * logical_or -> logical_and (("ou" | "or") logical_and)*
     */
    private Expression logicalOr()
    {
        Expression expression = logicalAnd();

        while (match(OR))
        {
            Token operator = previous();
            Expression right = logicalAnd();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    /**
     * logical_and -> equality (("et" | "and") equality)*
     */
    private Expression logicalAnd()
    {
        Expression expression = equality();

        while (match(AND))
        {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    /**
     * equality -> comparison (("!=" | "==") comparison)*
     */
    private Expression equality()
    {
        Expression expression = comparison();

        while (match(DIFF, EQUAL_EQUAL))
        {
            Token token = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * comparison -> term ((">" | ">=" | "<" | "<=") term)*
     */
    private Expression comparison()
    {
        Expression expression = term();

        while (match(GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL))
        {
            Token token = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * term -> factor (("-" | "+") factor)*
     */
    private Expression term()
    {
        Expression expression = factor();

        while (match(MINUS, PLUS))
        {
            Token token = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * factor -> unary (("/" | "*" | "mod") unary)*
     */
    private Expression factor()
    {
        Expression expression = unary();

        while (match(SLASH, STAR, MOD))
        {
            Token token = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, token, right);
        }

        return expression;
    }

    /**
     * unary -> ("!" | "-" | "non" | "not") unary | call
     */
    private Expression unary()
    {
        if (match(BANG, MINUS, NOT))
        {
            Token token = previous();
            Expression right = unary();
            return new Expression.Unary(token, right);
        }

        return call();
    }

    /**
     * call -> primary ( "(" arguments? ")" | "[" expression "]" | "." IDENTIFIER )*
     */
    private Expression call()
    {
        Expression expr = primary();

        while (true)
        {
            if (match(LEFT_PAREN))
            {
                // Function call
                List<Expression> arguments = new ArrayList<>();
                if (!check(RIGHT_PAREN))
                {
                    do {
                        arguments.add(expression());
                    } while (match(COMMA));
                }
                consume(RIGHT_PAREN, Messages.expectRightParen("arguments"));

                // expr should be a variable with the function name
                if (expr instanceof Expression.Variable)
                {
                    Token funcName = ((Expression.Variable) expr).name;
                    expr = new Expression.Call(funcName, arguments);
                }
                else
                {
                    throw error(previous(), "Can only call functions and methods");
                }
            }
            else if (match(LEFT_BRACKET))
            {
                // Array access
                Expression index = expression();
                consume(RIGHT_BRACKET, Messages.expectRightBracket("index"));
                expr = new Expression.ArrayAccess(expr, index);
            }
            else if (match(DOT))
            {
                // Field access
                Token field = consume(IDENTIFIER, Messages.expectFieldName());
                expr = new Expression.FieldAccess(expr, field);
            }
            else
            {
                break;
            }
        }

        return expr;
    }

    /**
     * primary -> NUMBER | STRING | CHAR | "true" | "false" | "nil"
     *          | IDENTIFIER | "(" expression ")"
     */
    private Expression primary()
    {
        // Boolean literals
        if (match(FALSE))
        {
            return new Expression.Literal(new AtomicValue<Boolean>(false, AtomicTypes.BOOLEAN));
        }

        if (match(TRUE))
        {
            return new Expression.Literal(new AtomicValue<Boolean>(true, AtomicTypes.BOOLEAN));
        }

        if (match(NIL))
        {
            return new Expression.Literal(new AtomicValue<Void>(null, AtomicTypes.VOID));
        }

        // Numeric, string, and character literals
        if (match(STRING_LITERAL, INTEGER_LITERAL, DOUBLE_LITERAL, CHARACTER_LITERAL))
        {
            return new Expression.Literal(previous().literal());
        }

        // Variable or identifier
        if (match(IDENTIFIER))
        {
            return new Expression.Variable(previous());
        }

        // Grouped expression
        if (match(LEFT_PAREN))
        {
            Expression expression = expression();
            consume(RIGHT_PAREN, Messages.expectRightParen("expression"));
            return new Expression.Grouping(expression);
        }

        throw error(peek(), Messages.expectExpression());
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if current token matches any of the given types
     */
    private boolean match(TokenType... types)
    {
        for (TokenType type : types)
        {
            if (check(type))
            {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Consume a token of the expected type or throw error
     */
    private Token consume(TokenType tokenType, String message)
    {
        if (check(tokenType)) return advance();
        throw error(peek(), message);
    }

    /**
     * Report an error and return ParseError exception
     */
    private ParseError error(Token token, String message)
    {
        Main.error(token, message);
        return new ParseError();
    }

    /**
     * Synchronize after an error
     */
    private void synchronize()
    {
        advance();

        while (!isAtEnd())
        {
            if (previous().type() == SEMICOLON) return;

            switch (peek().type())
            {
                case ALGORITHM:
                case BEGIN:
                case END:
                case IF:
                case FOR:
                case WHILE:
                case FUNCTION:
                case METHOD:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    /**
     * Check if current token is of given type
     */
    private boolean check(TokenType type)
    {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    /**
     * Consume current token and return it
     */
    private Token advance()
    {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Check if we're at end of tokens
     */
    private boolean isAtEnd()
    {
        return peek().type() == EOF;
    }

    /**
     * Get current token without consuming
     */
    private Token peek()
    {
        return tokens.get(current);
    }

    /**
     * Get previous token
     */
    private Token previous()
    {
        return tokens.get(current - 1);
    }

    /**
     * Peek ahead n tokens without consuming
     */
    private Token peekAhead(int n)
    {
        int index = current + n;
        if (index >= tokens.size()) return null;
        return tokens.get(index);
    }

    /**
     * Get the global symbol table
     */
    public Map<String, Type> getSymbolTable()
    {
        return globalSymbolTable;
    }

    /**
     * Get the function table
     */
    public Map<String, FunctionType> getFunctionTable()
    {
        return functionTable;
    }

    /**
     * Get the method table
     */
    public Map<String, List<Statement.Parameter>> getMethodTable()
    {
        return methodTable;
    }

    /**
     * Get the structure table
     */
    public Map<String, StructType> getStructTable()
    {
        return structTable;
    }

    /**
     * Get the constant table
     */
    public Map<String, Value> getConstantTable()
    {
        return constantTable;
    }
}
