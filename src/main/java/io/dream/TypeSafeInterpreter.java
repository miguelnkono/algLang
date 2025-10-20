package io.dream;

import io.dream.ast.Expression;
import io.dream.error.RuntimeError;
import io.dream.types.AtomicValue;
import io.dream.types.Checker;
import io.dream.types.Type;
import io.dream.types.TypeFactory;

public class TypeSafeInterpreter implements Expression.Visitor<Object>
{
    private final Checker typeChecker;

    public TypeSafeInterpreter()
    {
        this.typeChecker = new Checker();
    }

    public void interpret(Expression expression)
    {
        try
        {
            // First, type check the expression
            Expression typedExpr = typeChecker.check(expression);

            // Then evaluate it
            Object result = this.evaluate(typedExpr);
            System.out.println(this.stringify(result));
        } catch (RuntimeError re)
        {
            Main.runtimeError(re);
        }
    }

    private Object evaluate(Expression expression)
    {
        return expression.accept(this);
    }

    private String stringify(Object value)
    {
        if (value == null) return "nil";

        if (value instanceof Double)
        {
            String text = value.toString();
            if (text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression)
    {
        // Use the type information for safer evaluation
        Type exprType = expression.getType();
        Object left = this.evaluate(expression.left);
        Object right = this.evaluate(expression.right);

        // Since we've already type-checked, we can be more confident about types
        switch (expression.operator.type())
        {
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
                // Type checker already ensured these are numbers
                double leftVal = toDouble(left);
                double rightVal = toDouble(right);
                return getComparison(expression, leftVal, rightVal);

            case EQUAL_EQUAL:
                return this.isEqual(left, right);
            case DIFF:
                return !this.isEqual(left, right);

            case MINUS:
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return toInt(left) - toInt(right);
                } else
                {
                    return toDouble(left) - toDouble(right);
                }

            case SLASH:
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return toInt(left) / toInt(right);
                } else
                {
                    return toDouble(left) / toDouble(right);
                }

            case STAR:
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return toInt(left) * toInt(right);
                } else
                {
                    return toDouble(left) * toDouble(right);
                }

            case PLUS:
                if (exprType.equals(TypeFactory.STRING))
                {
                    return left.toString() + right.toString();
                } else if (exprType.equals(TypeFactory.INTEGER))
                {
                    return toInt(left) + toInt(right);
                } else
                {
                    return toDouble(left) + toDouble(right);
                }
        }

        return null;
    }

    private Object getComparison(Expression.Binary expression, double leftVal, double rightVal)
    {
        return switch (expression.operator.type())
        {
            case GREATER -> leftVal > rightVal;
            case GREATER_OR_EQUAL -> leftVal >= rightVal;
            case LESS -> leftVal < rightVal;
            case LESS_OR_EQUAL -> leftVal <= rightVal;
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression)
    {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression)
    {
        Object right = this.evaluate(expression.right);
        Type exprType = expression.getType();

        switch (expression.operator.type())
        {
            case MINUS:
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return -toInt(right);
                } else
                {
                    return -toDouble(right);
                }
            case BANG:
                return !this.isTruth(right);
        }

        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression)
    {
        // Extract the actual value from AtomicValue
        if (expression.value instanceof AtomicValue)
        {
            AtomicValue<?> atomicValue = (AtomicValue<?>) expression.value;
            return atomicValue.getValue();
        }
        return expression.value;
    }

    private boolean isTruth(Object value)
    {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        return true;
    }

    private boolean isEqual(Object left, Object right)
    {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.equals(right);
    }

    // Helper methods for type conversion
    private int toInt(Object value)
    {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Double) return ((Double) value).intValue();
        throw new RuntimeError(null, "Expected integer value");
    }

    private double toDouble(Object value)
    {
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        throw new RuntimeError(null, "Expected numeric value");
    }
}