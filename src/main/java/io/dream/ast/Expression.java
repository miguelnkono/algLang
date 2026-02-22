package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import java.util.Objects;
import io.dream.scanner.Token;

public abstract class Expression
{
	public interface Visitor<R> {
		 R visitBinaryExpression (Binary expression);
		 R visitGroupingExpression (Grouping expression);
		 R visitUnaryExpression (Unary expression);
		 R visitLiteralExpression (Literal expression);
		 R visitVariableExpression (Variable expression);
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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Binary that = (Expression.Binary) o;
			return Objects.equals(left, that.left) &&
				Objects.equals(operator, that.operator) &&
				Objects.equals(right, that.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, operator, right);
		}

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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Grouping that = (Expression.Grouping) o;
			return Objects.equals(expression, that.expression);
		}

		@Override
		public int hashCode() {
			return Objects.hash(expression);
		}

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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Unary that = (Expression.Unary) o;
			return Objects.equals(operator, that.operator) &&
				Objects.equals(right, that.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(operator, right);
		}

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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Literal that = (Expression.Literal) o;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

    }

    public static class Variable extends Expression 
    {
        public Variable (Token name)
        {
            this.name = name;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpression(this);
		}

		public final Token name;
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Variable that = (Expression.Variable) o;
			return Objects.equals(name, that.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}

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
