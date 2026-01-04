package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import io.dream.scanner.Token;

public abstract class Expression
{
	public interface Visitor<R> {
		 R visitBinaryExpression (Binary expression);
		 R visitGroupingExpression (Grouping expression);
		 R visitUnaryExpression (Unary expression);
		 R visitLiteralExpression (Literal expression);
	}

    public static class Binary extends Expression 
    {
        public Binary (Expression left, Token operator, Expression right)
        {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpression(this);
		}

		public final Expression left;
		public final Token operator;
		public final Expression right;
    }

    public static class Grouping extends Expression 
    {
        public Grouping (Expression expression)
        {
            this.expression = expression;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpression(this);
		}

		public final Expression expression;
    }

    public static class Unary extends Expression 
    {
        public Unary (Token operator, Expression right)
        {
            this.operator = operator;
            this.right = right;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpression(this);
		}

		public final Token operator;
		public final Expression right;
    }

    public static class Literal extends Expression 
    {
        public Literal (Value value)
        {
            this.value = value;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpression(this);
		}

		public final Value value;
    }

	private Type type;

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public abstract <R> R accept(Visitor<R> visitor);
}
