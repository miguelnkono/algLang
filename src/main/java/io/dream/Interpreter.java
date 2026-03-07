package io.dream;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.config.Messages;
import io.dream.environment.Environment;
import io.dream.error.RuntimeError;
import io.dream.scanner.Token;
import io.dream.types.*;

import java.util.*;

/**
 * Complete Interpreter for AlgoLang
 * Executes the validated AST with runtime value management
 */
public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void>
{
    // Global environment
    private final Environment globals;

    // Current environment (changes with scopes)
    private Environment environment;

    // Symbol tables from parser
    private final Map<String, Type> globalSymbolTable;
    private final Map<String, FunctionType> functionTable;
    private final Map<String, List<Statement.Parameter>> methodTable;
    private final Map<String, StructType> structTable;

    // Function and method bodies (for execution)
    private final Map<String, Statement.FunctionDeclaration> functions;
    private final Map<String, Statement.MethodDeclaration> methods;

    // Return value handling
    private static class ReturnException extends RuntimeException
    {
        final Object value;

        ReturnException(Object value)
        {
            super(null, null, false, false);
            this.value = value;
        }
    }

    /**
     * Constructor with just symbol table (backward compatibility)
     */
    public Interpreter(Map<String, Type> symbolTable)
    {
        this.globals = new Environment();
        this.environment = globals;
        this.globalSymbolTable = symbolTable;
        this.functionTable = new HashMap<>();
        this.methodTable = new HashMap<>();
        this.structTable = new HashMap<>();
        this.functions = new HashMap<>();
        this.methods = new HashMap<>();
    }

    /**
     * Constructor with all symbol tables
     */
    public Interpreter(Map<String, Type> symbolTable,
                       Map<String, FunctionType> functionTable,
                       Map<String, List<Statement.Parameter>> methodTable,
                       Map<String, StructType> structTable)
    {
        this.globals = new Environment();
        this.environment = globals;
        this.globalSymbolTable = symbolTable;
        this.functionTable = functionTable != null ? functionTable : new HashMap<>();
        this.methodTable = methodTable != null ? methodTable : new HashMap<>();
        this.structTable = structTable != null ? structTable : new HashMap<>();
        this.functions = new HashMap<>();
        this.methods = new HashMap<>();
    }

    /**
     * Main interpretation entry point
     */
    public void interpret(List<Statement> statements)
    {
        try
        {
            // First pass: Register functions and methods
            for (Statement statement : statements)
            {
                if (statement instanceof Statement.FunctionDeclaration)
                {
                    Statement.FunctionDeclaration func = (Statement.FunctionDeclaration) statement;
                    functions.put(func.name.lexeme(), func);
                }
                else if (statement instanceof Statement.MethodDeclaration)
                {
                    Statement.MethodDeclaration method = (Statement.MethodDeclaration) statement;
                    methods.put(method.name.lexeme(), method);
                }
            }

            // Initialize all global variables with their zero values
            for (Map.Entry<String, Type> entry : globalSymbolTable.entrySet())
            {
                String varName = entry.getKey();
                Type varType = entry.getValue();
                Value zeroValue = varType.zeroValue();
                environment.define(varName, varType, zeroValue);
            }

            // Execute all statements
            for (Statement statement : statements)
            {
                execute(statement);
            }
        }
        catch (RuntimeError re)
        {
            Main.runtimeError(re);
        }
    }

    /**
     * Execute a single statement
     */
    private void execute(Statement statement)
    {
        statement.accept(this);
    }

    /**
     * Evaluate an expression
     */
    private Object evaluate(Expression expression)
    {
        return expression.accept(this);
    }

    // ========================================================================
    // STATEMENT VISITORS
    // ========================================================================

    @Override
    public Void visitExpressionStmtStatement(Statement.ExpressionStmt statement)
    {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitWriteStatement(Statement.Write statement)
    {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReadStatement(Statement.Read statement)
    {
        // Read from standard input
        Scanner scanner = new Scanner(System.in);
        String varName = statement.variable.lexeme();

        // Get variable type
        Type varType = environment.get_type(varName);

        // Read value based on type
        Object value = null;

        if (varType.equals(TypeFactory.INTEGER))
        {
            if (scanner.hasNextInt())
            {
                value = scanner.nextInt();
            }
            else
            {
                throw new RuntimeError(statement.variable, "Expected integer input");
            }
        }
        else if (varType.equals(TypeFactory.FLOATING))
        {
            if (scanner.hasNextDouble())
            {
                value = scanner.nextDouble();
            }
            else
            {
                throw new RuntimeError(statement.variable, "Expected real number input");
            }
        }
        else if (varType.equals(TypeFactory.STRING))
        {
            value = scanner.nextLine();
        }
        else if (varType.equals(TypeFactory.CHAR))
        {
            String input = scanner.nextLine();
            if (input.length() > 0)
            {
                value = input.charAt(0);
            }
            else
            {
                throw new RuntimeError(statement.variable, "Expected character input");
            }
        }
        else if (varType.equals(TypeFactory.BOOLEAN))
        {
            if (scanner.hasNextBoolean())
            {
                value = scanner.nextBoolean();
            }
            else
            {
                throw new RuntimeError(statement.variable, "Expected boolean input");
            }
        }

        // Wrap value and update variable
        Value wrappedValue = wrapValue(value, varType);
        environment.update_value(varName, wrappedValue);

        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration statement)
    {
        Value value = null;

        if (statement.value != null)
        {
            Object evaluatedValue = evaluate(statement.value);
            Type varType = statement.value.getType();
            value = wrapValue(evaluatedValue, varType);
        }
        else
        {
            Type varType = statement.getType();
            if (varType != null)
            {
                value = varType.zeroValue();
            }
        }

        Type varType = statement.getType();
        if (varType == null && statement.value != null)
        {
            varType = statement.value.getType();
        }

        environment.define(statement.name.lexeme(), varType, value);
        return null;
    }

    @Override
    public Void visitConstantDeclarationStatement(Statement.ConstantDeclaration statement)
    {
        // Constants are evaluated and stored
        Object value = evaluate(statement.value);
        Type type = statement.value.getType();
        Value wrappedValue = wrapValue(value, type);

        // Store as a variable that won't be reassigned (no special handling needed)
        environment.define(statement.name.lexeme(), type, wrappedValue);
        return null;
    }

    @Override
    public Void visitAssignmentStatement(Statement.Assignment statement)
    {
        Object evaluatedValue = evaluate(statement.value);
        Type varType = statement.value.getType();
        Value value = wrapValue(evaluatedValue, varType);

        environment.update_value(statement.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement)
    {
        Object condition = evaluate(statement.condition);

        if (isTruthy(condition))
        {
            for (Statement stmt : statement.thenBranch)
            {
                execute(stmt);
            }
        }
        else if (statement.elseBranch != null && !statement.elseBranch.isEmpty())
        {
            for (Statement stmt : statement.elseBranch)
            {
                execute(stmt);
            }
        }

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement)
    {
        while (isTruthy(evaluate(statement.condition)))
        {
            for (Statement stmt : statement.body)
            {
                execute(stmt);
            }
        }
        return null;
    }

    @Override
    public Void visitDoWhileStatement(Statement.DoWhile statement)
    {
        do
        {
            for (Statement stmt : statement.body)
            {
                execute(stmt);
            }
        }
        while (isTruthy(evaluate(statement.condition)));

        return null;
    }

    @Override
    public Void visitNestedFieldArrayAssignmentStatement(Statement.NestedFieldArrayAssignment statement)
    {
        // Get structure value
        Value structVal = environment.get_value(statement.objectName.lexeme());

        if (!(structVal instanceof StructValue))
        {
            throw new RuntimeError(statement.objectName,
                    Messages.cannotAccessFieldOfNonStruct());
        }

        StructValue struct = (StructValue) structVal;

        // Get field value (should be an array)
        Value fieldVal = struct.getField(statement.fieldName.lexeme());

        if (!(fieldVal instanceof ArrayValue))
        {
            throw new RuntimeError(statement.fieldName,
                    "Field '" + statement.fieldName.lexeme() + "' is not an array");
        }

        ArrayValue array = (ArrayValue) fieldVal;

        // Evaluate index
        Object indexObj = evaluate(statement.index);
        if (!(indexObj instanceof Integer))
        {
            throw new RuntimeError(null, "Array index must be an integer");
        }

        int index = (Integer) indexObj;

        // Evaluate value
        Object value = evaluate(statement.value);
        Type valueType = statement.value.getType();
        Value wrappedValue = wrapValue(value, valueType);

        // Set array element within the structure's field
        array.set(index, wrappedValue);

        return null;
    }

    @Override
    public Void visitArrayAssignmentStatement(Statement.ArrayAssignment statement)
    {
        // Get array value
        Value arrayVal = environment.get_value(statement.arrayName.lexeme());

        if (!(arrayVal instanceof ArrayValue))
        {
            throw new RuntimeError(statement.arrayName, Messages.cannotIndexNonArray());
        }

        ArrayValue array = (ArrayValue) arrayVal;

        // Evaluate index
        Object indexObj = evaluate(statement.index);
        if (!(indexObj instanceof Integer))
        {
            throw new RuntimeError(null, "Array index must be an integer");
        }

        int index = (Integer) indexObj;

        // Evaluate value
        Object value = evaluate(statement.value);
        Type valueType = statement.value.getType();
        Value wrappedValue = wrapValue(value, valueType);

        // Set array element
        array.set(index, wrappedValue);

        return null;
    }

    @Override
    public Void visitFieldAssignmentStatement(Statement.FieldAssignment statement)
    {
        // Get structure value
        Value structVal = environment.get_value(statement.objectName.lexeme());

        if (!(structVal instanceof StructValue))
        {
            throw new RuntimeError(statement.objectName,
                    Messages.cannotAccessFieldOfNonStruct());
        }

        StructValue struct = (StructValue) structVal;

        // Evaluate value
        Object value = evaluate(statement.value);
        Type valueType = statement.value.getType();
        Value wrappedValue = wrapValue(value, valueType);

        // Set field
        struct.setField(statement.fieldName.lexeme(), wrappedValue);

        return null;
    }

    @Override
    public Void visitForStatement(Statement.For statement)
    {
        String varName = statement.variable.lexeme();

        // Evaluate start, end, and step
        int start = (Integer) evaluate(statement.start);
        int end = (Integer) evaluate(statement.end);
        int step = 1;

        if (statement.step != null)
        {
            step = (Integer) evaluate(statement.step);
        }

        // Determine loop direction
        boolean ascending = step > 0;

        // Execute loop
        if (ascending)
        {
            for (int i = start; i <= end; i += step)
            {
                // Update loop variable
                Value loopValue = new AtomicValue<>(i, AtomicTypes.INTEGER);
                environment.update_value(varName, loopValue);

                // Execute body
                for (Statement stmt : statement.body)
                {
                    execute(stmt);
                }
            }
        }
        else
        {
            for (int i = start; i >= end; i += step)
            {
                // Update loop variable
                Value loopValue = new AtomicValue<>(i, AtomicTypes.INTEGER);
                environment.update_value(varName, loopValue);

                // Execute body
                for (Statement stmt : statement.body)
                {
                    execute(stmt);
                }
            }
        }

        return null;
    }

    // ========================================================================
    // Continued in next part...
    // ========================================================================
    @Override
    public Void visitFunctionDeclarationStatement(Statement.FunctionDeclaration statement)
    {
        // Functions are registered in the first pass, nothing to do here
        return null;
    }

    @Override
    public Void visitMethodDeclarationStatement(Statement.MethodDeclaration statement)
    {
        // Methods are registered in the first pass, nothing to do here
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement)
    {
        Object value = evaluate(statement.value);
        throw new ReturnException(value);
    }

    @Override
    public Void visitStructDeclarationStatement(Statement.StructDeclaration statement)
    {
        // Structure declarations don't need runtime execution
        return null;
    }

    @Override
    public Void visitMethodCallStatement(Statement.MethodCall statement)
    {
        // Get method declaration
        Statement.MethodDeclaration method = methods.get(statement.name.lexeme());

        if (method == null)
        {
            throw new RuntimeError(statement.name,
                    Messages.functionNotDefined(statement.name.lexeme()));
        }

        // Evaluate arguments
        List<Object> arguments = new ArrayList<>();
        for (Expression arg : statement.arguments)
        {
            arguments.add(evaluate(arg));
        }

        // Execute method
        executeMethod(method, arguments);

        return null;
    }

    // ========================================================================
    // EXPRESSION VISITORS
    // ========================================================================

    @Override
    public Object visitBinaryExpression(Expression.Binary expression)
    {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        Type exprType = expression.getType();

        switch (expression.operator.type())
        {
            // Comparison operators
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
                return evaluateComparison(expression.operator.type(), left, right, exprType);

            case EQUAL_EQUAL:
                return isEqual(left, right);

            case DIFF:
                return !isEqual(left, right);

            // Arithmetic operators
            case MINUS:
                checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left - (int) right;
                }
                else
                {
                    return (double) left - (double) right;
                }

            case SLASH:
                checkNumberOperands(expression.operator, left, right);
                // Check for division by zero
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    int divisor = (int) right;
                    if (divisor == 0)
                    {
                        throw new RuntimeError(expression.operator, Messages.divisionByZero());
                    }
                    return (int) left / divisor;
                }
                else
                {
                    int divisor = (int) right;
                    if (divisor == 0.0)
                    {
                        throw new RuntimeError(expression.operator, Messages.divisionByZero());
                    }
                    return (double) left / divisor;
                }

            case STAR:
                checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left * (int) right;
                }
                else
                {
                    return (double) left * (double) right;
                }

            case MOD:
                checkNumberOperands(expression.operator, left, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    int divisor = (int) right;
                    if (divisor == 0)
                    {
                        throw new RuntimeError(expression.operator, Messages.divisionByZero());
                    }
                    return (int) left % divisor;
                }
                else
                {
                    double divisor = (double) right;
                    if (divisor == 0.0)
                    {
                        throw new RuntimeError(expression.operator, Messages.divisionByZero());
                    }
                    return (double) left % divisor;
                }

            case PLUS:
                // String concatenation
                if (exprType.equals(TypeFactory.STRING))
                {
                    return stringify(left) + stringify(right);
                }
                // Numeric addition
                else if (exprType.equals(TypeFactory.INTEGER))
                {
                    return (int) left + (int) right;
                }
                else
                {
                    return (double) left + (double) right;
                }
        }

        return null;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression)
    {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression)
    {
        Object right = evaluate(expression.right);
        Type exprType = expression.getType();

        switch (expression.operator.type())
        {
            case MINUS:
                checkNumberOperand(expression.operator, right);
                if (exprType.equals(TypeFactory.INTEGER))
                {
                    return -(int) right;
                }
                else
                {
                    return -(double) right;
                }

            case BANG:
            case NOT:
                return !isTruthy(right);
        }

        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression)
    {
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

        if (value instanceof AtomicValue)
        {
            return ((AtomicValue<?>) value).getValue();
        }

        return value;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression)
    {
        Object left = evaluate(expression.left);

        // Short-circuit evaluation
        if (expression.operator.type() == io.dream.scanner.TokenType.OR)
        {
            if (isTruthy(left)) return true;
        }
        else // AND
        {
            if (!isTruthy(left)) return false;
        }

        Object right = evaluate(expression.right);
        return isTruthy(right);
    }

    @Override
    public Object visitCallExpression(Expression.Call expression)
    {
        // Get function declaration
        Statement.FunctionDeclaration function = functions.get(expression.name.lexeme());

        if (function == null)
        {
            throw new RuntimeError(expression.name,
                    Messages.functionNotDefined(expression.name.lexeme()));
        }

        // Evaluate arguments
        List<Object> arguments = new ArrayList<>();
        for (Expression arg : expression.arguments)
        {
            arguments.add(evaluate(arg));
        }

        // Execute function and return result
        return executeFunction(function, arguments);
    }

    @Override
    public Object visitArrayAccessExpression(Expression.ArrayAccess expression)
    {
        // Evaluate array expression
        Object arrayObj = evaluate(expression.array);

        // Must be an ArrayValue
        if (!(arrayObj instanceof ArrayValue))
        {
            throw new RuntimeError(null, Messages.cannotIndexNonArray());
        }

        ArrayValue arrayValue = (ArrayValue) arrayObj;

        // Evaluate index
        Object indexObj = evaluate(expression.index);
        if (!(indexObj instanceof Integer))
        {
            throw new RuntimeError(null, "Array index must be an integer");
        }

        int index = (Integer) indexObj;

        // Get element
        Value element = arrayValue.get(index);

        // Unwrap if atomic value
        if (element instanceof AtomicValue)
        {
            return ((AtomicValue<?>) element).getValue();
        }

        return element;
    }

    @Override
    public Object visitFieldAccessExpression(Expression.FieldAccess expression)
    {
        // Evaluate object expression
        Object obj = evaluate(expression.object);

        // Must be a StructValue
        if (!(obj instanceof StructValue))
        {
            throw new RuntimeError(expression.field, Messages.cannotAccessFieldOfNonStruct());
        }

        StructValue structValue = (StructValue) obj;
        String fieldName = expression.field.lexeme();

        // Get field value
        Value fieldValue = structValue.getField(fieldName);

        // Unwrap if atomic value
        if (fieldValue instanceof AtomicValue)
        {
            return ((AtomicValue<?>) fieldValue).getValue();
        }

        return fieldValue;
    }

    @Override
    public Object visitArrayLiteralExpression(Expression.ArrayLiteral expression)
    {
        // Get array type from expression
        ArrayType arrayType = (ArrayType) expression.getType();
        ArrayValue arrayValue = new ArrayValue(arrayType);

        // Evaluate and store each element
        for (int i = 0; i < expression.elements.size(); i++)
        {
            Object element = evaluate(expression.elements.get(i));
            Type elementType = arrayType.getElementType();
            Value wrappedElement = wrapValue(element, elementType);
            arrayValue.set(i, wrappedElement);
        }

        return arrayValue;
    }

    /**
     * Execute a function and return its result
     */
    private Object executeFunction(Statement.FunctionDeclaration function, List<Object> arguments)
    {
        // Create new environment for function
        Environment previous = this.environment;
        this.environment = new Environment(globals);

        try
        {
            // Bind parameters
            for (int i = 0; i < function.parameters.size(); i++)
            {
                Statement.Parameter param = function.parameters.get(i);
                Object argValue = arguments.get(i);
                Value wrappedValue = wrapValue(argValue, param.type);
                environment.define(param.name.lexeme(), param.type, wrappedValue);
            }

            // initialize local variable to null;
            for (Map.Entry<String, Type> localVar : function.localVariables.entrySet())
            {
                environment.define(localVar.getKey(), localVar.getValue(), null);
            }

            // Execute function body
            for (Statement stmt : function.body)
            {
                execute(stmt);
            }
        }
        catch (ReturnException returnValue)
        {
            // Restore environment and return the value
            this.environment = previous;
            return returnValue.value;
        }
        finally
        {
            // Restore environment
            this.environment = previous;
        }

        // If no return statement was executed, return null
        // (This shouldn't happen with proper type checking)
        return null;
    }

    /**
     * Execute a method (no return value)
     */
    private void executeMethod(Statement.MethodDeclaration method, List<Object> arguments)
    {
        // Create new environment for method
        Environment previous = this.environment;
        this.environment = new Environment(globals);

        try
        {
            // Bind parameters
            for (int i = 0; i < method.parameters.size(); i++)
            {
                Statement.Parameter param = method.parameters.get(i);
                Object argValue = arguments.get(i);
                Value wrappedValue = wrapValue(argValue, param.type);
                environment.define(param.name.lexeme(), param.type, wrappedValue);
            }

            // Initialize local variables to null;
            for (Map.Entry<String, Type> localVar : method.localVariables.entrySet())
            {
                environment.define(localVar.getKey(), localVar.getValue(), null);
            }

            // Execute method body
            for (Statement stmt : method.body)
            {
                execute(stmt);
            }
        }
        finally
        {
            // Restore environment
            this.environment = previous;
        }
    }

    /**
     * Wrap a raw value into a Value object
     */
    private Value wrapValue(Object value, Type type)
    {
        if (value == null)
        {
            return type.zeroValue();
        }

        if (type.equals(TypeFactory.INTEGER))
        {
            return new AtomicValue<>((Integer) value, AtomicTypes.INTEGER);
        }
        else if (type.equals(TypeFactory.FLOATING))
        {
            return new AtomicValue<>((Double) value, AtomicTypes.FLOATING);
        }
        else if (type.equals(TypeFactory.STRING))
        {
            return new AtomicValue<>((String) value, AtomicTypes.STRING);
        }
        else if (type.equals(TypeFactory.CHAR))
        {
            return new AtomicValue<>((Character) value, AtomicTypes.CHAR);
        }
        else if (type.equals(TypeFactory.BOOLEAN))
        {
            return new AtomicValue<>((Boolean) value, AtomicTypes.BOOLEAN);
        }
        else if (value instanceof Value)
        {
            return (Value) value;
        }

        return type.zeroValue();
    }

    /**
     * Evaluate comparison operations
     */
    private Object evaluateComparison(io.dream.scanner.TokenType operator,
                                      Object left, Object right, Type type)
    {
        checkNumberOperands(null, left, right);

        int leftVal, rightVal;

        if (type.equals(TypeFactory.INTEGER))
        {
            leftVal = (int) left;
            rightVal = (int) right;
        }
        else
        {
            leftVal = (int) left;
            rightVal = (int) right;
        }

        return switch (operator) {
            case GREATER -> leftVal > rightVal;
            case GREATER_OR_EQUAL -> leftVal >= rightVal;
            case LESS -> leftVal < rightVal;
            case LESS_OR_EQUAL -> leftVal <= rightVal;
            default -> null;
        };

    }

    /**
     * Check if value is truthy
     */
    private boolean isTruthy(Object value)
    {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        return true;
    }

    /**
     * Check if two values are equal
     */
    private boolean isEqual(Object left, Object right)
    {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.equals(right);
    }

    /**
     * Check that operand is a number
     */
    private void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Number) return;
        throw new RuntimeError(operator, Messages.operandMustBeNumber());
    }

    /**
     * Check that both operands are numbers
     */
    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, Messages.operandsMustBeNumbers());
    }

    /**
     * Convert value to string for output
     */
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
