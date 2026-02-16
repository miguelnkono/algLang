package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import io.dream.scanner.Token;

public abstract class Statement
{
	public interface Visitor<R> {
		 R visitExpressionStmtStatement (ExpressionStmt statement);
		 R visitWriteStatement (Write statement);
		 R visitVariableDeclarationStatement (VariableDeclaration statement);
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

    public static class Write extends Statement 
    {
        public Write (Expression expression)
        {
            this.expression = expression;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWriteStatement(this);
		}

		public final Expression expression;
    }

    public static class VariableDeclaration extends Statement 
    {
        public VariableDeclaration (Token name, Expression value)
        {
            this.name = name;
            this.value = value;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableDeclarationStatement(this);
		}

		public final Token name;
		public final Expression value;
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
