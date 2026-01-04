package io.dream.types;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.error.TypeException;
import io.dream.scanner.TokenType;

import java.util.List;

public class Checker implements Expression.Visitor<Type>, Statement.Visitor<Void>
{
    public Checker()
    { }

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
            throw new TypeException("Échec de la vérification des types: " + e.getMessage());
        }
    }

    @Override
    public Void visitExpressionStmtStatement(Statement.ExpressionStmt statement)
    {
        // Type check the expression inside the statement
        Type exprType = statement.expression.accept(this);
        statement.expression.setType(exprType);
        statement.setType(exprType); // Optional: set type on statement too
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
                } else
                {
                    String operatorName = getOperatorName(expression.operator.type());
                    throw new TypeException(
                            String.format("Opérateur '%s' incompatible avec les types %s et %s. " +
                                            "Les opérandes doivent être deux nombres ou deux chaînes de caractères.",
                                    operatorName, leftType, rightType),
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
                            String.format("Opérateur de comparaison '%s' incompatible avec les types %s et %s. " +
                                            "Les opérandes doivent être des nombres.",
                                    operatorName, leftType, rightType),
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
                            String.format("Opérateur d'égalité '%s' incompatible avec les types %s et %s. " +
                                            "Les opérandes doivent être du même type.",
                                    operatorName, leftType, rightType),
                            expression.operator
                    );
                }
                break;

            default:
                throw new TypeException(
                        String.format("Opérateur binaire non supporté: %s", expression.operator.type()),
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
                            String.format("Opérateur unaire '-' incompatible avec le type %s. " +
                                            "L'opérande doit être un nombre (entier ou réel).",
                                    rightType),
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
                            String.format("Opérateur logique '!' incompatible avec le type %s. " +
                                            "L'opérande doit être un booléen.",
                                    rightType),
                            expression.operator
                    );
                }
                break;

            default:
                throw new TypeException(
                        String.format("Opérateur unaire non supporté: %s", expression.operator.type()),
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
        throw new TypeException(
                "Valeur littérale de type non supporté. " +
                        "Seules les valeurs atomiques (entier, réel, chaîne, caractère, booléen) sont autorisées."
        );
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
            throw new TypeException("L'expression n'a pas été vérifiée par le vérificateur de types.");
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
                    String.format("Type attendu: %s, mais obtenu: %s", expectedType, actualType)
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
                String.format("Type %s non autorisé. Types attendus: %s",
                        actualType, java.util.Arrays.toString(expectedTypes))
        );
    }
}