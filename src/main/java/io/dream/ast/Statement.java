package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import io.dream.scanner.Token;

public abstract class Statement
{
	public interface Visitor<R> {
		 R visitExpressionStmtStatement (ExpressionStmt statement);
	}

    public static class ExpressionStmt extends Statement 
    {
        public ExpressionStmt (Expression expression)
        {
            this.expression = expression;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmtStatement(this);
		}

		public final Expression expression;
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
