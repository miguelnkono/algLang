package io.dream;

import io.dream.ast.Expression;
import io.dream.runtime.RuntimeError;
import io.dream.scanner.Token;

public class Interpreter implements Expression.Visitor<Object>
{
    public void interpret(Expression expression)
    {
        try
        {
            Object result = this.evaluate(expression);
            System.out.println(this.stringify(result));
        }
        catch (RuntimeError re)
        {
            Main.runtimeError(re);
        }
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression)
    {
        Object left = this.evaluate(expression.left);
        Object right = this.evaluate(expression.right);

        switch (expression.operator.type())
        {
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
                this.checkNumberOperands(expression.operator, left, right);
                if (left instanceof Double || right instanceof Double) {
                    double leftVal = (left instanceof Double) ? (double) left : (int) left;
                    double rightVal = (right instanceof Double) ? (double) right : (int) right;
                    switch (expression.operator.type()) {
                        case GREATER: return leftVal > rightVal;
                        case GREATER_OR_EQUAL: return leftVal >= rightVal;
                        case LESS: return leftVal < rightVal;
                        case LESS_OR_EQUAL: return leftVal <= rightVal;
                    }
                } else {
                    int leftVal = (int) left;
                    int rightVal = (int) right;
                    switch (expression.operator.type()) {
                        case GREATER: return leftVal > rightVal;
                        case GREATER_OR_EQUAL: return leftVal >= rightVal;
                        case LESS: return leftVal < rightVal;
                        case LESS_OR_EQUAL: return leftVal <= rightVal;
                    }
                }

            case EQUAL_EQUAL:
                return this.isEqual(left, right);
            case DIFF:
                return !this.isEqual(left, right);

            case MINUS:
                this.checkNumberOperands(expression.operator, left, right);
                if (left instanceof Double || right instanceof Double) {
                    return ((Number) left).doubleValue() - ((Number) right).doubleValue();
                }
                return (int) left - (int) right;

            case SLASH:
                this.checkNumberOperands(expression.operator, left, right);
                if (left instanceof Double || right instanceof Double) {
                    return ((Number) left).doubleValue() / ((Number) right).doubleValue();
                }
                return (int) left / (int) right;

            case STAR:
                this.checkNumberOperands(expression.operator, left, right);
                if (left instanceof Double || right instanceof Double) {
                    return ((Number) left).doubleValue() * ((Number) right).doubleValue();
                }
                return (int) left * (int) right;

            case PLUS:
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                if (left instanceof Number && right instanceof Number) {
                    if (left instanceof Double || right instanceof Double) {
                        return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                    }
                    return (int) left + (int) right;
                }
                throw new RuntimeError(expression.operator,
                        "Les opérateurs doivent tous être des nombres ou des chaînes de caractères.");
        }

        return null;
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

        switch (expression.operator.type())
        {
            case MINUS:
                this.checkNumberOperand(expression.operator, right);
                if (right instanceof Double) {
                    return -(double) right;
                }
                return -(int) right;
            case BANG:
                return !this.isTruth(right);
        }

        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression)
    {
        return expression.value;
    }

    private Object evaluate(Expression expression)
    {
        return expression.accept(this);
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

    private void checkNumberOperand(Token operator, Object operand)
    {
        if (!(operand instanceof Number))
        {
            throw new RuntimeError(operator, "L'opérateur doit être un nombre");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Number && right instanceof Number)
        {
            return;
        }
        throw new RuntimeError(operator, "Les opérateurs doivent tous être des nombres.");
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
}
