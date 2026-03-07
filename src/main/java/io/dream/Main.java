package io.dream;

import io.dream.ast.Statement;
import io.dream.config.Config;
import io.dream.config.Messages;
import io.dream.error.RuntimeError;
import io.dream.parser.Parser;
import io.dream.repl.EnhancedREPL;
import io.dream.scanner.Scanner;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.Checker;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main
{
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static Interpreter interpreter = null;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length > 2)
        {
            System.out.format("Usage: alglang <script>.al\n");
            System.exit(64);
        } else if (args.length == 2)
        {
            if (args[1].startsWith("--language="))
            {
                int language_level = Integer.parseInt(args[1].split("=")[1].trim());
                if (language_level != 0 && language_level != 1)
                {
                    System.err.println("You should provide 0 or 1");
                    System.exit(64);
                }
                Config.setLanguage((language_level == 0));

                if (!Files.exists(Path.of(args[0])))
                {
                    // check to see if the file does exist.
                    System.err.println("File " + args[0] + " does not exists.");
                    System.exit(64);
                }
                if (args[0].endsWith(".al"))
                {
                    // run the file containing the source of the user only if the file ends with the .al extension.
                    runFile(args[0]);
                } else
                {
                    // the file does exist, but it is not an algo file.
                    System.err.println("Wrong script file");
                    System.exit(64);
                }

            }else
            {
                System.err.println("Your prefix should have this form: --language=(0|1).\n 0 means french and 1 english.");
                System.exit(64);
            }
        } else if (args.length == 1)
        {
            if (!Files.exists(Path.of(args[0])))
            {
                // check to see if the file does exist.
                System.err.println("File " + args[0] + " does not exists.");
                System.exit(64);
            }
            if (args[0].endsWith(".al"))
            {
                // run the file containing the source of the user only if the file ends with the .al extension.
                runFile(args[0]);
            } else
            {
                // the file does exist, but it is not an algo file.
                System.err.println("Wrong script file");
                System.exit(64);
            }
        } else
        {
            // user prefer run the prompt.
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException
    {
        EnhancedREPL repl = new EnhancedREPL();
        repl.start();
    }

    /**
     * This function read a file content and feeds it to the interpreter
     *
     * @param fileName this represents the name of the file we want to execute
     * @throws IOException the io exception
     */
    private static void runFile(String fileName) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(fileName));
        run(new String(bytes, Charset.defaultCharset()));

        if (Main.hadError) System.exit(64);
        if (Main.hadRuntimeError) System.exit(70);
    }

    /**
     * This function interprets the source code of the user
     *
     * @param script the script's content to interpret
     * @throws IOException the io exception
     */
    private static void run(String script) throws IOException
    {
        Scanner scanner = new Scanner(script);
        List<Token> tokens = scanner.scanTokens();
        tokens.stream()
                .filter(token -> token.lexeme().equals("quit"))
                .findFirst()
                .ifPresentOrElse(
                        token -> System.exit(0),
                        () -> tokens.forEach(System.out::println)
                );

        /*Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (!hadError)
        {
            try
            {
                // Create type checker with symbol table from parser
                Checker typeChecker = new Checker(parser.getSymbolTable());
                typeChecker.check(statements);

                // Create interpreter with symbol table
                interpreter = new Interpreter(parser.getSymbolTable());
            } catch (Exception e)
            {
                System.err.println(Messages.typeError() + e.getMessage());
                hadError = true;
                return;
            }
        }

        if (!hadError)
        {
            interpreter.interpret(statements);
        }*/
    }

    /**
     * Public method for REPL to execute code
     *
     * @param code the code to execute
     * @throws IOException if an I/O error occurs
     */
    public static void runCode(String code) throws IOException
    {
        run(code);
        hadError = false;  // Reset error state for REPL
        hadRuntimeError = false;
    }

    /**
     * This is the generale function to report errors to the users.
     *
     * @param line    the line where the error occurred
     * @param message the message that we will report to the users to inform them that an error
     *                occurred
     *
     */
    private static void report(int line, String message)
    {
        report(line, null, message);
    }

    /**
     * This is the generale function to report errors to the users.
     *
     * @param line    the line where the error occurred
     * @param where   where the error was found in the source code
     * @param message the message that we will report to the users to inform them that an error
     *                occurred
     *
     */
    private static void report(int line, String where, String message)
    {
        System.err.format("%s :  %s\n", Messages.errorPrefix(line, where), message);
        Main.hadError = true;
    }

    /**
     * This function report errors the users without telling them where on what the error was
     * found.
     *
     * @param line    the line where the error occurred
     * @param message the message to display to the users
     *
     */
    public static void error(int line, String message)
    {
        Main.report(line, "", message);
    }

    public static void error(Token token, String message)
    {
        if (token.type() == TokenType.EOF)
        {
            report(token.line(), Messages.atEnd(), message);
        } else
        {
            report(token.line(), Messages.atToken(token.lexeme()), message);
        }
    }

    public static void runtimeError(RuntimeError error)
    {
        System.err.println(error.getMessage() + "\n" + Messages.linePrefix(error.token().line()));
        hadRuntimeError = true;
    }
}
