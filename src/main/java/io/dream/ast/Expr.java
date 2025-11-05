package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import io.dream.scanner.Token;

public abstract class Expr
{
	public interface Visitor<R> {
		 R visitBinaryExpr (Binary expr);
		 R visitGroupingExpr (Grouping expr);
		 R visitUnaryExpr (Unary expr);
		 R visitLiteralExpr (Literal expr);
	}

    public static class Binary extends Expr 
    {
        public Binary (Expr left, Token operator, Expr right)
        {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		public final Expr left;
		public final Token operator;
		public final Expr right;
    }

    public static class Grouping extends Expr 
    {
        public Grouping (Expr expression)
        {
            this.expression = expression;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		public final Expr expression;
    }

    public static class Unary extends Expr 
    {
        public Unary (Token operator, Expr right)
        {
            this.operator = operator;
            this.right = right;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		public final Token operator;
		public final Expr right;
    }

    public static class Literal extends Expr 
    {
        public Literal (Value value)
        {
            this.value = value;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
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
