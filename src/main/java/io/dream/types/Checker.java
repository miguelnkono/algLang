package io.dream.types;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.config.Messages;
import io.dream.error.TypeException;
import io.dream.scanner.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Checker implements Expression.Visitor<Type>, Statement.Visitor<Void>
{
    private final Map<String, Type> symbolTable;

    public Checker()
    {
        this.symbolTable = new HashMap<>();
    }

    public Checker(Map<String, Type> symbolTable)
    {
        this.symbolTable = symbolTable;
    }

    // New: Check a list of statements
    public void check(List<Statement> statements)
    {
        for (Statement statement : statements)
        {
            check(statement);
        }
    }

    // New: Check a single statement
    public Statement check(Statement statement)
    {
        statement.accept(this);
        return statement;
    }

    // Keep the old method for backward compatibility
    public Expression check(Expression expression)
    {
        try
        {
            Type rootType = expression.accept(this);
            expression.setType(rootType);
            return expression;
        } catch (TypeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new TypeException(Messages.typeCheckingFailed(e.getMessage()));
        }
    }

    @Override
    public Void visitExpressionStmtStatement(Statement.ExpressionStmt statement)
    {
        // Type check the expression inside the statement
        Type exprType = statement.expression.accept(this);
        statement.expression.setType(exprType);
        return null;
    }

    @Override
    public Void visitWriteStatement(Statement.Write statement) {
        Type exprType = statement.expression.accept(this);
        statement.expression.setType(exprType);
        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration statement)
    {
        // Type check the initializer expression if present
        if (statement.value != null) {
            Type valueType = statement.value.accept(this);
            statement.value.setType(valueType);

            // Get the declared type from symbol table
            Type declaredType = symbolTable.get(statement.name.lexeme());
            if (declaredType == null) {
                throw new TypeException(
                        Messages.variableNotDeclared(statement.name.lexeme()),
                        statement.name
                );
            }

            // Check type compatibility
            if (!declaredType.equals(valueType)) {
                throw new TypeException(
                        Messages.typeIncompatibility(declaredType.toString(), valueType.toString()),
                        statement.name
                );
            }
        }
        return null;
    }

    @Override
    public Void visitAssignmentStatement(Statement.Assignment statement)
    {
        // Check if variable is declared
        Type varType = symbolTable.get(statement.name.lexeme());
        if (varType == null) {
            throw new TypeException(
                    Messages.variableNotDeclared(statement.name.lexeme()),
                    statement.name
            );
        }

        // Type check the value expression
        Type valueType = statement.value.accept(this);
        statement.value.setType(valueType);

        // Check type compatibility
        if (!varType.equals(valueType)) {
            throw new TypeException(
                    Messages.typeIncompatibility(varType.toString(), valueType.toString()),
                    statement.name
            );
        }

        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        // Type check the condition expression
        Type conditionType = statement.condition.accept(this);
        statement.condition.setType(conditionType);

        // check to see if the condition is a boolean type
        if (!conditionType.equals(TypeFactory.BOOLEAN)) {
            throw new TypeException(
                    Messages.typeIncompatibility(conditionType.toString(), TypeFactory.BOOLEAN.toString())
            );
        }

        if (!statement.thenBranch.isEmpty()) {
            for (Statement stmt : statement.thenBranch)
            {
                check(stmt);
            }
        }

        if (!statement.elseBranch.isEmpty()) {
            for (Statement stmt : statement.elseBranch)
            {
                check(stmt);
            }
        }

        return null;
    }

    // Rest of the Expression.Visitor methods remain exactly the same...
    @Override
    public Type visitBinaryExpression(Expression.Binary expression)
    {
        // Recursively type check left and right subexpressions
        Type leftType = expression.left.accept(this);
        expression.left.setType(leftType);

        Type rightType = expression.right.accept(this);
        expression.right.setType(rightType);

        Type resultType;

        switch (expression.operator.type())
        {
            // Arithmetic operators
            case PLUS, MINUS, STAR, SLASH:
                if (leftType.equals(TypeFactory.INTEGER) && rightType.equals(TypeFactory.INTEGER))
                {
                    resultType = TypeFactory.INTEGER;
                } else if (leftType.equals(TypeFactory.FLOATING) && rightType.equals(TypeFactory.FLOATING))
                {
                    resultType = TypeFactory.FLOATING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.STRING) && rightType.equals(TypeFactory.STRING))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.STRING) && rightType.equals(TypeFactory.FLOATING))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.FLOATING) && rightType.equals(TypeFactory.STRING))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.INTEGER) && rightType.equals(TypeFactory.STRING))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.STRING) && rightType.equals(TypeFactory.INTEGER))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.STRING) && rightType.equals(TypeFactory.CHAR))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.CHAR) && rightType.equals(TypeFactory.STRING))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.STRING) && rightType.equals(TypeFactory.BOOLEAN))
                {
                    resultType = TypeFactory.STRING;
                } else if (expression.operator.type() == TokenType.PLUS &&
                        leftType.equals(TypeFactory.BOOLEAN) && rightType.equals(TypeFactory.STRING))
                {
                    resultType = TypeFactory.STRING;
                } else
                {
                    String operatorName = getOperatorName(expression.operator.type());
                    throw new TypeException(
                            Messages.operatorIncompatibleWithTypes(operatorName, leftType.toString(), rightType.toString(),
                                    Messages.operatorRequirementNumbers()),
                            expression.operator
                    );
                }
                break;

            // Comparison operators
            case GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL:
                if ((leftType.equals(TypeFactory.INTEGER) || leftType.equals(TypeFactory.FLOATING)) &&
                        (rightType.equals(TypeFactory.INTEGER) || rightType.equals(TypeFactory.FLOATING)))
                {
                    resultType = TypeFactory.BOOLEAN;
                } else
                {
                    String operatorName = getOperatorName(expression.operator.type());
                    throw new TypeException(
                            Messages.operatorIncompatibleWithTypes(operatorName, leftType.toString(), rightType.toString(),
                                    Messages.operatorRequirementComparison()),
                            expression.operator
                    );
                }
                break;

            // Equality operators
            case EQUAL_EQUAL, DIFF:
                if (leftType.equals(rightType))
                {
                    resultType = TypeFactory.BOOLEAN;
                } else
                {
                    String operatorName = getOperatorName(expression.operator.type());
                    throw new TypeException(
                            Messages.operatorIncompatibleWithTypes(operatorName, leftType.toString(), rightType.toString(),
                                    Messages.operatorRequirementEquality()),
                            expression.operator
                    );
                }
                break;

            default:
                throw new TypeException(
                        Messages.unsupportedBinaryOperator(expression.operator.type().toString()),
                        expression.operator
                );
        }

        expression.setType(resultType);
        return resultType;
    }

    @Override
    public Type visitGroupingExpression(Expression.Grouping expression)
    {
        // Type check the inner expression
        Type innerType = expression.expression.accept(this);
        expression.expression.setType(innerType);
        expression.setType(innerType);
        return innerType;
    }

    @Override
    public Type visitUnaryExpression(Expression.Unary expression)
    {
        // Type check the right subexpression
        Type rightType = expression.right.accept(this);
        expression.right.setType(rightType);

        Type resultType;

        switch (expression.operator.type())
        {
            case MINUS:
                if (rightType.equals(TypeFactory.INTEGER) || rightType.equals(TypeFactory.FLOATING))
                {
                    resultType = rightType; // Result type is same as operand type for unary minus
                } else
                {
                    throw new TypeException(
                            Messages.unaryOperatorIncompatible("-", rightType.toString(),
                                    Messages.unaryMinusRequirement()),
                            expression.operator
                    );
                }
                break;

            case BANG:
                if (rightType.equals(TypeFactory.BOOLEAN))
                {
                    resultType = TypeFactory.BOOLEAN;
                } else
                {
                    throw new TypeException(
                            Messages.unaryOperatorIncompatible("!", rightType.toString(),
                                    Messages.unaryBangRequirement()),
                            expression.operator
                    );
                }
                break;

            default:
                throw new TypeException(
                        Messages.unsupportedUnaryOperator(expression.operator.type().toString()),
                        expression.operator
                );
        }

        expression.setType(resultType);
        return resultType;
    }

    @Override
    public Type visitLiteralExpression(Expression.Literal expression)
    {
        // Cast to AtomicValue and get its type
        if (expression.value instanceof AtomicValue)
        {
            AtomicValue<?> atomicValue = (AtomicValue<?>) expression.value;
            Type type = atomicValue.getType();
            expression.setType(type);
            return type;
        }
        throw new TypeException(Messages.unsupportedLiteralType());
    }

    @Override
    public Type visitVariableExpression(Expression.Variable expression)
    {
        // Look up the variable type in the symbol table
        Type type = symbolTable.get(expression.name.lexeme());
        if (type == null) {
            throw new TypeException(
                    Messages.variableNotDeclared(expression.name.lexeme()),
                    expression.name
            );
        }
        expression.setType(type);
        return type;
    }

    // Utility methods remain the same...
    public boolean isTyped(Expression expression)
    {
        return expression.getType() != null;
    }

    public Type getType(Expression expression)
    {
        if (expression.getType() == null)
        {
            throw new TypeException(Messages.expressionNotTypeChecked());
        }
        return expression.getType();
    }

    private String getOperatorName(TokenType tokenType)
    {
        return switch (tokenType)
        {
            case PLUS -> "+";
            case MINUS -> "-";
            case STAR -> "*";
            case SLASH -> "/";
            case GREATER -> ">";
            case GREATER_OR_EQUAL -> ">=";
            case LESS -> "<";
            case LESS_OR_EQUAL -> "<=";
            case EQUAL_EQUAL -> "==";
            case DIFF -> "!=";
            case BANG -> "!";
            default -> tokenType.toString().toLowerCase();
        };
    }

    public void validateType(Expression expression, Type expectedType)
    {
        Type actualType = check(expression).getType();
        if (!actualType.equals(expectedType))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot(expectedType.toString(), actualType.toString())
            );
        }
    }

    public void validateType(Expression expression, Type... expectedTypes)
    {
        Type actualType = check(expression).getType();
        for (Type expectedType : expectedTypes)
        {
            if (actualType.equals(expectedType))
            {
                return;
            }
        }
        throw new TypeException(
                Messages.typeNotAllowed(actualType.toString(), java.util.Arrays.toString(expectedTypes))
        );
    }
}
