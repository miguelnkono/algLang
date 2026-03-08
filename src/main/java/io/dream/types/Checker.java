package io.dream.types;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.config.Messages;
import io.dream.error.TypeException;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete Type Checker for AlgoLang
 * Performs static type analysis on the AST
 */
public class Checker implements Expression.Visitor<Type>, Statement.Visitor<Void>
{
    // Symbol tables from parser
    private final Map<String, Type> globalSymbolTable;
    private final Map<String, FunctionType> functionTable;
    private final Map<String, List<Statement.Parameter>> methodTable;
    private final Map<String, StructType> structTable;

    // Current scope for type checking (switches between global and local)
    private Map<String, Type> currentScope;

    // Track if we're inside a function (for return statement validation)
    private boolean inFunction = false;
    private Type currentFunctionReturnType = null;

    /**
     * Constructor with just symbol table (backward compatibility)
     */
    public Checker(Map<String, Type> symbolTable)
    {
        this.globalSymbolTable = symbolTable;
        this.currentScope = symbolTable;
        this.functionTable = new HashMap<>();
        this.methodTable = new HashMap<>();
        this.structTable = new HashMap<>();
    }

    /**
     * Constructor with all symbol tables
     */
    public Checker(Map<String, Type> symbolTable,
                   Map<String, FunctionType> functionTable,
                   Map<String, List<Statement.Parameter>> methodTable,
                   Map<String, StructType> structTable)
    {
        this.globalSymbolTable = symbolTable;
        this.currentScope = symbolTable;
        this.functionTable = functionTable != null ? functionTable : new HashMap<>();
        this.methodTable = methodTable != null ? methodTable : new HashMap<>();
        this.structTable = structTable != null ? structTable : new HashMap<>();
    }

    /**
     * Check a list of statements
     */
    public void check(List<Statement> statements)
    {
        for (Statement statement : statements)
        {
            check(statement);
        }
    }

    /**
     * Check a single statement
     */
    public Statement check(Statement statement)
    {
        statement.accept(this);
        return statement;
    }

    /**
     * Check an expression and return its type
     */
    public Expression check(Expression expression)
    {
        try
        {
            Type rootType = expression.accept(this);
            expression.setType(rootType);
            return expression;
        } catch (TypeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new TypeException(Messages.typeCheckingFailed(e.getMessage()));
        }
    }

    // ========================================================================
    // STATEMENT VISITORS
    // ========================================================================

    @Override
    public Void visitExpressionStmtStatement(Statement.ExpressionStmt statement)
    {
        Type exprType = statement.expression.accept(this);
        statement.expression.setType(exprType);
        return null;
    }

    @Override
    public Void visitWriteStatement(Statement.Write statement)
    {
        Type exprType = statement.expression.accept(this);
        statement.expression.setType(exprType);
        return null;
    }

    @Override
    public Void visitReadStatement(Statement.Read statement)
    {
        // Check that the variable exists
        String varName = statement.variable.lexeme();
        if (!currentScope.containsKey(varName))
        {
            throw new TypeException(
                    Messages.variableNotDeclared(varName),
                    statement.variable
            );
        }
        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration statement)
    {
        if (statement.value != null)
        {
            Type valueType = statement.value.accept(this);
            statement.value.setType(valueType);

            Type declaredType = currentScope.get(statement.name.lexeme());
            if (declaredType == null)
            {
                throw new TypeException(
                        Messages.variableNotDeclared(statement.name.lexeme()),
                        statement.name
                );
            }

            if (!declaredType.equals(valueType))
            {
                throw new TypeException(
                        Messages.typeIncompatibility(declaredType.toString(), valueType.toString()),
                        statement.name
                );
            }
        }
        return null;
    }

    @Override
    public Void visitConstantDeclarationStatement(Statement.ConstantDeclaration statement)
    {
        // Type check the value
        Type valueType = statement.value.accept(this);
        statement.value.setType(valueType);

        // If a type was specified, check compatibility
        if (statement.type != null && !statement.type.equals(valueType))
        {
            throw new TypeException(
                    Messages.typeIncompatibility(statement.type.toString(), valueType.toString()),
                    statement.name
            );
        }

        return null;
    }

    @Override
    public Void visitAssignmentStatement(Statement.Assignment statement)
    {
        // Check if variable exists
        Type varType = currentScope.get(statement.name.lexeme());
        if (varType == null)
        {
            throw new TypeException(
                    Messages.variableNotDeclared(statement.name.lexeme()),
                    statement.name
            );
        }

        // Type check the value
        Type valueType = statement.value.accept(this);
        statement.value.setType(valueType);

        // Check type compatibility
        if (!varType.equals(valueType))
        {
            throw new TypeException(
                    Messages.typeIncompatibility(varType.toString(), valueType.toString()),
                    statement.name
            );
        }

        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement)
    {
        // Type check condition
        Type conditionType = statement.condition.accept(this);
        statement.condition.setType(conditionType);

        // Condition must be boolean
        if (!conditionType.equals(TypeFactory.BOOLEAN))
        {
            throw new TypeException(Messages.conditionMustBeBoolean());
        }

        // Type check then branch
        if (!statement.thenBranch.isEmpty())
        {
            for (Statement stmt : statement.thenBranch)
            {
                check(stmt);
            }
        }

        // Type check else branch
        if (!statement.elseBranch.isEmpty())
        {
            for (Statement stmt : statement.elseBranch)
            {
                check(stmt);
            }
        }

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement)
    {
        // Type check condition
        Type conditionType = statement.condition.accept(this);
        statement.condition.setType(conditionType);

        // Condition must be boolean
        if (!conditionType.equals(TypeFactory.BOOLEAN))
        {
            throw new TypeException(Messages.conditionMustBeBoolean());
        }

        // Type check body
        for (Statement stmt : statement.body)
        {
            check(stmt);
        }

        return null;
    }

    @Override
    public Void visitDoWhileStatement(Statement.DoWhile statement)
    {
        // Type check body first
        for (Statement stmt : statement.body)
        {
            check(stmt);
        }

        // Type check condition
        Type conditionType = statement.condition.accept(this);
        statement.condition.setType(conditionType);

        // Condition must be boolean
        if (!conditionType.equals(TypeFactory.BOOLEAN))
        {
            throw new TypeException(Messages.conditionMustBeBoolean());
        }

        return null;
    }

    @Override
    public Void visitForStatement(Statement.For statement)
    {
        // Check that loop variable exists
        String varName = statement.variable.lexeme();
        Type varType = currentScope.get(varName);

        if (varType == null)
        {
            throw new TypeException(
                    Messages.variableNotDeclared(varName),
                    statement.variable
            );
        }

        // Loop variable must be integer
        if (!varType.equals(TypeFactory.INTEGER))
        {
            throw new TypeException(Messages.loopVariableMustBeInteger());
        }

        // Type check start expression
        Type startType = statement.start.accept(this);
        statement.start.setType(startType);

        if (!startType.equals(TypeFactory.INTEGER))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("entier", startType.toString())
            );
        }

        // Type check end expression
        Type endType = statement.end.accept(this);
        statement.end.setType(endType);

        if (!endType.equals(TypeFactory.INTEGER))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("entier", endType.toString())
            );
        }

        // Type check step expression if present
        if (statement.step != null)
        {
            Type stepType = statement.step.accept(this);
            statement.step.setType(stepType);

            if (!stepType.equals(TypeFactory.INTEGER))
            {
                throw new TypeException(
                        Messages.expectedTypeButGot("entier", stepType.toString())
                );
            }
        }

        // Type check body
        for (Statement stmt : statement.body)
        {
            check(stmt);
        }

        return null;
    }

    @Override
    public Void visitArrayAssignmentStatement(Statement.ArrayAssignment statement)
    {
        // Get array type
        Type arrayType = currentScope.get(statement.arrayName.lexeme());
        if (arrayType == null)
        {
            throw new TypeException(
                    Messages.variableNotDeclared(statement.arrayName.lexeme()),
                    statement.arrayName
            );
        }

        // Must be an array
        if (!(arrayType instanceof ArrayType))
        {
            throw new TypeException(Messages.cannotIndexNonArray());
        }

        ArrayType arrType = (ArrayType) arrayType;

        // Type check index
        Type indexType = statement.index.accept(this);
        statement.index.setType(indexType);

        if (!indexType.equals(TypeFactory.INTEGER))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("entier", indexType.toString())
            );
        }

        // Type check value
        Type valueType = statement.value.accept(this);
        statement.value.setType(valueType);

        // Value must match element type
        if (!valueType.equals(arrType.getElementType()))
        {
            throw new TypeException(
                    Messages.typeIncompatibility(
                            arrType.getElementType().toString(),
                            valueType.toString()
                    )
            );
        }

        return null;
    }

    @Override
    public Void visitNestedFieldArrayAssignmentStatement(Statement.NestedFieldArrayAssignment statement)
    {
        // Get structure type
        Type structType = currentScope.get(statement.objectName.lexeme());
        if (structType == null)
        {
            throw new TypeException(
                    Messages.variableNotDeclared(statement.objectName.lexeme()),
                    statement.objectName
            );
        }

        // Must be a structure
        if (!(structType instanceof StructType))
        {
            throw new TypeException(Messages.cannotAccessFieldOfNonStruct());
        }

        StructType struct = (StructType) structType;

        // Check field exists
        String fieldName = statement.fieldName.lexeme();
        if (!struct.hasField(fieldName))
        {
            throw new TypeException(
                    Messages.fieldNotFound(struct.getName(), fieldName),
                    statement.fieldName
            );
        }

        // Get field type - must be an array
        Type fieldType = struct.getFieldType(fieldName);
        if (!(fieldType instanceof ArrayType))
        {
            throw new TypeException(
                    "Field '" + fieldName + "' is not an array"
            );
        }

        ArrayType arrayType = (ArrayType) fieldType;

        // Type check index
        Type indexType = statement.index.accept(this);
        statement.index.setType(indexType);

        if (!indexType.equals(TypeFactory.INTEGER))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("entier", indexType.toString())
            );
        }

        // Type check value
        Type valueType = statement.value.accept(this);
        statement.value.setType(valueType);

        // Value must match array element type
        if (!valueType.equals(arrayType.getElementType()))
        {
            throw new TypeException(
                    Messages.typeIncompatibility(
                            arrayType.getElementType().toString(),
                            valueType.toString()
                    )
            );
        }

        return null;
    }

    @Override
    public Void visitFieldAssignmentStatement(Statement.FieldAssignment statement)
    {
        // Get structure type
        Type structType = currentScope.get(statement.objectName.lexeme());
        if (structType == null)
        {
            throw new TypeException(
                    Messages.variableNotDeclared(statement.objectName.lexeme()),
                    statement.objectName
            );
        }

        // Must be a structure
        if (!(structType instanceof StructType))
        {
            throw new TypeException(Messages.cannotAccessFieldOfNonStruct());
        }

        StructType struct = (StructType) structType;

        // Check field exists
        String fieldName = statement.fieldName.lexeme();
        if (!struct.hasField(fieldName))
        {
            throw new TypeException(
                    Messages.fieldNotFound(struct.getName(), fieldName),
                    statement.fieldName
            );
        }

        // Type check value
        Type valueType = statement.value.accept(this);
        statement.value.setType(valueType);

        // Value must match field type
        Type fieldType = struct.getFieldType(fieldName);
        if (!valueType.equals(fieldType))
        {
            throw new TypeException(
                    Messages.typeIncompatibility(
                            fieldType.toString(),
                            valueType.toString()
                    )
            );
        }

        return null;
    }

    @Override
    public Void visitFunctionDeclarationStatement(Statement.FunctionDeclaration statement)
    {
        // Create new scope for function
        Map<String, Type> previousScope = currentScope;
        currentScope = new HashMap<>();

        // Add parameters to function scope
        for (Statement.Parameter param : statement.parameters)
        {
            currentScope.put(param.name.lexeme(), param.type);
        }

        currentScope.putAll(statement.localVariables);

        // Set function context
        boolean wasInFunction = inFunction;
        Type previousReturnType = currentFunctionReturnType;
        inFunction = true;
        currentFunctionReturnType = statement.returnType;

        // Type check function body
        for (Statement stmt : statement.body)
        {
            check(stmt);
        }

        // Restore previous context
        inFunction = wasInFunction;
        currentFunctionReturnType = previousReturnType;
        currentScope = previousScope;

        return null;
    }

    @Override
    public Void visitMethodDeclarationStatement(Statement.MethodDeclaration statement)
    {
        // Create new scope for method
        Map<String, Type> previousScope = currentScope;
        currentScope = new HashMap<>();

        // Add parameters to method scope
        for (Statement.Parameter param : statement.parameters)
        {
            currentScope.put(param.name.lexeme(), param.type);
        }

        currentScope.putAll(statement.localVariables);

        // Type check method body
        for (Statement stmt : statement.body)
        {
            check(stmt);
        }

        // Restore previous scope
        currentScope = previousScope;

        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement)
    {
        // Check that we're inside a function
        if (!inFunction)
        {
            throw new TypeException("Return statement outside of function");
        }

        // Type check return value
        Type returnType = statement.value.accept(this);
        statement.value.setType(returnType);

        // Check that return type matches function return type
        if (!returnType.equals(currentFunctionReturnType))
        {
            throw new TypeException(
                    Messages.returnTypeMismatch(
                            currentFunctionReturnType.toString(),
                            returnType.toString()
                    )
            );
        }

        return null;
    }

    @Override
    public Void visitStructDeclarationStatement(Statement.StructDeclaration statement)
    {
        // Structure declaration doesn't need runtime type checking
        // It was already validated during parsing
        return null;
    }

    @Override
    public Void visitMethodCallStatement(Statement.MethodCall statement)
    {
        // Check that method exists
        if (!methodTable.containsKey(statement.name.lexeme()))
        {
            throw new TypeException(
                    Messages.functionNotDefined(statement.name.lexeme()),
                    statement.name
            );
        }

        // Get method parameters
        List<Statement.Parameter> params = methodTable.get(statement.name.lexeme());

        // Check argument count
        if (statement.arguments.size() != params.size())
        {
            throw new TypeException(
                    Messages.wrongNumberOfArguments(params.size(), statement.arguments.size())
            );
        }

        // Type check each argument
        for (int i = 0; i < statement.arguments.size(); i++)
        {
            Expression arg = statement.arguments.get(i);
            Type argType = arg.accept(this);
            arg.setType(argType);

            Type expectedType = params.get(i).type;
            if (!argType.equals(expectedType))
            {
                throw new TypeException(
                        Messages.expectedTypeButGot(expectedType.toString(), argType.toString())
                );
            }
        }

        return null;
    }

    @Override
    public Type visitBinaryExpression(Expression.Binary expression)
    {
        // Type check operands
        Type leftType = expression.left.accept(this);
        expression.left.setType(leftType);

        Type rightType = expression.right.accept(this);
        expression.right.setType(rightType);

        Type resultType;

        switch (expression.operator.type())
        {
            // Arithmetic operators
            case PLUS:
            case MINUS:
            case STAR:
            case SLASH:
            case MOD:
                resultType = checkArithmeticOperation(expression.operator, leftType, rightType);
                break;

            // Comparison operators
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
                resultType = checkComparisonOperation(expression.operator, leftType, rightType);
                break;

            // Equality operators
            case EQUAL_EQUAL:
            case DIFF:
                resultType = checkEqualityOperation(expression.operator, leftType, rightType);
                break;

            default:
                throw new TypeException(
                        Messages.unsupportedBinaryOperator(expression.operator.type().toString()),
                        expression.operator
                );
        }

        expression.setType(resultType);
        return resultType;
    }

    /**
     * Check arithmetic operations (+, -, *, /, mod)
     */
    private Type checkArithmeticOperation(Token operator, Type leftType, Type rightType)
    {
        // Special case: + operator can concatenate strings
        if (operator.type() == TokenType.PLUS)
        {
            // String concatenation
            if (leftType.equals(TypeFactory.STRING) || rightType.equals(TypeFactory.STRING))
            {
                return TypeFactory.STRING;
            }
        }

        // Numeric operations
        if (leftType.equals(TypeFactory.INTEGER) && rightType.equals(TypeFactory.INTEGER))
        {
            return TypeFactory.INTEGER;
        }
        else if (leftType.equals(TypeFactory.FLOATING) && rightType.equals(TypeFactory.FLOATING))
        {
            return TypeFactory.FLOATING;
        }
        else if ((leftType.equals(TypeFactory.INTEGER) || leftType.equals(TypeFactory.FLOATING)) &&
                (rightType.equals(TypeFactory.INTEGER) || rightType.equals(TypeFactory.FLOATING)))
        {
            // Mixed integer and floating -> result is floating
            return TypeFactory.FLOATING;
        }
        else
        {
            String operatorName = getOperatorName(operator.type());
            throw new TypeException(
                    Messages.operatorIncompatibleWithTypes(
                            operatorName,
                            leftType.toString(),
                            rightType.toString(),
                            Messages.operatorRequirementNumbers()
                    ),
                    operator
            );
        }
    }

    /**
     * Check comparison operations (<, <=, >, >=)
     */
    private Type checkComparisonOperation(Token operator, Type leftType, Type rightType)
    {
        if ((leftType.equals(TypeFactory.INTEGER) || leftType.equals(TypeFactory.FLOATING)) &&
                (rightType.equals(TypeFactory.INTEGER) || rightType.equals(TypeFactory.FLOATING)))
        {
            return TypeFactory.BOOLEAN;
        }
        else
        {
            String operatorName = getOperatorName(operator.type());
            throw new TypeException(
                    Messages.operatorIncompatibleWithTypes(
                            operatorName,
                            leftType.toString(),
                            rightType.toString(),
                            Messages.operatorRequirementComparison()
                    ),
                    operator
            );
        }
    }

    /**
     * Check equality operations (==, !=)
     */
    private Type checkEqualityOperation(Token operator, Type leftType, Type rightType)
    {
        if (leftType.equals(rightType))
        {
            return TypeFactory.BOOLEAN;
        }
        else if ((leftType.equals(TypeFactory.INTEGER) && rightType.equals(TypeFactory.FLOATING)) ||
                (leftType.equals(TypeFactory.FLOATING) && rightType.equals(TypeFactory.INTEGER)))
        {
            return TypeFactory.BOOLEAN;
        }
        else
        {
            String operatorName = getOperatorName(operator.type());
            throw new TypeException(
                    Messages.operatorIncompatibleWithTypes(
                            operatorName,
                            leftType.toString(),
                            rightType.toString(),
                            Messages.operatorRequirementEquality()
                    ),
                    operator
            );
        }
    }

    @Override
    public Type visitGroupingExpression(Expression.Grouping expression)
    {
        Type innerType = expression.expression.accept(this);
        expression.expression.setType(innerType);
        expression.setType(innerType);
        return innerType;
    }

    @Override
    public Type visitUnaryExpression(Expression.Unary expression)
    {
        Type rightType = expression.right.accept(this);
        expression.right.setType(rightType);

        Type resultType;

        switch (expression.operator.type())
        {
            case MINUS:
                if (rightType.equals(TypeFactory.INTEGER) || rightType.equals(TypeFactory.FLOATING))
                {
                    resultType = rightType;
                }
                else
                {
                    throw new TypeException(
                            Messages.unaryOperatorIncompatible(
                                    "-",
                                    rightType.toString(),
                                    Messages.unaryMinusRequirement()
                            ),
                            expression.operator
                    );
                }
                break;

            case BANG:
            case NOT:
                if (rightType.equals(TypeFactory.BOOLEAN))
                {
                    resultType = TypeFactory.BOOLEAN;
                }
                else
                {
                    throw new TypeException(
                            Messages.unaryOperatorIncompatible(
                                    "!",
                                    rightType.toString(),
                                    Messages.unaryBangRequirement()
                            ),
                            expression.operator
                    );
                }
                break;

            default:
                throw new TypeException(
                        Messages.unsupportedUnaryOperator(expression.operator.type().toString()),
                        expression.operator
                );
        }

        expression.setType(resultType);
        return resultType;
    }

    @Override
    public Type visitLiteralExpression(Expression.Literal expression)
    {
        if (expression.value instanceof AtomicValue)
        {
            AtomicValue<?> atomicValue = (AtomicValue<?>) expression.value;
            Type type = atomicValue.getType();
            expression.setType(type);
            return type;
        }
        throw new TypeException(Messages.unsupportedLiteralType());
    }

    @Override
    public Type visitVariableExpression(Expression.Variable expression)
    {
        Type type = currentScope.get(expression.name.lexeme());
        if (type == null)
        {
            throw new TypeException(
                    Messages.variableNotDeclared(expression.name.lexeme()),
                    expression.name
            );
        }
        expression.setType(type);
        return type;
    }

    @Override
    public Type visitLogicalExpression(Expression.Logical expression)
    {
        // Type check operands
        Type leftType = expression.left.accept(this);
        expression.left.setType(leftType);

        Type rightType = expression.right.accept(this);
        expression.right.setType(rightType);

        // Both operands must be boolean
        if (!leftType.equals(TypeFactory.BOOLEAN))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("booleen", leftType.toString())
            );
        }

        if (!rightType.equals(TypeFactory.BOOLEAN))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("booleen", rightType.toString())
            );
        }

        // Result is boolean
        expression.setType(TypeFactory.BOOLEAN);
        return TypeFactory.BOOLEAN;
    }

    @Override
    public Type visitCallExpression(Expression.Call expression)
    {
        // Check if it's a function
        if (!functionTable.containsKey(expression.name.lexeme()))
        {
            throw new TypeException(
                    Messages.functionNotDefined(expression.name.lexeme()),
                    expression.name
            );
        }

        // Get function type
        FunctionType funcType = functionTable.get(expression.name.lexeme());

        // Check argument count
        if (expression.arguments.size() != funcType.getArity())
        {
            throw new TypeException(
                    Messages.wrongNumberOfArguments(funcType.getArity(), expression.arguments.size())
            );
        }

        // Type check each argument
        List<Type> paramTypes = funcType.getParameterTypes();
        for (int i = 0; i < expression.arguments.size(); i++)
        {
            Expression arg = expression.arguments.get(i);
            Type argType = arg.accept(this);
            arg.setType(argType);

            Type expectedType = paramTypes.get(i);
            if (!argType.equals(expectedType))
            {
                throw new TypeException(
                        Messages.expectedTypeButGot(expectedType.toString(), argType.toString())
                );
            }
        }

        // Return function's return type
        Type returnType = funcType.getReturnType();
        expression.setType(returnType);
        return returnType;
    }

    @Override
    public Type visitArrayAccessExpression(Expression.ArrayAccess expression)
    {
        // Type check the array expression
        Type arrayType = expression.array.accept(this);
        expression.array.setType(arrayType);

        // Must be an array type
        if (!(arrayType instanceof ArrayType))
        {
            throw new TypeException(Messages.cannotIndexNonArray());
        }

        ArrayType arrType = (ArrayType) arrayType;

        // Type check the index
        Type indexType = expression.index.accept(this);
        expression.index.setType(indexType);

        // Index must be integer
        if (!indexType.equals(TypeFactory.INTEGER))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot("entier", indexType.toString())
            );
        }

        // Return element type
        Type elementType = arrType.getElementType();
        expression.setType(elementType);
        return elementType;
    }

    @Override
    public Type visitFieldAccessExpression(Expression.FieldAccess expression)
    {
        // Type check the object expression
        Type objectType = expression.object.accept(this);
        expression.object.setType(objectType);

        // Must be a structure type
        if (!(objectType instanceof StructType))
        {
            throw new TypeException(Messages.cannotAccessFieldOfNonStruct());
        }

        StructType structType = (StructType) objectType;

        // Check if field exists
        String fieldName = expression.field.lexeme();
        if (!structType.hasField(fieldName))
        {
            throw new TypeException(
                    Messages.fieldNotFound(structType.getName(), fieldName),
                    expression.field
            );
        }

        // Return field type
        Type fieldType = structType.getFieldType(fieldName);
        expression.setType(fieldType);
        return fieldType;
    }

    @Override
    public Type visitArrayLiteralExpression(Expression.ArrayLiteral expression)
    {
        // Type check all elements
        if (expression.elements.isEmpty())
        {
            throw new TypeException("Array literal cannot be empty");
        }

        // Get type of first element
        Type firstType = expression.elements.get(0).accept(this);
        expression.elements.get(0).setType(firstType);

        // All elements must have same type
        for (int i = 1; i < expression.elements.size(); i++)
        {
            Expression elem = expression.elements.get(i);
            Type elemType = elem.accept(this);
            elem.setType(elemType);

            if (!elemType.equals(firstType))
            {
                throw new TypeException(
                        "Array elements must all have the same type. Expected " +
                                firstType.toString() + " but got " + elemType.toString()
                );
            }
        }

        // Create array type
        int size = expression.elements.size();
        ArrayType arrayType = new ArrayType(firstType, 0, size - 1);
        expression.setType(arrayType);
        return arrayType;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get operator name for error messages
     */
    private String getOperatorName(TokenType tokenType)
    {
        return switch (tokenType)
        {
            case PLUS -> "+";
            case MINUS -> "-";
            case STAR -> "*";
            case SLASH -> "/";
            case MOD -> "mod";
            case GREATER -> ">";
            case GREATER_OR_EQUAL -> ">=";
            case LESS -> "<";
            case LESS_OR_EQUAL -> "<=";
            case EQUAL_EQUAL -> "==";
            case DIFF -> "!=";
            case BANG -> "!";
            case AND -> "et";
            case OR -> "ou";
            case NOT -> "non";
            default -> tokenType.toString().toLowerCase();
        };
    }

    /**
     * Check if expression has been type checked
     */
    public boolean isTyped(Expression expression)
    {
        return expression.getType() != null;
    }

    /**
     * Get type of an expression (must be type checked first)
     */
    public Type getType(Expression expression)
    {
        if (expression.getType() == null)
        {
            throw new TypeException(Messages.expressionNotTypeChecked());
        }
        return expression.getType();
    }

    /**
     * Validate that an expression has a specific type
     */
    public void validateType(Expression expression, Type expectedType)
    {
        Type actualType = check(expression).getType();
        if (!actualType.equals(expectedType))
        {
            throw new TypeException(
                    Messages.expectedTypeButGot(expectedType.toString(), actualType.toString())
            );
        }
    }

    /**
     * Validate that an expression has one of several allowed types
     */
    public void validateType(Expression expression, Type... expectedTypes)
    {
        Type actualType = check(expression).getType();
        for (Type expectedType : expectedTypes)
        {
            if (actualType.equals(expectedType))
            {
                return;
            }
        }
        throw new TypeException(
                Messages.typeNotAllowed(actualType.toString(), java.util.Arrays.toString(expectedTypes))
        );
    }
}
