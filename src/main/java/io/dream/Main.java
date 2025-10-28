package io.dream;

import io.dream.ast.Expression;
import io.dream.config.Config;
import io.dream.error.RuntimeError;
import io.dream.parser.Parser;
import io.dream.scanner.Scanner;
import io.dream.scanner.Token;
import io.dream.scanner.TokenType;
import io.dream.types.Checker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This is the entry point of the entire interpreter.
 *
 */
public class Main
{
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static Interpreter interpreter = new Interpreter();

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
            System.out.format("Usage: algolang <script>.al\n");
            System.exit(64);
        } else if (args.length == 2)
        {
            if (args[1].startsWith("--language="))
            {
                String level = args[1].split("=")[1].trim();
                int language_level = Integer.parseInt(level);
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
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; )
        {
            System.out.print("> ");
            String line = reader.readLine();

            if (line.contentEquals(".exit"))
            {
                System.out.println("Goodbye!");
                break;
            }

            run(line);
            hadError = false;
        }
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

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        if (!hadError)
        {
            try
            {
                Checker typeChecker = new Checker();
                typeChecker.check(expression);
            } catch (Exception e)
            {
                System.err.println("Erreur de type: " + e.getMessage());
                hadError = true;
                return;
            }
        }

        if (!hadError)
        {
            interpreter.interpret(expression);
        }
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
        System.err.format("[ligne %d ] Erreur : %s :  %s\n", line, where, message);
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
            report(token.line(), " à la fin", message);
        } else
        {
            report(token.line(), " à '" + token.lexeme() + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error)
    {
        System.err.println(error.getMessage() + "\n[Ligne " + error.token().line() + "]");
        hadRuntimeError = true;
    }
}
