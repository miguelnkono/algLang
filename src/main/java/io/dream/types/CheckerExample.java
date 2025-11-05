package io.dream.types;

import io.dream.ast.Expr;
import io.dream.error.TypeException;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;

public class CheckerExample
{
    public static void main(String[] args)
    {
        Checker checker = new Checker();

        // Example 1: Successful type checking
        try
        {
            // Create expression: (10 + 20) * 2
            Expr.Literal ten = new Expr.Literal(
                    new AtomicValue<Integer>(10, AtomicTypes.INTEGER));
            Expr.Literal twenty = new Expr.Literal(
                    new AtomicValue<Integer>(20, AtomicTypes.INTEGER));
            Expr.Literal two = new Expr.Literal(
                    new AtomicValue<Integer>(2, AtomicTypes.INTEGER));

            Token plus = new Token(TokenType.PLUS, "+", null, 1);
            Token star = new Token(TokenType.STAR, "*", null, 1);

            Expr.Binary addition = new Expr.Binary(ten, plus, twenty);
            Expr.Grouping grouping = new Expr.Grouping(addition);
            Expr.Binary multiplication = new Expr.Binary(grouping, star, two);

            // Type check the entire expression - returns the SAME tree with types added
            Expr typedExpr = checker.check(multiplication);

            System.out.println("Type checking successful!");
            System.out.println("Root type: " + typedExpr.getType()); // "entier"
            System.out.println("Is this the same object? " + (typedExpr == multiplication)); // true

            // You can now access types at every level
            Expr.Binary multBinary = (Expr.Binary) typedExpr;
            Expr.Grouping multGrouping = (Expr.Grouping) multBinary.left;
            Expr.Binary addBinary = (Expr.Binary) multGrouping.expression;

            System.out.println("Multiplication type: " + multBinary.getType());
            System.out.println("Grouping type: " + multGrouping.getType());
            System.out.println("Addition type: " + addBinary.getType());
            System.out.println("Left operand type: " + addBinary.left.getType());
            System.out.println("Right operand type: " + addBinary.right.getType());

        } catch (TypeException e)
        {
            System.out.println("Type error: " + e.getMessage());
        }

        // Example 2: Type error
        try
        {
            Expr.Literal number = new Expr.Literal(
                    new AtomicValue<Integer>(42, AtomicTypes.INTEGER));
            Expr.Literal bool = new Expr.Literal(
                    new AtomicValue<Boolean>(true, AtomicTypes.BOOLEAN));

            Token plus = new Token(TokenType.PLUS, "+", null, 1);
            Expr.Binary invalid = new Expr.Binary(number, plus, bool);

            Expr result = checker.check(invalid); // This will throw TypeException

        } catch (TypeException e)
        {
            System.out.println("Caught expected type error: " + e.getMessage());
        }
    }

    // Utility method to print the expression tree with types
    private static void printExpressionWithTypes(Expr expr, int indent)
    {
        String spaces = "  ".repeat(indent);

        if (expr instanceof Expr.Binary)
        {
            Expr.Binary binary = (Expr.Binary) expr;
            System.out.println(spaces + "Binary[" + binary.operator.lexeme() + "] : " + binary.getType());
            printExpressionWithTypes(binary.left, indent + 1);
            printExpressionWithTypes(binary.right, indent + 1);
        } else if (expr instanceof Expr.Unary)
        {
            Expr.Unary unary = (Expr.Unary) expr;
            System.out.println(spaces + "Unary[" + unary.operator.lexeme() + "] : " + unary.getType());
            printExpressionWithTypes(unary.right, indent + 1);
        } else if (expr instanceof Expr.Grouping)
        {
            Expr.Grouping grouping = (Expr.Grouping) expr;
            System.out.println(spaces + "Grouping : " + grouping.getType());
            printExpressionWithTypes(grouping.expression, indent + 1);
        } else if (expr instanceof Expr.Literal)
        {
            Expr.Literal literal = (Expr.Literal) expr;
            System.out.println(spaces + "Literal[" + literal.value + "] : " + literal.getType());
        }
    }
}