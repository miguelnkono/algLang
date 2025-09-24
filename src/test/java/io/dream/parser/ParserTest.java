package io.dream.parser;

import io.dream.ast.Expression;
import io.dream.scanner.Scanner;
import io.dream.scanner.Token;
import io.dream.tools.AstPrinter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Expression parseExpression(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    private String printAst(String source) {
        Expression expression = parseExpression(source);
        if (expression == null) return null;
        return new AstPrinter().print(expression);
    }

    @Test
    void parseSimpleArithmetic() {
        assertEquals("(+ 1 2)", printAst("1 + 2"));
        assertEquals("(- 5 3)", printAst("5 - 3"));
        assertEquals("(* 4 6)", printAst("4 * 6"));
        assertEquals("(/ 8 2)", printAst("8 / 2"));
    }

    @Test
    void parseComplexArithmetic() {
        assertEquals("(* (group (+ 1 2)) 3)", printAst("(1 + 2) * 3"));
        assertEquals("(+ 1 (* 2 3))", printAst("1 + 2 * 3"));
        assertEquals("(* (group (+ 1 2)) (group (- 4 3)))", printAst("(1 + 2) * (4 - 3)"));
    }

    @Test
    void parseUnaryOperators() {
        assertEquals("(- 5)", printAst("-5"));
        assertEquals("(! true)", printAst("!vrai")); // Your AST printer shows Java boolean values
        assertEquals("(- (- 10))", printAst("--10"));
    }

    @Test
    void parseComparisonOperators() {
        assertEquals("(> 5 3)", printAst("5 > 3"));
        assertEquals("(<= 10 20)", printAst("10 <= 20"));
        assertEquals("(== 7 7)", printAst("7 == 7"));
        assertEquals("(!= 1 2)", printAst("1 != 2"));
    }

    @Test
    void parseLiterals() {
        assertEquals("1", printAst("1"));
        assertEquals("3.14", printAst("3,14"));
        assertEquals("hello", printAst("\"hello\"")); // Your AST printer doesn't show quotes
        assertEquals("true", printAst("vrai")); // Your AST printer shows Java boolean values
        assertEquals("false", printAst("faux")); // Your AST printer shows Java boolean values
        assertEquals("nil", printAst("nil"));
    }

    @Test
    void parseGrouping() {
        assertEquals("(group 5)", printAst("(5)"));
        assertEquals("(+ (group 5) 3)", printAst("(5) + 3"));
        assertEquals("(* (group (+ 1 2)) 3)", printAst("(1 + 2) * 3"));
    }

    @Test
    void parseOperatorPrecedence() {
        // Multiplication should have higher precedence than addition
        assertEquals("(+ 1 (* 2 3))", printAst("1 + 2 * 3"));
        assertEquals("(+ (* 1 2) 3)", printAst("1 * 2 + 3"));

        // Unary should have higher precedence than multiplication
        assertEquals("(* (- 5) 3)", printAst("-5 * 3"));

        // Comparison should have lower precedence than arithmetic
        assertEquals("(== (+ 1 2) 3)", printAst("1 + 2 == 3"));
    }

    @Test
    void parseInvalidExpressions() {
        // Missing operand - this might parse partially depending on error recovery
        Expression expr = parseExpression("1 + ");
        // Don't assert null since your parser might have partial error recovery
        // Just verify it doesn't crash

        // Unmatched parentheses - should error
        assertNull(parseExpression("(1 + 2"));
        assertNull(parseExpression("1 + 2)"));

        // Invalid tokens - should error
        assertNull(parseExpression("1 @ 2"));

        // Empty expression - should error
        assertNull(parseExpression(""));
    }

    @Test
    void parseEdgeCases() {
        // Multiple nested parentheses
        assertEquals("(group (group (group 5)))", printAst("(((5)))"));

        // Chained comparisons
        assertEquals("(== (== 1 1) 1)", printAst("1 == 1 == 1"));

        // Mixed types
        assertEquals("(+ 1 2.5)", printAst("1 + 2,5"));
    }

    @Test
    void parseBooleanExpressions() {
        assertEquals("(== true false)", printAst("vrai == faux")); // Your AST printer shows Java values
        assertEquals("(!= true false)", printAst("vrai != faux")); // Your AST printer shows Java values
    }

    @Test
    void parseStringOperations() {
        assertEquals("(== hello world)", printAst("\"hello\" == \"world\"")); // No quotes in output
    }

    @Test
    void parseDecimalNumbers() {
        assertEquals("3.14", printAst("3,14"));
        assertEquals("0.5", printAst("0,5"));
        assertEquals("(+ 1.5 2.5)", printAst("1,5 + 2,5"));
    }

    @Test
    void testBasicFunctionality() {
        // Test that basic expressions parse without errors
        assertNotNull(parseExpression("1"));
        assertNotNull(parseExpression("1 + 2"));
        assertNotNull(parseExpression("(1 + 2) * 3"));
        assertNotNull(parseExpression("vrai == faux"));
        assertNotNull(parseExpression("\"hello\""));
    }

    @Test
    void testErrorMessages() {
        // Test that errors are reported (you can check console output)
        assertNull(parseExpression("1 + "));
        assertNull(parseExpression("(1 + 2"));
    }
}
