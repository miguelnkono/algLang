package io.dream.types;

import io.dream.ast.Expr;
import io.dream.ast.Expr.Variable;
import io.dream.error.TypeException;
import io.dream.scanner.TokenType;

public class Checker implements Expr.Visitor<Type>
{

    public Checker()
    {
    }

    /**
     * Public entry point for type checking an expression
     *
     * @param expr The expression to type check
     * @return The same expression tree with type annotations added to each node
     * @throws TypeException if type checking fails
     */
    public Expr check(Expr expr)
    {
        try
        {
            // This will recursively type check and annotate the entire tree
            Type rootType = expr.accept(this);
            expr.setType(rootType);
            return expr;
        } catch (TypeException e)
        {
            // Re-throw TypeException as is
            throw e;
        } catch (Exception e)
        {
            // Wrap other exceptions in TypeException
            throw new TypeException("Échec de la vérification des types: " + e.getMessage());
        }
    }

    /**
     * Public entry point for type checking with error recovery
     *
     * @param expr    The expression to type check
     * @param returnOnError The expression to return if type checking fails
     * @return The type-annotated expression tree or returnOnError if checking fails
     */
    public Expr check(Expr expr, Expr returnOnError)
    {
        try
        {
            Type rootType = expr.accept(this);
            expr.setType(rootType);
            return expr;
        } catch (Exception e)
        {
            if (returnOnError != null)
            {
                return returnOnError;
            }
            throw new TypeException("Échec de la vérification des types: " + e.getMessage());
        }
    }

    @Override
    public Type visitBinaryExpr(Expr.Binary expression)
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

        // The expression's type will be set by the check() method
        return resultType;
    }

    @Override
    public Type visitGroupingExpr(Expr.Grouping expression)
    {
        // Type check the inner expression
        Type innerType = expression.expression.accept(this);
        expression.expression.setType(innerType);

        // Grouping has the same type as its inner expression
        return innerType;
    }

    @Override
    public Type visitUnaryExpr(Expr.Unary expression)
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

        return resultType;
    }

    @Override
    public Type visitLiteralExpr(Expr.Literal expression)
    {
        // Cast to AtomicValue and get its type
        if (expression.value instanceof AtomicValue)
        {
            AtomicValue<?> atomicValue = (AtomicValue<?>) expression.value;
            return atomicValue.getType();
        }
        throw new TypeException(
                "Valeur littérale de type non supporté. " +
                        "Seules les valeurs atomiques (entier, réel, chaîne, caractère, booléen) sont autorisées."
        );
    }

    /**
     * Utility method to check if an expression is properly typed
     */
    public boolean isTyped(Expr expr)
    {
        return expr.getType() != null;
    }

    /**
     * Utility method to get the type of an expression (convenience method)
     */
    public Type getType(Expr expr)
    {
        if (expr.getType() == null)
        {
            throw new TypeException("L'expression n'a pas été vérifiée par le vérificateur de types.");
        }
        return expr.getType();
    }

    /**
     * Helper method to get French operator names
     */
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

    /**
     * Validates that an expression has a specific expected type
     */
    public void validateType(Expr expr, Type expectedType)
    {
        Type actualType = check(expr).getType();
        if (!actualType.equals(expectedType))
        {
            throw new TypeException(
                    String.format("Type attendu: %s, mais obtenu: %s", expectedType, actualType)
            );
        }
    }

    /**
     * Validates that an expression has one of the expected types
     */
    public void validateType(Expr expr, Type... expectedTypes)
    {
        Type actualType = check(expr).getType();
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

		@Override
		public Type visitVariableExpr(Variable expr)
		{
			// TODO Auto-generated method stub
			return null;
		}
}