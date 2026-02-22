package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import java.util.Objects;
import io.dream.scanner.Token;

public abstract class Statement
{
	public interface Visitor<R> {
		 R visitExpressionStmtStatement (ExpressionStmt statement);
		 R visitWriteStatement (Write statement);
		 R visitVariableDeclarationStatement (VariableDeclaration statement);
		 R visitAssignmentStatement (Assignment statement);
		 R visitIfStatement (If statement);
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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.ExpressionStmt that = (Statement.ExpressionStmt) o;
			return Objects.equals(expression, that.expression);
		}

		@Override
		public int hashCode() {
			return Objects.hash(expression);
		}

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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.Write that = (Statement.Write) o;
			return Objects.equals(expression, that.expression);
		}

		@Override
		public int hashCode() {
			return Objects.hash(expression);
		}

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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.VariableDeclaration that = (Statement.VariableDeclaration) o;
			return Objects.equals(name, that.name) &&
				Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}

    }

    public static class Assignment extends Statement 
    {
        public Assignment (Token name, Expression value)
        {
            this.name = name;
            this.value = value;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignmentStatement(this);
		}

		public final Token name;
		public final Expression value;
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.Assignment that = (Statement.Assignment) o;
			return Objects.equals(name, that.name) &&
				Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}

    }

    public static class If extends Statement 
    {
        public If (Expression condition, Statement thenBranch, Statement elseBranch)
        {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStatement(this);
		}

		public final Expression condition;
		public final Statement thenBranch;
		public final Statement elseBranch;
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.If that = (Statement.If) o;
			return Objects.equals(condition, that.condition) &&
				Objects.equals(thenBranch, that.thenBranch) &&
				Objects.equals(elseBranch, that.elseBranch);
		}

		@Override
		public int hashCode() {
			return Objects.hash(condition, thenBranch, elseBranch);
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
