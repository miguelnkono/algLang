package io.dream;

import io.dream.ast.Expression;
import io.dream.error.RuntimeError;
import io.dream.scanner.Token;
import io.dream.types.*;

public class Interpreter implements Expression.Visitor<Object>
{
  private final Checker typeChecker;

  public Interpreter() {
    this.typeChecker = new Checker();
  }

  public void interpret(Expression expression)
  {
    try
    {
      // First, type check the expression
      Expression typedExpression = typeChecker.check(expression);

      // Then evaluate it
      Object result = this.evaluate(typedExpression);
      System.out.println(this.stringify(result));
    }
    catch (RuntimeError re)
    {
      Main.runtimeError(re);
    }
  }

  private Object evaluate(Expression expression)
  {
    return expression.accept(this);
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
        if (exprType.equals(TypeFactory.INTEGER)) {
          int leftVal = (int) left;
          int rightVal = (int) right;
          return getComparison(expression, (double)leftVal, (double)rightVal);
        } else {
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
        if (exprType.equals(TypeFactory.INTEGER)) {
          return (int) left - (int) right;
        } else {
          return (double) left - (double) right;
        }

      case SLASH:
        this.checkNumberOperands(expression.operator, left, right);
        if (exprType.equals(TypeFactory.INTEGER)) {
          return (int) left / (int) right;
        } else {
          return (double) left / (double) right;
        }

      case STAR:
        this.checkNumberOperands(expression.operator, left, right);
        if (exprType.equals(TypeFactory.INTEGER)) {
          return (int) left * (int) right;
        } else {
          return (double) left * (double) right;
        }

      case PLUS:
        if (exprType.equals(TypeFactory.STRING)) {
          return (String) left + (String) right;
        } else if (exprType.equals(TypeFactory.INTEGER)) {
          return (int) left + (int) right;
        } else {
          return (double) left + (double) right;
        }
    }

    return null;
  }

  private static Object getComparison(Expression.Binary expression, double leftVal, double rightVal)
  {
    return switch (expression.operator.type()) {
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
        if (exprType.equals(TypeFactory.INTEGER)) {
          return -(int) right;
        } else {
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
    if (expression.value instanceof AtomicValue) {
      AtomicValue<?> atomicValue = (AtomicValue<?>) expression.value;
      return atomicValue.getValue();
    }
    return expression.value;
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
      throw new RuntimeError(operator, "L'opérateur doit être un nombre");
    }
  }

  private void checkNumberOperands(Token operator, Object left, Object right)
  {
    if (left instanceof Number && right instanceof Number)
    {
      return;
    }
    throw new RuntimeError(operator, "Les opérateurs doivent tous être des nombres.");
  }
}