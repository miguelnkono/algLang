package io.dream.tools;

import io.dream.ast.Expr;
import io.dream.ast.Expr.Variable;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;

public class AstPrinter implements Expr.Visitor<String>
{
    public String print(Expr expr)
    {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expression)
    {
        return parenthesize(expression.operator.lexeme(), expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expression)
    {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expression) {
        return parenthesize(expression.operator.lexeme(), expression.right);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expression) {
        if (expression.value == null)
        {
            return "nil";
        }
        return expression.value.toString();
    }

    private String parenthesize(String name, Expr... exprs)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(").append(name);
        for (Expr expr : exprs)
        {
            stringBuilder.append(" ");
            stringBuilder.append(expr.accept(this));
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    public static void main(String[] args)
    {
        Expr expr = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
//                        new Expression.Literal(123)
                        new Expr.Binary(
                                new Expr.Literal(new AtomicValue<Integer>(22, AtomicTypes.INTEGER)),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expr.Literal(new AtomicValue<Integer>(5, AtomicTypes.INTEGER))
                        )
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(new AtomicValue<Double>(45.67, AtomicTypes.FLOATING)))
        );

        System.out.println(new AstPrinter().print(expr));
    }

		@Override
		public String visitVariableExpr(Variable expr)
		{
			// TODO Auto-generated method stub
			return null;
		}
}
