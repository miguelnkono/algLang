package io.dream.parser;

import io.dream.ast.Expression;
import io.dream.ast.Statement;
import io.dream.scanner.Scanner;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.AtomicTypes;
import io.dream.types.AtomicValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    public void testIfStmt() {
        // if_stmt           -> "Si" expression "alors" statement ("Sinon" statement)?
        String source = "Algorithme: SiTest;\n Debut: \nsi 2 == 2 alors:\n ecrire(\"cool\");\nfinsi Fin";
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        Statement.If ifStmt = new Statement.If(
                new Expression.Binary(
                    new Expression.Literal(new AtomicValue<Integer>(2, AtomicTypes.INTEGER)),
                    new Token(TokenType.EQUAL_EQUAL, "==", null, 3),
                    new Expression.Literal(new AtomicValue<Integer>(2, AtomicTypes.INTEGER))
                ),
                List.of (
                    new Statement.Write(
                        new Expression.Literal(new AtomicValue<String>("cool", AtomicTypes.STRING))
                    )
                ),
                null
        );
        assertEquals(1, statements.size());
        assertEquals(ifStmt, statements.getFirst(), () -> "test failed!");
    }
}
