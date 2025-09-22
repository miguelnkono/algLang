package io.dream.ast;

import java.util.List;

import io.dream.scanner.Token;

abstract class Expression
{
    static class Binary extends Expression 
    {
        Binary (Expression left, Token operator, Expression right)
        {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expression left;
        final Token operator;
        final Expression right;
    }

    static class Grouping extends Expression 
    {
        Grouping (Expression expression)
        {
            this.expression = expression;
        }

        final Expression expression;
    }

    static class Unary extends Expression 
    {
        Unary (Token operator, Expression expression)
        {
            this.operator = operator;
            this.expression = expression;
        }

        final Token operator;
        final Expression expression;
    }

    static class Literal extends Expression 
    {
        Literal (Object value)
        {
            this.value = value;
        }

        final Object value;
    }

}
