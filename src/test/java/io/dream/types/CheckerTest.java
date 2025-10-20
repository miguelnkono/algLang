package io.dream.types;

import io.dream.ast.Expression;
import io.dream.error.TypeException;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckerTest
{
  private Checker checker;

  @BeforeEach
  void setUp()
  {
    checker = new Checker();
  }

  @AfterEach
  void tearDown()
  {
    checker = null;
  }

  @Test
  void check_ExpressionValide_RetourneExpressionTypee()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));

    // Act
    Expression result = checker.check(literal);

    // Assert
    assertNotNull(result);
    assertEquals(TypeFactory.INTEGER, result.getType());
    assertTrue(checker.isTyped(result));
  }

  @Test
  void check_ExpressionInvalide_LanceTypeException()
  {
    // Arrange
    Expression.Literal entier = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    Expression.Literal bool = new Expression.Literal(
        new AtomicValue<>(true, AtomicTypes.BOOLEAN));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expression.Binary expressionInvalide = new Expression.Binary(entier, plus, bool);

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.check(expressionInvalide);
    });

    assertTrue(exception.getMessage().contains("incompatible"));
    assertTrue(exception.getMessage().contains("entier"));
    assertTrue(exception.getMessage().contains("booléen"));
  }

  @Test
  void testCheck_AvecRecuperationErreur_RetourneExpressionParDefaut()
  {
    // Arrange
    Expression.Literal defaut = new Expression.Literal(
        new AtomicValue<>(0, AtomicTypes.INTEGER));
    Expression expressionInvalide = null; // Expression invalide

    // Act
    Expression result = checker.check(expressionInvalide, defaut);

    // Assert
    assertEquals(defaut, result);
  }

  @Test
  void visitBinaryExpression_AdditionEntiers_RetourneTypeEntier()
  {
    // Arrange
    Expression.Literal gauche = new Expression.Literal(
        new AtomicValue<>(10, AtomicTypes.INTEGER));
    Expression.Literal droite = new Expression.Literal(
        new AtomicValue<>(20, AtomicTypes.INTEGER));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expression.Binary addition = new Expression.Binary(gauche, plus, droite);

    // Act
    Type result = checker.visitBinaryExpression(addition);

    // Assert
    assertEquals(TypeFactory.INTEGER, result);
    assertEquals(TypeFactory.INTEGER, gauche.getType());
    assertEquals(TypeFactory.INTEGER, droite.getType());
  }

  @Test
  void visitBinaryExpression_AdditionChaines_RetourneTypeChaine()
  {
    // Arrange
    Expression.Literal gauche = new Expression.Literal(
        new AtomicValue<>("Bonjour ", AtomicTypes.STRING));
    Expression.Literal droite = new Expression.Literal(
        new AtomicValue<>("Monde", AtomicTypes.STRING));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expression.Binary concatenation = new Expression.Binary(gauche, plus, droite);

    // Act
    Type result = checker.visitBinaryExpression(concatenation);

    // Assert
    assertEquals(TypeFactory.STRING, result);
  }

  @Test
  void visitBinaryExpression_ComparaisonNombres_RetourneTypeBooleen()
  {
    // Arrange
    Expression.Literal gauche = new Expression.Literal(
        new AtomicValue<>(10, AtomicTypes.INTEGER));
    Expression.Literal droite = new Expression.Literal(
        new AtomicValue<>(5, AtomicTypes.INTEGER));
    Token plusGrand = new Token(TokenType.GREATER, ">", null, 1);
    Expression.Binary comparaison = new Expression.Binary(gauche, plusGrand, droite);

    // Act
    Type result = checker.visitBinaryExpression(comparaison);

    // Assert
    assertEquals(TypeFactory.BOOLEAN, result);
  }

  @Test
  void visitBinaryExpression_AdditionTypesIncompatibles_LanceException()
  {
    // Arrange
    Expression.Literal entier = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    Expression.Literal chaine = new Expression.Literal(
        new AtomicValue<>("texte", AtomicTypes.STRING));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expression.Binary additionInvalide = new Expression.Binary(entier, plus, chaine);

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.visitBinaryExpression(additionInvalide);
    });

    assertTrue(exception.getMessage().contains("incompatible"));
    assertTrue(exception.getMessage().contains("entier"));
    assertTrue(exception.getMessage().contains("chaîne"));
  }

  @Test
  void visitGroupingExpression_ExpressionInterne_RetourneMemeType()
  {
    // Arrange
    Expression.Literal interne = new Expression.Literal(
        new AtomicValue<>(3.14, AtomicTypes.FLOATING));
    Expression.Grouping groupement = new Expression.Grouping(interne);

    // Act
    Type result = checker.visitGroupingExpression(groupement);

    // Assert
    assertEquals(TypeFactory.FLOATING, result);
    assertEquals(TypeFactory.FLOATING, interne.getType());
  }

  @Test
  void visitUnaryExpression_MoinsUnaireEntier_RetourneTypeEntier()
  {
    // Arrange
    Expression.Literal operand = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    Token moins = new Token(TokenType.MINUS, "-", null, 1);
    Expression.Unary moinsUnaire = new Expression.Unary(moins, operand);

    // Act
    Type result = checker.visitUnaryExpression(moinsUnaire);

    // Assert
    assertEquals(TypeFactory.INTEGER, result);
    assertEquals(TypeFactory.INTEGER, operand.getType());
  }

  @Test
  void visitUnaryExpression_NotBooleen_RetourneTypeBooleen()
  {
    // Arrange
    Expression.Literal operand = new Expression.Literal(
        new AtomicValue<>(true, AtomicTypes.BOOLEAN));
    Token not = new Token(TokenType.BANG, "!", null, 1);
    Expression.Unary notExpression = new Expression.Unary(not, operand);

    // Act
    Type result = checker.visitUnaryExpression(notExpression);

    // Assert
    assertEquals(TypeFactory.BOOLEAN, result);
  }

  @Test
  void visitUnaryExpression_MoinsUnaireNonNumerique_LanceException()
  {
    // Arrange
    Expression.Literal operand = new Expression.Literal(
        new AtomicValue<>("texte", AtomicTypes.STRING));
    Token moins = new Token(TokenType.MINUS, "-", null, 1);
    Expression.Unary moinsInvalide = new Expression.Unary(moins, operand);

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.visitUnaryExpression(moinsInvalide);
    });

    assertTrue(exception.getMessage().contains("incompatible"));
    assertTrue(exception.getMessage().contains("chaîne"));
  }

  @Test
  void visitLiteralExpression_Entier_RetourneTypeEntier()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(123, AtomicTypes.INTEGER));

    // Act
    Type result = checker.visitLiteralExpression(literal);

    // Assert
    assertEquals(TypeFactory.INTEGER, result);
  }

  @Test
  void visitLiteralExpression_Booleen_RetourneTypeBooleen()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(false, AtomicTypes.BOOLEAN));

    // Act
    Type result = checker.visitLiteralExpression(literal);

    // Assert
    assertEquals(TypeFactory.BOOLEAN, result);
  }

  @Test
  void isTyped_ExpressionTypee_RetourneVrai()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    checker.check(literal);

    // Act
    boolean result = checker.isTyped(literal);

    // Assert
    assertTrue(result);
  }

  @Test
  void isTyped_ExpressionNonTypee_RetourneFaux()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));

    // Act
    boolean result = checker.isTyped(literal);

    // Assert
    assertFalse(result);
  }

  @Test
  void getType_ExpressionTypee_RetourneType()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(3.14, AtomicTypes.FLOATING));
    checker.check(literal);

    // Act
    Type result = checker.getType(literal);

    // Assert
    assertEquals(TypeFactory.FLOATING, result);
  }

  @Test
  void getType_ExpressionNonTypee_LanceException()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>("test", AtomicTypes.STRING));

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.getType(literal);
    });

    assertTrue(exception.getMessage().contains("vérifiée"));
  }

  @Test
  void validateType_TypeCorrect_PasDException()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));

    // Act & Assert
    assertDoesNotThrow(() -> {
      checker.validateType(literal, TypeFactory.INTEGER);
    });
  }

  @Test
  void validateType_TypeIncorrect_LanceException()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.validateType(literal, TypeFactory.STRING);
    });

    assertTrue(exception.getMessage().contains("attendu"));
    assertTrue(exception.getMessage().contains("obtenu"));
  }

  @Test
  void testValidateType_TypeDansListe_PasDException()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>('A', AtomicTypes.CHAR));

    // Act & Assert
    assertDoesNotThrow(() -> {
      checker.validateType(literal, TypeFactory.CHAR, TypeFactory.STRING);
    });
  }

  @Test
  void testValidateType_TypeHorsListe_LanceException()
  {
    // Arrange
    Expression.Literal literal = new Expression.Literal(
        new AtomicValue<>(true, AtomicTypes.BOOLEAN));

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.validateType(literal, TypeFactory.INTEGER, TypeFactory.STRING);
    });

    assertTrue(exception.getMessage().contains("autorisé"));
    assertFalse(exception.getMessage().contains("booleen"));
  }

  @Test
  void test_ExpressionComplexe_VerificationComplete()
  {
    // Arrange: (10 + 20) * 3
    Expression.Literal dix = new Expression.Literal(
        new AtomicValue<>(10, AtomicTypes.INTEGER));
    Expression.Literal vingt = new Expression.Literal(
        new AtomicValue<>(20, AtomicTypes.INTEGER));
    Expression.Literal trois = new Expression.Literal(
        new AtomicValue<>(3, AtomicTypes.INTEGER));

    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Token fois = new Token(TokenType.STAR, "*", null, 1);

    Expression.Binary addition = new Expression.Binary(dix, plus, vingt);
    Expression.Grouping groupement = new Expression.Grouping(addition);
    Expression.Binary multiplication = new Expression.Binary(groupement, fois, trois);

    // Act
    Expression result = checker.check(multiplication);

    // Assert
    assertNotNull(result);
    assertEquals(TypeFactory.INTEGER, result.getType());
    assertEquals(TypeFactory.INTEGER, groupement.getType());
    assertEquals(TypeFactory.INTEGER, addition.getType());
    assertEquals(TypeFactory.INTEGER, dix.getType());
    assertEquals(TypeFactory.INTEGER, vingt.getType());
    assertEquals(TypeFactory.INTEGER, trois.getType());
  }
}
