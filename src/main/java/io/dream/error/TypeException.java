package io.dream.error;

import io.dream.config.Messages;
import io.dream.scanner.Token;

public class TypeException extends RuntimeException
{
  public TypeException(String message)
  {
    super(message);
  }

  public TypeException(String message, Token token)
  {
    super(message + Messages.atLine(token.line()));
  }
}
