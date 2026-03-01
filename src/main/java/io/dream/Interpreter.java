package io.dream;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.config.Messages;
import io.dream.environment.Environment;
import io.dream.error.RuntimeError;
import io.dream.scanner.Token;
import io.dream.types.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void>
{
    private final Checker typeChecker;
    private final Environment environment;
    private final Map<String, Type> symbolTable;

    public Interpreter()
    {
        this.typeChecker = new Checker();
        this.environment = new Environment();
        this.symbolTable = new HashMap<>();
    }

    public Interpreter(Map<String, Type> symbolTable)
    {
        this.typeChecker = new Checker(symbolTable);
        this.environment = new Environment();
        this.symbolTable = symbolTable;
    }

    // Updated to accept List<Statement>
    public void interpret(List<Statement> statements)
    {
        try
        {
            // Initialize all declared variables with their zero values
            for (Map.Entry<String, Type> entry : symbolTable.entrySet())
            {
                String varName = entry.getKey();
                Type varType = entry.getValue();
                Value zeroValue = varType.zeroValue();
                environment.define(varName, varType, zeroValue);
            }

            // Then execute the statements
            for (Statement statement : statements)
            {
                execute(statement);
            }
        } catch (RuntimeError re)
        {
            Main.runtimeError(re);
        }
    }

    // Keep this for backward compatibility if needed
//    public void interpret(Expression expression)
//    {
//        try
//        {
//            Expression typedExpression = typeChecker.check(expression);
//            Object result = this.evaluate(typedExpression);
//            System.out.println(this.stringify(result));
//        } catch (RuntimeError re)
//        {
//            Main.runtimeError(re);
//        }
//    }

    private void execute(Statement statement)
    {
        statement.accept(this);
    }

    @Override
    public Void visitExpressionStmtStatement(Statement.ExpressionStmt statement)
    {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitWriteStatement(Statement.Write statement) {
        System.out.println(stringify(evaluate(statement.expression)));
        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration statement)
    {
        Value value = null;

        // If there's an initializer, evaluate it
        if (statement.value != null) {
            Object evaluatedValue = evaluate(statement.value);
            Type varType = statement.value.getType();

            // Wrap the value in an AtomicValue
            if (varType.equals(TypeFactory.INTEGER)) {
                value = new AtomicValue<>((Integer) evaluatedValue, AtomicTypes.INTEGER);
            } else if (varType.equals(TypeFactory.FLOATING)) {
                value = new AtomicValue<>((Double) evaluatedValue, AtomicTypes.FLOATING);
            } else if (varType.equals(TypeFactory.STRING)) {
                value = new AtomicValue<>((String) evaluatedValue, AtomicTypes.STRING);
            } else if (varType.equals(TypeFactory.CHAR)) {
                value = new AtomicValue<>((Character) evaluatedValue, AtomicTypes.CHAR);
            } else if (varType.equals(TypeFactory.BOOLEAN)) {
                value = new AtomicValue<>((Boolean) evaluatedValue, AtomicTypes.BOOLEAN);
            }
        } else {
            // Use zero value from type
            Type varType = statement.getType();
            if (varType != null) {
                value = varType.zeroValue();
            }
        }

        Type varType = statement.getType();
        if (varType == null) {
            // Get type from the expression
            varType = statement.value != null ? statement.value.getType() : TypeFactory.VOID;
        }

        environment.define(statement.name.lexeme(), varType, value);
        return null;
    }

    @Override
    public Void visitAssignmentStatement(Statement.Assignment statement)
    {
        Object evaluatedValue = evaluate(statement.value);
        Type varType = statement.value.getType();

        // Wrap the value in an AtomicValue
        Value value = null;
        if (varType.equals(TypeFactory.INTEGER)) {
            value = new AtomicValue<>((Integer) evaluatedValue, AtomicTypes.INTEGER);
        } else if (varType.equals(TypeFactory.FLOATING)) {
            value = new AtomicValue<>((Double) evaluatedValue, AtomicTypes.FLOATING);
        } else if (varType.equals(TypeFactory.STRING)) {
            value = new AtomicValue<>((String) evaluatedValue, AtomicTypes.STRING);
        } else if (varType.equals(TypeFactory.CHAR)) {
            value = new AtomicValue<>((Character) evaluatedValue, AtomicTypes.CHAR);
        } else if (varType.equals(TypeFactory.BOOLEAN)) {
            value = new AtomicValue<>((Boolean) evaluatedValue, AtomicTypes.BOOLEAN);
        }

        environment.update_value(statement.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruth(evaluate(statement.condition))) {
            for (Statement stmt : statement.thenBranch) {
                execute(stmt);
            }
        } else if (statement.elseBranch != null) {
            for (Statement stmt : statement.elseBranch) {
                execute(stmt);
            }
        }

        return null;
    }

    private Object evaluate(Expression expression)
    {
        return expression.accept(this);
    }

    // Rest of the Expression.Visitor methods remain the same...
    @Override
    public Object visitBinaryExpression(Expression.Binary expression)
    {
        Object left = this.evaluate(expression.left);
        Object right = this.evaluate(expression.right);

        // Use the type information from the type checker
        Type exprType = expression.getType();

        switch (expression.operator.type())
        {
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
                this.checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    int leftVal = (int) left;
                    int rightVal = (int) right;
                    return getComparison(expression, (double) leftVal, (double) rightVal);
                } else
                {
                    double leftVal = (double) left;
                    double rightVal = (double) right;
                    return getComparison(expression, leftVal, rightVal);
                }

            case EQUAL_EQUAL:
                return this.isEqual(left, right);
            case DIFF:
                return !this.isEqual(left, right);

            case MINUS:
                this.checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left - (int) right;
                } else
                {
                    return (double) left - (double) right;
                }

            case SLASH:
                this.checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left / (int) right;
                } else
                {
                    return (double) left / (double) right;
                }

            case STAR:
                this.checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left * (int) right;
                } else
                {
                    return (double) left * (double) right;
                }

            case PLUS:
                if (exprType.equals(TypeFactory.STRING))
                {
                    // Handle string concatenation with various types
                    String result = "";

                    if (left instanceof String) {
                        result += (String) left;
                    } else {
                        result += String.valueOf(left);
                    }

                    if (right instanceof String) {
                        result += (String) right;
                    } else {
                        result += String.valueOf(right);
                    }

                    return result;
                } else if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left + (int) right;
                } else
                {
                    return (double) left + (double) right;
                }
        }

        return null;
    }

    private static Object getComparison(Expression.Binary expression, double leftVal, double rightVal)
    {
        return switch (expression.operator.type())
        {
            case GREATER -> leftVal > rightVal;
            case GREATER_OR_EQUAL -> leftVal >= rightVal;
            case LESS -> leftVal < rightVal;
            case LESS_OR_EQUAL -> leftVal <= rightVal;
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression)
    {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression)
    {
        Object right = this.evaluate(expression.right);

        // Use the type information from the type checker
        Type exprType = expression.getType();

        switch (expression.operator.type())
        {
            case MINUS:
                this.checkNumberOperand(expression.operator, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return -(int) right;
                } else
                {
                    return -(double) right;
                }
            case BANG:
                return !this.isTruth(right);
        }

        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression)
    {
        // Extract value from AtomicValue
        if (expression.value instanceof AtomicValue)
        {
            AtomicValue<?> atomicValue = (AtomicValue<?>) expression.value;
            return atomicValue.getValue();
        }
        return expression.value;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression)
    {
        Value value = environment.get_value(expression.name.lexeme());

        // Extract the actual value from AtomicValue
        if (value instanceof AtomicValue) {
            return ((AtomicValue<?>) value).getValue();
        }

        return value;
    }

    private boolean isTruth(Object value)
    {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        return true;
    }

    private boolean isEqual(Object left, Object right)
    {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand)
    {
        if (!(operand instanceof Number))
        {
            throw new RuntimeError(operator, Messages.operandMustBeNumber());
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Number && right instanceof Number)
        {
            return;
        }
        throw new RuntimeError(operator, Messages.operandsMustBeNumbers());
    }

    private String stringify(Object value)
    {
        if (value == null) return "nil";

        if (value instanceof Double)
        {
            String text = value.toString();
            if (text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }
}
