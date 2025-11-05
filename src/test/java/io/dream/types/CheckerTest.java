package io.dream.types;

import io.dream.ast.Expr;
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
    Expr.Literal literal = new Expr.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));

    // Act
    Expr result = checker.check(literal);

    // Assert
    assertNotNull(result);
    assertEquals(TypeFactory.INTEGER, result.getType());
    assertTrue(checker.isTyped(result));
  }

  @Test
  void check_ExpressionInvalide_LanceTypeException()
  {
    // Arrange
    Expr.Literal entier = new Expr.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    Expr.Literal bool = new Expr.Literal(
        new AtomicValue<>(true, AtomicTypes.BOOLEAN));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expr.Binary expressionInvalide = new Expr.Binary(entier, plus, bool);

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
    Expr.Literal defaut = new Expr.Literal(
        new AtomicValue<>(0, AtomicTypes.INTEGER));
    Expr exprInvalide = null; // Expression invalide

    // Act
    Expr result = checker.check(exprInvalide, defaut);

    // Assert
    assertEquals(defaut, result);
  }

  @Test
  void visitBinaryExpression_AdditionEntiers_RetourneTypeEntier()
  {
    // Arrange
    Expr.Literal gauche = new Expr.Literal(
        new AtomicValue<>(10, AtomicTypes.INTEGER));
    Expr.Literal droite = new Expr.Literal(
        new AtomicValue<>(20, AtomicTypes.INTEGER));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expr.Binary addition = new Expr.Binary(gauche, plus, droite);

    // Act
    Type result = checker.visitBinaryExpr(addition);

    // Assert
    assertEquals(TypeFactory.INTEGER, result);
    assertEquals(TypeFactory.INTEGER, gauche.getType());
    assertEquals(TypeFactory.INTEGER, droite.getType());
  }

  @Test
  void visitBinaryExpression_AdditionChaines_RetourneTypeChaine()
  {
    // Arrange
    Expr.Literal gauche = new Expr.Literal(
        new AtomicValue<>("Bonjour ", AtomicTypes.STRING));
    Expr.Literal droite = new Expr.Literal(
        new AtomicValue<>("Monde", AtomicTypes.STRING));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expr.Binary concatenation = new Expr.Binary(gauche, plus, droite);

    // Act
    Type result = checker.visitBinaryExpr(concatenation);

    // Assert
    assertEquals(TypeFactory.STRING, result);
  }

  @Test
  void visitBinaryExpression_ComparaisonNombres_RetourneTypeBooleen()
  {
    // Arrange
    Expr.Literal gauche = new Expr.Literal(
        new AtomicValue<>(10, AtomicTypes.INTEGER));
    Expr.Literal droite = new Expr.Literal(
        new AtomicValue<>(5, AtomicTypes.INTEGER));
    Token plusGrand = new Token(TokenType.GREATER, ">", null, 1);
    Expr.Binary comparaison = new Expr.Binary(gauche, plusGrand, droite);

    // Act
    Type result = checker.visitBinaryExpr(comparaison);

    // Assert
    assertEquals(TypeFactory.BOOLEAN, result);
  }

  @Test
  void visitBinaryExpression_AdditionTypesIncompatibles_LanceException()
  {
    // Arrange
    Expr.Literal entier = new Expr.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    Expr.Literal chaine = new Expr.Literal(
        new AtomicValue<>("texte", AtomicTypes.STRING));
    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Expr.Binary additionInvalide = new Expr.Binary(entier, plus, chaine);

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.visitBinaryExpr(additionInvalide);
    });

    assertTrue(exception.getMessage().contains("incompatible"));
    assertTrue(exception.getMessage().contains("entier"));
    assertTrue(exception.getMessage().contains("chaîne"));
  }

  @Test
  void visitGroupingExpression_ExpressionInterne_RetourneMemeType()
  {
    // Arrange
    Expr.Literal interne = new Expr.Literal(
        new AtomicValue<>(3.14, AtomicTypes.FLOATING));
    Expr.Grouping groupement = new Expr.Grouping(interne);

    // Act
    Type result = checker.visitGroupingExpr(groupement);

    // Assert
    assertEquals(TypeFactory.FLOATING, result);
    assertEquals(TypeFactory.FLOATING, interne.getType());
  }

  @Test
  void visitUnaryExpression_MoinsUnaireEntier_RetourneTypeEntier()
  {
    // Arrange
    Expr.Literal operand = new Expr.Literal(
        new AtomicValue<>(42, AtomicTypes.INTEGER));
    Token moins = new Token(TokenType.MINUS, "-", null, 1);
    Expr.Unary moinsUnaire = new Expr.Unary(moins, operand);

    // Act
    Type result = checker.visitUnaryExpr(moinsUnaire);

    // Assert
    assertEquals(TypeFactory.INTEGER, result);
    assertEquals(TypeFactory.INTEGER, operand.getType());
  }

  @Test
  void visitUnaryExpression_NotBooleen_RetourneTypeBooleen()
  {
    // Arrange
    Expr.Literal operand = new Expr.Literal(
        new AtomicValue<>(true, AtomicTypes.BOOLEAN));
    Token not = new Token(TokenType.BANG, "!", null, 1);
    Expr.Unary notExpression = new Expr.Unary(not, operand);

    // Act
    Type result = checker.visitUnaryExpr(notExpression);

    // Assert
    assertEquals(TypeFactory.BOOLEAN, result);
  }

  @Test
  void visitUnaryExpression_MoinsUnaireNonNumerique_LanceException()
  {
    // Arrange
    Expr.Literal operand = new Expr.Literal(
        new AtomicValue<>("texte", AtomicTypes.STRING));
    Token moins = new Token(TokenType.MINUS, "-", null, 1);
    Expr.Unary moinsInvalide = new Expr.Unary(moins, operand);

    // Act & Assert
    TypeException exception = assertThrows(TypeException.class, () -> {
      checker.visitUnaryExpr(moinsInvalide);
    });

    assertTrue(exception.getMessage().contains("incompatible"));
    assertTrue(exception.getMessage().contains("chaîne"));
  }

  @Test
  void visitLiteralExpression_Entier_RetourneTypeEntier()
  {
    // Arrange
    Expr.Literal literal = new Expr.Literal(
        new AtomicValue<>(123, AtomicTypes.INTEGER));

    // Act
    Type result = checker.visitLiteralExpr(literal);

    // Assert
    assertEquals(TypeFactory.INTEGER, result);
  }

  @Test
  void visitLiteralExpression_Booleen_RetourneTypeBooleen()
  {
    // Arrange
    Expr.Literal literal = new Expr.Literal(
        new AtomicValue<>(false, AtomicTypes.BOOLEAN));

    // Act
    Type result = checker.visitLiteralExpr(literal);

    // Assert
    assertEquals(TypeFactory.BOOLEAN, result);
  }

  @Test
  void isTyped_ExpressionTypee_RetourneVrai()
  {
    // Arrange
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal literal = new Expr.Literal(
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
    Expr.Literal dix = new Expr.Literal(
        new AtomicValue<>(10, AtomicTypes.INTEGER));
    Expr.Literal vingt = new Expr.Literal(
        new AtomicValue<>(20, AtomicTypes.INTEGER));
    Expr.Literal trois = new Expr.Literal(
        new AtomicValue<>(3, AtomicTypes.INTEGER));

    Token plus = new Token(TokenType.PLUS, "+", null, 1);
    Token fois = new Token(TokenType.STAR, "*", null, 1);

    Expr.Binary addition = new Expr.Binary(dix, plus, vingt);
    Expr.Grouping groupement = new Expr.Grouping(addition);
    Expr.Binary multiplication = new Expr.Binary(groupement, fois, trois);

    // Act
    Expr result = checker.check(multiplication);

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
