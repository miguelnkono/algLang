package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import io.dream.scanner.Token;

public abstract class Stmt
{
	public interface Visitor<R> {
		 R visitExpressionStmt (Expression stmt);
		 R visitPrintStmt (Print stmt);
		 R visitVarStmt (Var stmt);
	}

    public static class Expression extends Stmt 
    {
        public Expression (Expr expression)
        {
            this.expression = expression;
        }

			@Override
			public <R> R accept(Visitor<R> visitor) {
				return visitor.visitExpressionStmt(this);
			}

			public final Expr expression;
    }

    public static class Print extends Stmt 
    {
        public Print (Expr expression)
        {
            this.expression = expression;
        }

			@Override
			public <R> R accept(Visitor<R> visitor) {
				return visitor.visitPrintStmt(this);
			}

			public final Expr expression;
    }

    public static class Var extends Stmt 
    {
        public Var (Token name, Expr initializer)
        {
            this.name = name;
            this.initializer = initializer;
        }

			@Override
			public <R> R accept(Visitor<R> visitor) {
				return visitor.visitVarStmt(this);
			}

			public final Token name;
			public final Expr initializer;
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
