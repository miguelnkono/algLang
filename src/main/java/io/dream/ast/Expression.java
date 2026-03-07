package io.dream.ast;

import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;
import java.util.Objects;
import io.dream.scanner.Token;

/**
 * Expression AST nodes for AlgoLang
 * Includes all expression types: literals, variables, binary/unary operations,
 * function calls, array access, field access
 */
public abstract class Expression
{
	public interface Visitor<R> {
		R visitBinaryExpression(Binary expression);
		R visitGroupingExpression(Grouping expression);
		R visitUnaryExpression(Unary expression);
		R visitLiteralExpression(Literal expression);
		R visitVariableExpression(Variable expression);
		R visitLogicalExpression(Logical expression);
		R visitCallExpression(Call expression);
		R visitArrayAccessExpression(ArrayAccess expression);
		R visitFieldAccessExpression(FieldAccess expression);
		R visitArrayLiteralExpression(ArrayLiteral expression);
	}

	// ========================================================================
	// BINARY EXPRESSION (e.g., a + b, x * y)
	// ========================================================================
	public static class Binary extends Expression
	{
		public Binary(Expression left, Token operator, Expression right)
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

	// ========================================================================
	// GROUPING EXPRESSION (e.g., (a + b))
	// ========================================================================
	public static class Grouping extends Expression
	{
		public Grouping(Expression expression)
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

	// ========================================================================
	// UNARY EXPRESSION (e.g., -x, !flag)
	// ========================================================================
	public static class Unary extends Expression
	{
		public Unary(Token operator, Expression right)
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

	// ========================================================================
	// LITERAL EXPRESSION (e.g., 42, "hello", true)
	// ========================================================================
	public static class Literal extends Expression
	{
		public Literal(Value value)
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

	// ========================================================================
	// VARIABLE EXPRESSION (e.g., x, name, count)
	// ========================================================================
	public static class Variable extends Expression
	{
		public Variable(Token name)
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

	// ========================================================================
	// LOGICAL EXPRESSION (e.g., a and b, x or y)
	// ========================================================================
	public static class Logical extends Expression
	{
		public Logical(Expression left, Token operator, Expression right)
		{
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpression(this);
		}

		public final Expression left;
		public final Token operator;
		public final Expression right;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Logical that = (Expression.Logical) o;
			return Objects.equals(left, that.left) &&
					Objects.equals(operator, that.operator) &&
					Objects.equals(right, that.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, operator, right);
		}
	}

	// ========================================================================
	// CALL EXPRESSION (e.g., function(arg1, arg2))
	// ========================================================================
	public static class Call extends Expression
	{
		public Call(Token name, List<Expression> arguments)
		{
			this.name = name;
			this.arguments = arguments;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpression(this);
		}

		public final Token name;
		public final List<Expression> arguments;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.Call that = (Expression.Call) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(arguments, that.arguments);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, arguments);
		}
	}

	// ========================================================================
	// ARRAY ACCESS EXPRESSION (e.g., arr[i], matrix[1][2])
	// ========================================================================
	public static class ArrayAccess extends Expression
	{
		public ArrayAccess(Expression array, Expression index)
		{
			this.array = array;
			this.index = index;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitArrayAccessExpression(this);
		}

		public final Expression array;
		public final Expression index;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.ArrayAccess that = (Expression.ArrayAccess) o;
			return Objects.equals(array, that.array) &&
					Objects.equals(index, that.index);
		}

		@Override
		public int hashCode() {
			return Objects.hash(array, index);
		}
	}

	// ========================================================================
	// FIELD ACCESS EXPRESSION (e.g., person.name, point.x)
	// ========================================================================
	public static class FieldAccess extends Expression
	{
		public FieldAccess(Expression object, Token field)
		{
			this.object = object;
			this.field = field;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFieldAccessExpression(this);
		}

		public final Expression object;
		public final Token field;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.FieldAccess that = (Expression.FieldAccess) o;
			return Objects.equals(object, that.object) &&
					Objects.equals(field, that.field);
		}

		@Override
		public int hashCode() {
			return Objects.hash(object, field);
		}
	}

	// ========================================================================
	// ARRAY LITERAL EXPRESSION (e.g., [1, 2, 3])
	// For inline array initialization
	// ========================================================================
	public static class ArrayLiteral extends Expression
	{
		public ArrayLiteral(List<Expression> elements)
		{
			this.elements = elements;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitArrayLiteralExpression(this);
		}

		public final List<Expression> elements;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Expression.ArrayLiteral that = (Expression.ArrayLiteral) o;
			return Objects.equals(elements, that.elements);
		}

		@Override
		public int hashCode() {
			return Objects.hash(elements);
		}
	}

	// ========================================================================
	// TYPE INFORMATION
	// ========================================================================
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
