package io.dream.config;

/**
 * Utility class for bilingual messages (French/English)
 */
public class Messages
{
    // Parser messages
    public static String expectAfter(String expected, String after) {
        return Config.getLanguage()
                ? "Attend '" + expected + "' après '" + after + "'."
                : "Expect '" + expected + "' after '" + after + "'.";
    }

    public static String expectColon(String after) {
        return Config.getLanguage()
                ? "Attend ':' après '" + after + "'."
                : "Expect ':' after '" + after + "'.";
    }

    public static String expectSemicolon(String after) {
        return Config.getLanguage()
                ? "Attend ';' après " + after + "."
                : "Expect ';' after " + after + ".";
    }

    public static String expectVariableName() {
        return Config.getLanguage()
                ? "Attend un nom de variable."
                : "Expect a variable name.";
    }

    public static String expectVariableType() {
        return Config.getLanguage()
                ? "Attends d'un type de variable."
                : "Expect a variable type.";
    }

    public static String expectAlgorithmName() {
        return Config.getLanguage()
                ? "Attend un nom d'algorithme après ':'."
                : "Expect an algorithm name after ':'.";
    }

    public static String expectBeginBlock() {
        return Config.getLanguage()
                ? "Attend 'Debut' pour commencer le bloc."
                : "Expect 'Begin' to start the block.";
    }

    public static String expectEndBlock() {
        return Config.getLanguage()
                ? "Attend 'Fin' pour terminer le bloc."
                : "Expect 'End' to finish the block.";
    }

    public static String expectStatement() {
        return Config.getLanguage()
                ? "Attend une instruction."
                : "Expect a statement.";
    }

    public static String expectExpression() {
        return Config.getLanguage()
                ? "Attends d'une expression."
                : "Expect an expression.";
    }

    public static String expectLeftParen(String after) {
        return Config.getLanguage()
                ? "Attend '(' après '" + after + "'."
                : "Expect '(' after '" + after + "'.";
    }

    public static String expectRightParen(String after) {
        return Config.getLanguage()
                ? "Attend ')' après " + after + "."
                : "Expect ')' after " + after + ".";
    }

    public static String expectAssignOperator() {
        return Config.getLanguage()
                ? "Attend '<-' pour l'affectation."
                : "Expect '<-' for assignment.";
    }

    public static String expectAlgorithmKeyword() {
        return Config.getLanguage()
                ? "Attend 'Algorithme' au début du programme."
                : "Expect 'Algorithm' at the beginning of the program.";
    }

    public static String variableAlreadyDeclared(String name) {
        return Config.getLanguage()
                ? "Variable '" + name + "' déjà déclarée."
                : "Variable '" + name + "' already declared.";
    }

    // Runtime error messages
    public static String variableAlreadyDefined(String name) {
        return Config.getLanguage()
                ? "Variable '" + name + "' est déjà définie."
                : "Variable '" + name + "' is already defined.";
    }

    public static String variableNotDefined(String name) {
        return Config.getLanguage()
                ? "Variable '" + name + "' n'est pas définie."
                : "Variable '" + name + "' is not defined.";
    }

    public static String operandMustBeNumber() {
        return Config.getLanguage()
                ? "L'opérande doit être un nombre."
                : "Operand must be a number.";
    }

    public static String operandsMustBeNumbers() {
        return Config.getLanguage()
                ? "Les opérandes doivent tous être des nombres."
                : "Operands must all be numbers.";
    }

    public static String operandsMustBeStringsOrNumbers() {
        return Config.getLanguage()
                ? "Les opérandes doivent être des chaînes ou des nombres pour l'opérateur +."
                : "Operands must be strings or numbers for the + operator.";
    }

    // Type checker messages
    public static String variableNotDeclared(String name) {
        return Config.getLanguage()
                ? "Variable '" + name + "' non déclarée."
                : "Variable '" + name + "' not declared.";
    }

    public static String typeIncompatibility(String varType, String valueType) {
        return Config.getLanguage()
                ? "Incompatibilité de type: variable de type " + varType + " ne peut pas recevoir une valeur de type " + valueType
                : "Type incompatibility: variable of type " + varType + " cannot receive a value of type " + valueType;
    }

    public static String operatorIncompatibleWithTypes(String operator, String leftType, String rightType, String requirement) {
        return Config.getLanguage()
                ? "Opérateur '" + operator + "' incompatible avec les types " + leftType + " et " + rightType + ". " + requirement
                : "Operator '" + operator + "' incompatible with types " + leftType + " and " + rightType + ". " + requirement;
    }

    public static String operatorRequirementNumbers() {
        return Config.getLanguage()
                ? "Les opérandes doivent être deux nombres ou deux chaînes de caractères."
                : "Operands must be two numbers or two strings.";
    }

    public static String operatorRequirementComparison() {
        return Config.getLanguage()
                ? "Les opérandes doivent être des nombres."
                : "Operands must be numbers.";
    }

    public static String operatorRequirementEquality() {
        return Config.getLanguage()
                ? "Les opérandes doivent être du même type."
                : "Operands must be of the same type.";
    }

    public static String unaryOperatorIncompatible(String operator, String type, String requirement) {
        return Config.getLanguage()
                ? "Opérateur unaire '" + operator + "' incompatible avec le type " + type + ". " + requirement
                : "Unary operator '" + operator + "' incompatible with type " + type + ". " + requirement;
    }

    public static String unaryMinusRequirement() {
        return Config.getLanguage()
                ? "L'opérande doit être un nombre (entier ou réel)."
                : "Operand must be a number (integer or real).";
    }

    public static String unaryBangRequirement() {
        return Config.getLanguage()
                ? "L'opérande doit être un booléen."
                : "Operand must be a boolean.";
    }

    public static String unsupportedBinaryOperator(String operator) {
        return Config.getLanguage()
                ? "Opérateur binaire non supporté: " + operator
                : "Unsupported binary operator: " + operator;
    }

    public static String unsupportedUnaryOperator(String operator) {
        return Config.getLanguage()
                ? "Opérateur unaire non supporté: " + operator
                : "Unsupported unary operator: " + operator;
    }

    public static String unsupportedLiteralType() {
        return Config.getLanguage()
                ? "Valeur littérale de type non supporté. Seules les valeurs atomiques (entier, réel, chaîne, caractère, booléen) sont autorisées."
                : "Unsupported literal type. Only atomic values (integer, real, string, character, boolean) are allowed.";
    }

    public static String typeCheckingFailed(String message) {
        return Config.getLanguage()
                ? "Échec de la vérification des types: " + message
                : "Type checking failed: " + message;
    }

    public static String expressionNotTypeChecked() {
        return Config.getLanguage()
                ? "L'expression n'a pas été vérifiée par le vérificateur de types."
                : "Expression has not been type-checked.";
    }

    public static String expectedTypeButGot(String expected, String actual) {
        return Config.getLanguage()
                ? "Type attendu: " + expected + ", mais obtenu: " + actual
                : "Expected type: " + expected + ", but got: " + actual;
    }

    public static String typeNotAllowed(String actual, String expected) {
        return Config.getLanguage()
                ? "Type " + actual + " non autorisé. Types attendus: " + expected
                : "Type " + actual + " not allowed. Expected types: " + expected;
    }

    // Scanner messages
    public static String unterminatedString() {
        return Config.getLanguage()
                ? "Chaîne de caractères non terminée."
                : "Unterminated string literal.";
    }

    public static String unterminatedCharacter() {
        return Config.getLanguage()
                ? "Caractère littéral non terminé."
                : "Unterminated character literal.";
    }

    public static String characterMustBeOne() {
        return Config.getLanguage()
                ? "Le caractère littéral doit contenir exactement un caractère."
                : "Character literal must contain exactly one character.";
    }

    public static String unsupportedCharacter() {
        return Config.getLanguage()
                ? "Caractère non supporté."
                : "Unsupported character.";
    }

    public static String wrongDecimalSeparatorFrench() {
        return Config.getLanguage()
                ? "En utilisant le français comme langage de l'interpréteur tu dois utiliser la virgule (,) pour définir les nombres réels."
                : "When using French as the interpreter language you must use the comma (,) to define real numbers.";
    }

    public static String wrongDecimalSeparatorEnglish() {
        return Config.getLanguage()
                ? "Lors de l'utilisation de l'interpréteur en anglais, assurez-vous d'utiliser le point (.) pour définir vos nombres réels."
                : "When using the interpreter in English make sure to use the dot (.) to define your real numbers (floating numbers and double numbers).";
    }

    // Main/General messages
    public static String atLine(int line) {
        return Config.getLanguage()
                ? " à la ligne " + line
                : " at line " + line;
    }

    public static String atToken(String lexeme) {
        return Config.getLanguage()
                ? " à '" + lexeme + "'"
                : " at '" + lexeme + "'";
    }

    public static String atEnd() {
        return Config.getLanguage()
                ? " à la fin"
                : " at end";
    }

    public static String linePrefix(int line) {
        return Config.getLanguage()
                ? "[ligne " + line + "]"
                : "[line " + line + "]";
    }

    public static String errorPrefix(int line, String where) {
        return Config.getLanguage()
                ? "[ligne " + line + " ] Erreur : " + where
                : "[line " + line + " ] Error : " + where;
    }

    public static String typeError() {
        return Config.getLanguage()
                ? "Erreur de type: "
                : "Type error: ";
    }

    public static String unrecognizedAtomicType(String type) {
        return Config.getLanguage()
                ? "Type basique non reconnu: " + type
                : "Unrecognized atomic type: " + type;
    }
}
