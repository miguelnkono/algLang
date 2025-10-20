package io.dream.types;

import io.dream.ast.Expression;
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
            Expression.Literal ten = new Expression.Literal(
                    new AtomicValue<Integer>(10, AtomicTypes.INTEGER));
            Expression.Literal twenty = new Expression.Literal(
                    new AtomicValue<Integer>(20, AtomicTypes.INTEGER));
            Expression.Literal two = new Expression.Literal(
                    new AtomicValue<Integer>(2, AtomicTypes.INTEGER));

            Token plus = new Token(TokenType.PLUS, "+", null, 1);
            Token star = new Token(TokenType.STAR, "*", null, 1);

            Expression.Binary addition = new Expression.Binary(ten, plus, twenty);
            Expression.Grouping grouping = new Expression.Grouping(addition);
            Expression.Binary multiplication = new Expression.Binary(grouping, star, two);

            // Type check the entire expression - returns the SAME tree with types added
            Expression typedExpr = checker.check(multiplication);

            System.out.println("Type checking successful!");
            System.out.println("Root type: " + typedExpr.getType()); // "entier"
            System.out.println("Is this the same object? " + (typedExpr == multiplication)); // true

            // You can now access types at every level
            Expression.Binary multBinary = (Expression.Binary) typedExpr;
            Expression.Grouping multGrouping = (Expression.Grouping) multBinary.left;
            Expression.Binary addBinary = (Expression.Binary) multGrouping.expression;

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
            Expression.Literal number = new Expression.Literal(
                    new AtomicValue<Integer>(42, AtomicTypes.INTEGER));
            Expression.Literal bool = new Expression.Literal(
                    new AtomicValue<Boolean>(true, AtomicTypes.BOOLEAN));

            Token plus = new Token(TokenType.PLUS, "+", null, 1);
            Expression.Binary invalid = new Expression.Binary(number, plus, bool);

            Expression result = checker.check(invalid); // This will throw TypeException

        } catch (TypeException e)
        {
            System.out.println("Caught expected type error: " + e.getMessage());
        }
    }

    // Utility method to print the expression tree with types
    private static void printExpressionWithTypes(Expression expr, int indent)
    {
        String spaces = "  ".repeat(indent);

        if (expr instanceof Expression.Binary)
        {
            Expression.Binary binary = (Expression.Binary) expr;
            System.out.println(spaces + "Binary[" + binary.operator.lexeme() + "] : " + binary.getType());
            printExpressionWithTypes(binary.left, indent + 1);
            printExpressionWithTypes(binary.right, indent + 1);
        } else if (expr instanceof Expression.Unary)
        {
            Expression.Unary unary = (Expression.Unary) expr;
            System.out.println(spaces + "Unary[" + unary.operator.lexeme() + "] : " + unary.getType());
            printExpressionWithTypes(unary.right, indent + 1);
        } else if (expr instanceof Expression.Grouping)
        {
            Expression.Grouping grouping = (Expression.Grouping) expr;
            System.out.println(spaces + "Grouping : " + grouping.getType());
            printExpressionWithTypes(grouping.expression, indent + 1);
        } else if (expr instanceof Expression.Literal)
        {
            Expression.Literal literal = (Expression.Literal) expr;
            System.out.println(spaces + "Literal[" + literal.value + "] : " + literal.getType());
        }
    }
}