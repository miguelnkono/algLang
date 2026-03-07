package io.dream;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private Interpreter interpreter;
    private Map<String, Type> symbolTable;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        symbolTable = new HashMap<>();
        interpreter = new Interpreter(symbolTable);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        interpreter = null;
        symbolTable = null;
    }

    @Test
    void testWriteStatement() {
        // Arrange
        Expression.Literal literal = new Expression.Literal(
                new AtomicValue<>("Hello World", AtomicTypes.STRING));
        literal.setType(TypeFactory.STRING);
        Statement.Write writeStmt = new Statement.Write(literal);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("Hello World\n", outContent.toString());
    }

    @Test
    void testVariableDeclarationAndAssignment() {
        // Arrange
        symbolTable.put("x", TypeFactory.INTEGER);

        Expression.Literal value = new Expression.Literal(
                new AtomicValue<>(42, AtomicTypes.INTEGER));
        value.setType(TypeFactory.INTEGER);

        Token varName = new Token(TokenType.IDENTIFIER, "x", null, 1);
        Statement.Assignment assignment = new Statement.Assignment(varName, value);

        Expression.Variable varExpr = new Expression.Variable(varName);
        varExpr.setType(TypeFactory.INTEGER);
        Statement.Write writeStmt = new Statement.Write(varExpr);

        // Act
        interpreter.interpret(List.of(assignment, writeStmt));

        // Assert
        assertEquals("42\n", outContent.toString());
    }

    @Test
    void testBinaryExpression_Addition() {
        // Arrange
        Expression.Literal left = new Expression.Literal(
                new AtomicValue<>(10, AtomicTypes.INTEGER));
        left.setType(TypeFactory.INTEGER);

        Expression.Literal right = new Expression.Literal(
                new AtomicValue<>(5, AtomicTypes.INTEGER));
        right.setType(TypeFactory.INTEGER);

        Token plus = new Token(TokenType.PLUS, "+", null, 1);
        Expression.Binary addition = new Expression.Binary(left, plus, right);
        addition.setType(TypeFactory.INTEGER);

        Statement.Write writeStmt = new Statement.Write(addition);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("15\n", outContent.toString());
    }

    @Test
    void testBinaryExpression_Subtraction() {
        // Arrange
        Expression.Literal left = new Expression.Literal(
                new AtomicValue<>(20, AtomicTypes.INTEGER));
        left.setType(TypeFactory.INTEGER);

        Expression.Literal right = new Expression.Literal(
                new AtomicValue<>(8, AtomicTypes.INTEGER));
        right.setType(TypeFactory.INTEGER);

        Token minus = new Token(TokenType.MINUS, "-", null, 1);
        Expression.Binary subtraction = new Expression.Binary(left, minus, right);
        subtraction.setType(TypeFactory.INTEGER);

        Statement.Write writeStmt = new Statement.Write(subtraction);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("12\n", outContent.toString());
    }

    @Test
    void testBinaryExpression_Multiplication() {
        // Arrange
        Expression.Literal left = new Expression.Literal(
                new AtomicValue<>(6, AtomicTypes.INTEGER));
        left.setType(TypeFactory.INTEGER);

        Expression.Literal right = new Expression.Literal(
                new AtomicValue<>(7, AtomicTypes.INTEGER));
        right.setType(TypeFactory.INTEGER);

        Token star = new Token(TokenType.STAR, "*", null, 1);
        Expression.Binary multiplication = new Expression.Binary(left, star, right);
        multiplication.setType(TypeFactory.INTEGER);

        Statement.Write writeStmt = new Statement.Write(multiplication);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("42\n", outContent.toString());
    }

    @Test
    void testBinaryExpression_StringConcatenation() {
        // Arrange
        Expression.Literal left = new Expression.Literal(
                new AtomicValue<>("Hello ", AtomicTypes.STRING));
        left.setType(TypeFactory.STRING);

        Expression.Literal right = new Expression.Literal(
                new AtomicValue<>("World", AtomicTypes.STRING));
        right.setType(TypeFactory.STRING);

        Token plus = new Token(TokenType.PLUS, "+", null, 1);
        Expression.Binary concatenation = new Expression.Binary(left, plus, right);
        concatenation.setType(TypeFactory.STRING);

        Statement.Write writeStmt = new Statement.Write(concatenation);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("Hello World\n", outContent.toString());
    }

    @Test
    void testUnaryExpression_Negation() {
        // Arrange
        Expression.Literal operand = new Expression.Literal(
                new AtomicValue<>(10, AtomicTypes.INTEGER));
        operand.setType(TypeFactory.INTEGER);

        Token minus = new Token(TokenType.MINUS, "-", null, 1);
        Expression.Unary negation = new Expression.Unary(minus, operand);
        negation.setType(TypeFactory.INTEGER);

        Statement.Write writeStmt = new Statement.Write(negation);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("-10\n", outContent.toString());
    }

    @Test
    void testIfStatement_TrueBranch() {
        // Arrange
        Expression.Literal condition = new Expression.Literal(
                new AtomicValue<>(true, AtomicTypes.BOOLEAN));
        condition.setType(TypeFactory.BOOLEAN);

        Expression.Literal thenValue = new Expression.Literal(
                new AtomicValue<>("True branch", AtomicTypes.STRING));
        thenValue.setType(TypeFactory.STRING);
        Statement.Write thenStmt = new Statement.Write(thenValue);

        Statement.If ifStmt = new Statement.If(condition, List.of(thenStmt), List.of());

        // Act
        interpreter.interpret(List.of(ifStmt));

        // Assert
        assertEquals("True branch\n", outContent.toString());
    }

    @Test
    void testIfStatement_FalseBranch() {
        // Arrange
        Expression.Literal condition = new Expression.Literal(
                new AtomicValue<>(false, AtomicTypes.BOOLEAN));
        condition.setType(TypeFactory.BOOLEAN);

        Expression.Literal elseValue = new Expression.Literal(
                new AtomicValue<>("False branch", AtomicTypes.STRING));
        elseValue.setType(TypeFactory.STRING);
        Statement.Write elseStmt = new Statement.Write(elseValue);

        Statement.If ifStmt = new Statement.If(condition, List.of(), List.of(elseStmt));

        // Act
        interpreter.interpret(List.of(ifStmt));

        // Assert
        assertEquals("False branch\n", outContent.toString());
    }

    @Test
    void testGroupingExpression() {
        // Arrange
        Expression.Literal inner = new Expression.Literal(
                new AtomicValue<>(42, AtomicTypes.INTEGER));
        inner.setType(TypeFactory.INTEGER);

        Expression.Grouping grouping = new Expression.Grouping(inner);
        grouping.setType(TypeFactory.INTEGER);

        Statement.Write writeStmt = new Statement.Write(grouping);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("42\n", outContent.toString());
    }

    @Test
    void testComplexExpression() {
        // Test: (10 + 5) * 2 = 30
        Expression.Literal ten = new Expression.Literal(
                new AtomicValue<>(10, AtomicTypes.INTEGER));
        ten.setType(TypeFactory.INTEGER);

        Expression.Literal five = new Expression.Literal(
                new AtomicValue<>(5, AtomicTypes.INTEGER));
        five.setType(TypeFactory.INTEGER);

        Token plus = new Token(TokenType.PLUS, "+", null, 1);
        Expression.Binary addition = new Expression.Binary(ten, plus, five);
        addition.setType(TypeFactory.INTEGER);

        Expression.Grouping grouped = new Expression.Grouping(addition);
        grouped.setType(TypeFactory.INTEGER);

        Expression.Literal two = new Expression.Literal(
                new AtomicValue<>(2, AtomicTypes.INTEGER));
        two.setType(TypeFactory.INTEGER);

        Token star = new Token(TokenType.STAR, "*", null, 1);
        Expression.Binary multiplication = new Expression.Binary(grouped, star, two);
        multiplication.setType(TypeFactory.INTEGER);

        Statement.Write writeStmt = new Statement.Write(multiplication);

        // Act
        interpreter.interpret(List.of(writeStmt));

        // Assert
        assertEquals("30\n", outContent.toString());
    }
}
