package io.dream.scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.dream.scanner.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class ScannerTest
{

    // we create a scanner object
    private Scanner scanner;

    @BeforeEach
    void setUp()
    {}

    @AfterEach
    void tearDown()
    {}

    @Test
    void scanTokens()
    {
        var source = ",;:()[]";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // expect 8 tokens inside the list of tokens
        assertEquals(8, tokens.size(), "Testing to see if the size of tokens expected were " +
                "satisfied");

        // Verify each token type and lexeme
        assertEquals(COMMA, tokens.get(0).type());
        assertEquals(",", tokens.get(0).lexeme());

        assertEquals(SEMICOLON, tokens.get(1).type());
        assertEquals(";", tokens.get(1).lexeme());

        assertEquals(COLON, tokens.get(2).type());
        assertEquals(":", tokens.get(2).lexeme());

        assertEquals(LEFT_PAREN, tokens.get(3).type());
        assertEquals("(", tokens.get(3).lexeme());

        assertEquals(RIGHT_PAREN, tokens.get(4).type());
        assertEquals(")", tokens.get(4).lexeme());

        assertEquals(LEFT_BRACKET, tokens.get(5).type());
        assertEquals("[", tokens.get(5).lexeme());

        assertEquals(RIGHT_BRACKET, tokens.get(6).type());
        assertEquals("]", tokens.get(6).lexeme());

        assertEquals(EOF, tokens.get(7).type());
    }

    @Test
    void scanTokensIndent()
    {
      String source = "\tcool\n\t\n";
      scanner = new Scanner(source);
      var tokens = scanner.scanTokens();

      // verify the number of tokens returned.
      assertEquals(3, tokens.size());

      // verify the tokens type and lexeme.
      assertEquals(INDENT, tokens.get(0).type());
      assertEquals("\t", tokens.get(0).lexeme());
      assertEquals(IDENTIFIER, tokens.get(1).type());
      assertEquals("cool", tokens.get(1).lexeme());
      assertEquals(EOF, tokens.get(2).type());
      assertEquals("nil", tokens.get(2).lexeme());
    }

    @Test
    void scanTokensWithWhitespace()
    {
        var source = " , ; : ( ) [ ] ";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Should still get the same tokens (whitespace ignored) + EOF
        assertEquals(8, tokens.size());

        assertEquals(COMMA, tokens.get(0).type());
        assertEquals(SEMICOLON, tokens.get(1).type());
        assertEquals(COLON, tokens.get(2).type());
        assertEquals(LEFT_PAREN, tokens.get(3).type());
        assertEquals(RIGHT_PAREN, tokens.get(4).type());
        assertEquals(LEFT_BRACKET, tokens.get(5).type());
        assertEquals(RIGHT_BRACKET, tokens.get(6).type());
        assertEquals(EOF, tokens.get(7).type());
    }

    @Test
    void scanTokensEmptySource()
    {
        // Test empty source code
        String source = "";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Should only contain EOF token
        assertEquals(1, tokens.size());
        assertEquals(EOF, tokens.get(0).type());
    }

    @Test
    void scanTokensUnsupportedCharacter()
    {
        // Test unsupported character (should trigger error but still produce EOF)
        String source = "@";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Should contain only EOF (unsupported character won't create a token but will log error)
        assertEquals(1, tokens.size());
        assertEquals(EOF, tokens.get(0).type());
    }

    @Test
    void scanTokensMixedValidAndInvalid()
    {
        // Test mix of valid and invalid characters
        String source = ",@;";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Should contain COMMA, SEMICOLON, and EOF (invalid character @ triggers error but doesn't create token)
        assertEquals(3, tokens.size());
        assertEquals(COMMA, tokens.get(0).type());
        assertEquals(SEMICOLON, tokens.get(1).type());
        assertEquals(EOF, tokens.get(2).type());
    }

    @Test
    void scanTokensMultipleLines()
    {
        // Test tokens spanning multiple lines
        String source = ",\n;\n:";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size()); // COMMA, SEMICOLON, COLON, EOF

        // Verify tokens and their line numbers
        assertEquals(COMMA, tokens.get(0).type());
        assertEquals(0, tokens.get(0).line()); // First line

        assertEquals(SEMICOLON, tokens.get(1).type());
        assertEquals(1, tokens.get(1).line()); // Second line

        assertEquals(COLON, tokens.get(2).type());
        assertEquals(2, tokens.get(2).line()); // Third line
    }

    @Test
    void scanTokensAllSymbols()
    {
        var source = """
                // this is a comment
                (( )) // grouping stuff
                !*+-/=<> <= == // operators
                """;
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        List<TokenType> tokenTypes = List.of(LEFT_PAREN, LEFT_PAREN,
                RIGHT_PAREN, RIGHT_PAREN, BANG, STAR, PLUS, MINUS, SLASH, EQUAL, LESS, GREATER,
                LESS_OR_EQUAL, EQUAL_EQUAL, EQUAL, EQUAL_EQUAL);

        // test the number of tokens present in this source code.
        assertEquals(15, tokens.size(), "checking to see if the number of tokens expected is the " +
                "same to the one produce after scanning the source.");

        // test each individual tokens.
        for (TokenType tokenType : tokenTypes)
        {
            assertEquals(tokenType, tokens.get(tokenTypes.indexOf(tokenType)).type(), "testing to" +
                    " see the type of the current in the source code correspond to the token " +
                    "expected.");
        }
    }

    @Test
    void scanStringToken()
    {
        var source = "\"cool\"";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        assertEquals(2, tokens.size(), "test to see if the number of tokens in this list is one.");
        assertEquals(STRING_LITERAL, tokens.get(0).type(), "check to see if the token scanned is a " +
                "string" +
                ".");
    }

    @Test
    void scanNumberToken()
    {
        var source = "10\n12,4";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        assertEquals(3, tokens.size(), "test to see if the number of tokens in this list is two.");
        assertEquals(INTEGER_LITERAL, tokens.get(0).type());
        assertEquals(DOUBLE_LITERAL, tokens.get(1).type());
    }

    @Test
    void scanIdentifierToken()
    {
        var source = "age\nVariables Algorithme";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size(), "check the number of tokens");
        assertEquals(IDENTIFIER, tokens.get(0).type());
        assertEquals(VARIABLE, tokens.get(1).type());
        assertEquals(ALGORITHM, tokens.get(2).type());
    }

    @Test
    void scanStatements()
    {
        String source = "Variables:\n\ta: entier;\n\tb: reel;\n";
        scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        assertEquals(13, tokens.size());
        assertEquals(VARIABLE, tokens.get(0).type());
        assertEquals(COLON, tokens.get(1).type());
        assertEquals(INDENT, tokens.get(2).type());
        assertEquals(IDENTIFIER, tokens.get(3).type());
        assertEquals(COLON, tokens.get(4).type());
        assertEquals(INTEGER, tokens.get(5).type());
        assertEquals(SEMICOLON, tokens.get(6).type());
        assertEquals(INDENT, tokens.get(7).type());
        assertEquals(IDENTIFIER, tokens.get(8).type());
        assertEquals(COLON, tokens.get(9).type());
        assertEquals(DOUBLE, tokens.get(10).type());
        assertEquals(SEMICOLON, tokens.get(11).type());
        assertEquals(EOF, tokens.get(12).type());
    }
}
