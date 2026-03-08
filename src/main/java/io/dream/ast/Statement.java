package io.dream.ast;

import java.util.HashMap;
import java.util.List;
import io.dream.types.Type;
import io.dream.types.Value;

import java.util.Map;
import java.util.Objects;
import io.dream.scanner.Token;

/**
 * Statement AST nodes for AlgoLang
 * Includes all statement types: declarations, assignments, control flow,
 * functions, methods, structures, etc.
 */
public abstract class Statement
{
	public interface Visitor<R> {
		R visitExpressionStmtStatement(ExpressionStmt statement);
		R visitWriteStatement(Write statement);
		R visitReadStatement(Read statement);
		R visitVariableDeclarationStatement(VariableDeclaration statement);
		R visitConstantDeclarationStatement(ConstantDeclaration statement);
		R visitAssignmentStatement(Assignment statement);
		R visitIfStatement(If statement);
		R visitWhileStatement(While statement);
		R visitDoWhileStatement(DoWhile statement);
		R visitForStatement(For statement);
		R visitFunctionDeclarationStatement(FunctionDeclaration statement);
		R visitMethodDeclarationStatement(MethodDeclaration statement);
		R visitReturnStatement(Return statement);
		R visitStructDeclarationStatement(StructDeclaration statement);
		R visitMethodCallStatement(MethodCall statement);
		R visitArrayAssignmentStatement(ArrayAssignment statement);
		R visitFieldAssignmentStatement(FieldAssignment statement);
		R visitNestedFieldArrayAssignmentStatement(NestedFieldArrayAssignment statement);
		R visitFieldReadStatement(FieldRead statement);
		R visitArrayReadStatement(ArrayRead statement);
		R visitNestedFieldArrayReadStatement(NestedFieldArrayRead statement);
	}

	/**
	 * Read into nested field+array: lire(obj.field[index])
	 * Example: lire(etudiant.notes[i])
	 */
	public static class NestedFieldArrayRead extends Statement
	{
		public final Token objectName;
		public final Token fieldName;
		public final Expression index;

		public NestedFieldArrayRead(Token objectName, Token fieldName, Expression index)
		{
			this.objectName = objectName;
			this.fieldName = fieldName;
			this.index = index;
		}

		@Override
		public <R> R accept(Visitor<R> visitor)
		{
			return visitor.visitNestedFieldArrayReadStatement(this);
		}
	}

	// ========================================================================
	// EXPRESSION STATEMENT (e.g., 5 + 3;)
	// ========================================================================
	public static class ExpressionStmt extends Statement
	{
		public ExpressionStmt(Expression expression)
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

	// ========================================================================
	// WRITE STATEMENT (e.g., ecrire("Hello");)
	// ========================================================================
	public static class Write extends Statement
	{
		public Write(Expression expression)
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

	// ========================================================================
	// READ STATEMENT (e.g., lire(x);)
	// ========================================================================
	public static class Read extends Statement
	{
		public Read(Token variable)
		{
			this.variable = variable;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReadStatement(this);
		}

		public final Token variable;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.Read that = (Statement.Read) o;
			return Objects.equals(variable, that.variable);
		}

		@Override
		public int hashCode() {
			return Objects.hash(variable);
		}
	}

	// ========================================================================
	// VARIABLE DECLARATION (e.g., x : entier;)
	// ========================================================================
	public static class VariableDeclaration extends Statement
	{
		public VariableDeclaration(Token name, Expression value)
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

	// ========================================================================
	// CONSTANT DECLARATION (e.g., PI = 3.14;)
	// ========================================================================
	public static class ConstantDeclaration extends Statement
	{
		public ConstantDeclaration(Token name, Expression value, Type type)
		{
			this.name = name;
			this.value = value;
			this.type = type;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitConstantDeclarationStatement(this);
		}

		public final Token name;
		public final Expression value;
		public final Type type;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.ConstantDeclaration that = (Statement.ConstantDeclaration) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(value, that.value) &&
					Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value, type);
		}
	}

	// ========================================================================
	// ASSIGNMENT STATEMENT (e.g., x <- 5;)
	// ========================================================================
	public static class Assignment extends Statement
	{
		public Assignment(Token name, Expression value)
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
	/**
	 * Array assignment: arr[index] <- value
	 */
	public static class ArrayAssignment extends Statement
	{
		public final Token arrayName;
		public final Expression index;
		public final Expression value;

		public ArrayAssignment(Token arrayName, Expression index, Expression value)
		{
			this.arrayName = arrayName;
			this.index = index;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor)
		{
			return visitor.visitArrayAssignmentStatement(this);
		}
	}

	/**
	 * Field assignment: object.field <- value
	 */
	public static class FieldAssignment extends Statement
	{
		public final Token objectName;
		public final Token fieldName;
		public final Expression value;

		public FieldAssignment(Token objectName, Token fieldName, Expression value)
		{
			this.objectName = objectName;
			this.fieldName = fieldName;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor)
		{
			return visitor.visitFieldAssignmentStatement(this);
		}
	}

	/**
	 * Nested field+array assignment: object.field[index] <- value
	 * Example: etudiant.notes[1] <- 15.5;
	 */
	public static class NestedFieldArrayAssignment extends Statement
	{
		public final Token objectName;
		public final Token fieldName;
		public final Expression index;
		public final Expression value;

		public NestedFieldArrayAssignment(Token objectName, Token fieldName,
										  Expression index, Expression value)
		{
			this.objectName = objectName;
			this.fieldName = fieldName;
			this.index = index;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor)
		{
			return visitor.visitNestedFieldArrayAssignmentStatement(this);
		}
	}

	// ========================================================================
	// IF STATEMENT (e.g., si condition alors ... finsi)
	// ========================================================================
	public static class If extends Statement
	{
		public If(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch)
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
		public final List<Statement> thenBranch;
		public final List<Statement> elseBranch;

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

	// ========================================================================
	// WHILE STATEMENT (e.g., tant_que (condition) faire ... fintantque)
	// ========================================================================
	public static class While extends Statement
	{
		public While(Expression condition, List<Statement> body)
		{
			this.condition = condition;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStatement(this);
		}

		public final Expression condition;
		public final List<Statement> body;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.While that = (Statement.While) o;
			return Objects.equals(condition, that.condition) &&
					Objects.equals(body, that.body);
		}

		@Override
		public int hashCode() {
			return Objects.hash(condition, body);
		}
	}

	// ========================================================================
	// DO-WHILE STATEMENT (e.g., repeter ... jusqu_a (condition);)
	// ========================================================================
	public static class DoWhile extends Statement
	{
		public DoWhile(List<Statement> body, Expression condition)
		{
			this.body = body;
			this.condition = condition;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitDoWhileStatement(this);
		}

		public final List<Statement> body;
		public final Expression condition;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.DoWhile that = (Statement.DoWhile) o;
			return Objects.equals(body, that.body) &&
					Objects.equals(condition, that.condition);
		}

		@Override
		public int hashCode() {
			return Objects.hash(body, condition);
		}
	}

	// ========================================================================
	// FOR STATEMENT (e.g., pour i <- 1 jusqu_a 10 faire ... finpour)
	// ========================================================================
	public static class For extends Statement
	{
		public For(Token variable, Expression start, Expression end, Expression step, List<Statement> body)
		{
			this.variable = variable;
			this.start = start;
			this.end = end;
			this.step = step;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitForStatement(this);
		}

		public final Token variable;
		public final Expression start;
		public final Expression end;
		public final Expression step;  // Can be null (defaults to 1)
		public final List<Statement> body;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.For that = (Statement.For) o;
			return Objects.equals(variable, that.variable) &&
					Objects.equals(start, that.start) &&
					Objects.equals(end, that.end) &&
					Objects.equals(step, that.step) &&
					Objects.equals(body, that.body);
		}

		@Override
		public int hashCode() {
			return Objects.hash(variable, start, end, step, body);
		}
	}

	// ========================================================================
	// FUNCTION DECLARATION
	// (e.g., Fonction: carre(x: entier): entier; ... FinFonction;)
	// ========================================================================
	public static class FunctionDeclaration extends Statement
	{
		public FunctionDeclaration(Token name, List<Parameter> parameters, Type returnType,
								   List<Statement> body, Map<String, Type> localVariables)
		{
			this.name = name;
			this.parameters = parameters;
			this.returnType = returnType;
			this.body = body;
			this.localVariables = localVariables != null ? localVariables : new HashMap<>();
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionDeclarationStatement(this);
		}

		public final Token name;
		public final List<Parameter> parameters;
		public final Type returnType;
		public final List<Statement> body;
		public final Map<String, Type> localVariables;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.FunctionDeclaration that = (Statement.FunctionDeclaration) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(parameters, that.parameters) &&
					Objects.equals(returnType, that.returnType) &&
					Objects.equals(body, that.body);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, parameters, returnType, body);
		}
	}

	/**
	 * Read into structure field: lire(obj.field)
	 */
	public static class FieldRead extends Statement
	{
		public final Token objectName;
		public final Token fieldName;

		public FieldRead(Token objectName, Token fieldName)
		{
			this.objectName = objectName;
			this.fieldName = fieldName;
		}

		@Override
		public <R> R accept(Visitor<R> visitor)
		{
			return visitor.visitFieldReadStatement(this);
		}
	}

	/**
	 * Read into array element: lire(arr[index])
	 */
	public static class ArrayRead extends Statement
	{
		public final Token arrayName;
		public final Expression index;

		public ArrayRead(Token arrayName, Expression index)
		{
			this.arrayName = arrayName;
			this.index = index;
		}

		@Override
		public <R> R accept(Visitor<R> visitor)
		{
			return visitor.visitArrayReadStatement(this);
		}
	}

	public static class MethodDeclaration extends Statement
	{
		public MethodDeclaration(Token name, List<Parameter> parameters, List<Statement> body, Map<String, Type> localVariables)
		{
			this.name = name;
			this.parameters = parameters;
			this.body = body;
			this.localVariables = localVariables != null ? localVariables : new HashMap<>();
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitMethodDeclarationStatement(this);
		}

		public final Token name;
		public final List<Parameter> parameters;
		public final List<Statement> body;
		public final Map<String, Type> localVariables;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.MethodDeclaration that = (Statement.MethodDeclaration) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(parameters, that.parameters) &&
					Objects.equals(body, that.body);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, parameters, body);
		}
	}

	// ========================================================================
	// RETURN STATEMENT (e.g., retourne x * x;)
	// ========================================================================
	public static class Return extends Statement
	{
		public Return(Token keyword, Expression value)
		{
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStatement(this);
		}

		public final Token keyword;
		public final Expression value;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.Return that = (Statement.Return) o;
			return Objects.equals(keyword, that.keyword) &&
					Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(keyword, value);
		}
	}

	// ========================================================================
	// STRUCTURE DECLARATION
	// (e.g., Type: Structure Personne ... FinStruct)
	// ========================================================================
	public static class StructDeclaration extends Statement
	{
		public StructDeclaration(Token name, List<Field> fields)
		{
			this.name = name;
			this.fields = fields;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitStructDeclarationStatement(this);
		}

		public final Token name;
		public final List<Field> fields;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.StructDeclaration that = (Statement.StructDeclaration) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(fields, that.fields);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, fields);
		}
	}

	// ========================================================================
	// METHOD CALL STATEMENT (e.g., afficher(x);)
	// ========================================================================
	public static class MethodCall extends Statement
	{
		public MethodCall(Token name, List<Expression> arguments)
		{
			this.name = name;
			this.arguments = arguments;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitMethodCallStatement(this);
		}

		public final Token name;
		public final List<Expression> arguments;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Statement.MethodCall that = (Statement.MethodCall) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(arguments, that.arguments);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, arguments);
		}
	}

	// ========================================================================
	// HELPER CLASSES
	// ========================================================================

	/**
	 * Parameter for function/method declarations
	 */
	public static class Parameter
	{
		public Parameter(Token name, Type type)
		{
			this.name = name;
			this.type = type;
		}

		public final Token name;
		public final Type type;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Parameter that = (Parameter) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, type);
		}
	}

	/**
	 * Field for structure declarations
	 */
	public static class Field
	{
		public Field(Token name, Type type)
		{
			this.name = name;
			this.type = type;
		}

		public final Token name;
		public final Type type;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Field that = (Field) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, type);
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
