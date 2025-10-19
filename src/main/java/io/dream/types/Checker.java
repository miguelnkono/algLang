package io.dream.types;

import io.dream.ast.Expression;

public class Checker implements Expression.Visitor<Void>
{
  @Override
  public Void visitBinaryExpression(Expression.Binary expression)
  {
    return null;
  }

  @Override
  public Void visitGroupingExpression(Expression.Grouping expression)
  {
    return null;
  }

  @Override
  public Void visitUnaryExpression(Expression.Unary expression)
  {
    return null;
  }

  @Override
  public Void visitLiteralExpression(Expression.Literal expression)
  {
    return null;
  }
}
