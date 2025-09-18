package io.dream;

import io.dream.scanner.Scanner;
import io.dream.scanner.Token;

import java.io.IOException;
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

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.out.format("Usage: algolang <script>.al\n");
            System.exit(64);
        } else
        {
            if (!Files.exists(Path.of(args[0])))
            {
                System.err.println("File " + args[0] + " does not exists.");
                System.exit(64);
            }
            if (args[0].endsWith(".al"))
            {
                // run the file containing the source of the user.
                runFile(args[0]);
            }
            else
            {
                System.err.println("Wrong script file");
                System.exit(64);
            }
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

        for (Token token : tokens)
        {
            System.out.println(token.toString());
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
    private static void report(int line, String message) {
        report(line, null, message);
    }

    /**
     * This is the generale function to report errors to the users.
     *
     * @param line the line where the error occurred
     * @param where where the error was found in the source code
     * @param message the message that we will report to the users to inform them that an error
     *                occurred
     * */
    private static void report(int line, String where, String message)
    {
        System.err.format("[line %d ] Error : %s :  %s\n", line, where, message);
        Main.hadError = true;
    }

    /**
     * This function report errors the users without telling them where on what the error was
     * found.
     *
     * @param line the line where the error occurred
     * @param message the message to display to the users
     * */
    public static void error(int line, String message)
    {
        Main.report(line, "", message);
    }
}
