package io.dream.parser;

import io.dream.ast.Statement;
import io.dream.scanner.Scanner;
import io.dream.scanner.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    public void testSimpleProgram() {
        String source = """
            Algorithme: test;
            Variables:
                x : entier;
            Debut:
                x <- 5;
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements, "Statements should not be null");
        assertTrue(parser.getSymbolTable().containsKey("x"),
                "Variable 'x' should be in symbol table");
    }

    @Test
    public void testIfStatement() {
        String source = """
            Algorithme: SiTest;
            Variables:
                x : entier;
            Debut:
                x <- 2;
                si x == 2 alors:
                    ecrire("cool");
                finsi
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(parser.getSymbolTable().containsKey("x"));
    }

    @Test
    public void testVariableDeclaration() {
        String source = """
            Algorithme: VarTest;
            Variables:
                x, y : entier;
                nom : chaine_charactere;
            Debut:
                x <- 10;
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(parser.getSymbolTable().containsKey("x"));
        assertTrue(parser.getSymbolTable().containsKey("y"));
        assertTrue(parser.getSymbolTable().containsKey("nom"));
    }

    @Test
    public void testWriteStatement() {
        String source = """
            Algorithme: WriteTest;
            Variables:
                x : entier;
            Debut:
                x <- 42;
                ecrire("Test: " + x);
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(parser.getSymbolTable().containsKey("x"));
    }

    @Test
    public void testComplexExpression() {
        String source = """
            Algorithme: ExprTest;
            Variables:
                x, y, z : entier;
            Debut:
                x <- 10;
                y <- 5;
                z <- (x + y) * 2;
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(parser.getSymbolTable().containsKey("x"));
        assertTrue(parser.getSymbolTable().containsKey("y"));
        assertTrue(parser.getSymbolTable().containsKey("z"));
    }

    @Test
    public void testMultipleStatements() {
        String source = """
            Algorithme: MultiTest;
            Variables:
                a, b, c : entier;
            Debut:
                a <- 5;
                b <- 10;
                c <- a + b;
                ecrire(c);
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(statements.size() > 0, "Should have multiple statements");
    }

    @Test
    public void testBooleanVariable() {
        String source = """
            Algorithme: BoolTest;
            Variables:
                flag : booleen;
            Debut:
                flag <- vrai;
                ecrire(flag);
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(parser.getSymbolTable().containsKey("flag"));
    }

    @Test
    public void testRealNumber() {
        String source = """
            Algorithme: RealTest;
            Variables:
                pi : reel;
            Debut:
                pi <- 3,14159;
                ecrire(pi);
            Fin
            """;

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        assertNotNull(statements);
        assertTrue(parser.getSymbolTable().containsKey("pi"));
    }
}
