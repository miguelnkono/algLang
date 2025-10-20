package io.dream.tools;

import io.dream.ast.Expression;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;

public class AstPrinter implements Expression.Visitor<String>
{
    public String print(Expression expression)
    {
        return expression.accept(this);
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression)
    {
        return parenthesize(expression.operator.lexeme(), expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression)
    {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator.lexeme(), expression.right);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value == null)
        {
            return "nil";
        }
        return expression.value.toString();
    }

    private String parenthesize(String name, Expression... expressions)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(").append(name);
        for (Expression expression : expressions)
        {
            stringBuilder.append(" ");
            stringBuilder.append(expression.accept(this));
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    public static void main(String[] args)
    {
        Expression expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
//                        new Expression.Literal(123)
                        new Expression.Binary(
                                new Expression.Literal(new AtomicValue<Integer>(22, AtomicTypes.INTEGER)),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expression.Literal(new AtomicValue<Integer>(5, AtomicTypes.INTEGER))
                        )
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expression.Grouping(new Expression.Literal(new AtomicValue<Double>(45.67, AtomicTypes.FLOATING)))
        );

        System.out.println(new AstPrinter().print(expression));
    }
}
