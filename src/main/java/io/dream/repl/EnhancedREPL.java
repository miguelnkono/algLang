package io.dream.repl;

import io.dream.Main;
import io.dream.config.Config;
import io.dream.config.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced REPL (Read-Eval-Print-Loop) with user-friendly features
 */
public class EnhancedREPL {

    private final BufferedReader reader;
    private final List<String> history;
    private final StringBuilder multiLineBuffer;
    private boolean inMultiLineMode;
    private final Map<String, ReplCommand> commands;

    // ANSI color codes for better UI
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";
    private static final String BOLD = "\u001B[1m";

    public EnhancedREPL() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.history = new ArrayList<>();
        this.multiLineBuffer = new StringBuilder();
        this.inMultiLineMode = false;
        this.commands = new HashMap<>();
        initializeCommands();
    }

    private void initializeCommands() {
        // Help command
        commands.put(".help", new ReplCommand(
                "Show all available commands",
                this::showHelp
        ));

        // Exit command
        commands.put(".exit", new ReplCommand(
                "Exit the REPL",
                () -> { throw new ExitException(); }
        ));

        commands.put(".quit", new ReplCommand(
                "Exit the REPL (alias for .exit)",
                () -> { throw new ExitException(); }
        ));

        // Clear command
        commands.put(".clear", new ReplCommand(
                "Clear the screen",
                this::clearScreen
        ));

        // History command
        commands.put(".history", new ReplCommand(
                "Show command history",
                this::showHistory
        ));

        // Language switch
        commands.put(".lang", new ReplCommand(
                "Toggle between French and English",
                this::toggleLanguage
        ));

        commands.put(".french", new ReplCommand(
                "Switch to French mode",
                () -> setLanguage(true)
        ));

        commands.put(".english", new ReplCommand(
                "Switch to English mode",
                () -> setLanguage(false)
        ));

        // Example programs
        commands.put(".example", new ReplCommand(
                "Show example programs",
                this::showExamples
        ));

        // Syntax reference
        commands.put(".syntax", new ReplCommand(
                "Show syntax reference",
                this::showSyntax
        ));

        // Reset environment
        commands.put(".reset", new ReplCommand(
                "Reset the interpreter (clear all variables)",
                this::resetInterpreter
        ));
    }

    public void start() throws IOException {
        printWelcomeBanner();

        try {
            runLoop();
        } catch (ExitException e) {
            printGoodbye();
        }
    }

    private void runLoop() throws IOException {
        while (true) {
            try {
                String prompt = inMultiLineMode
                        ? GRAY + "... " + RESET
                        : BLUE + (Config.getLanguage() ? "algolang> " : "algolang> ") + RESET;

                System.out.print(prompt);
                String line = reader.readLine();

                if (line == null) { // EOF (Ctrl+D)
                    break;
                }

                line = line.trim();

                // Empty line handling
                if (line.isEmpty()) {
                    if (inMultiLineMode) {
                        // Empty line in multi-line mode executes the buffer
                        executeMultiLineBuffer();
                    }
                    continue;
                }

                // Check for REPL commands
                if (line.startsWith(".")) {
                    handleCommand(line);
                    continue;
                }

                // Check for multi-line mode start
                if (isMultiLineStart(line)) {
                    startMultiLineMode(line);
                    continue;
                }

                // Check for multi-line mode end
                if (inMultiLineMode && isMultiLineEnd(line)) {
                    endMultiLineMode(line);
                    continue;
                }

                // In multi-line mode, accumulate
                if (inMultiLineMode) {
                    multiLineBuffer.append(line).append("\n");
                    continue;
                }

                // Single line execution
                executeLine(line);

            } catch (ExitException e) {
                throw e;
            } catch (Exception e) {
                System.err.println(YELLOW + "Error: " + e.getMessage() + RESET);
            }
        }
    }

    private boolean isMultiLineStart(String line) {
        String upper = line.toUpperCase();
        return upper.startsWith("ALGORITHME:") || upper.startsWith("ALGORITHM:");
    }

    // TODO: Will end a multi line with double enter click, need to fix it.
    private boolean isMultiLineEnd(String line) {
        String upper = line.toUpperCase();
        return upper.equals("FIN") || upper.equals("END");
    }

    private void startMultiLineMode(String line) {
        inMultiLineMode = true;
        multiLineBuffer.setLength(0);
        multiLineBuffer.append(line).append("\n");
        System.out.println(GRAY + "(Multi-line mode. Type 'Fin' or 'End' to execute, or empty line to cancel)" + RESET);
    }

    private void endMultiLineMode(String line) {
        multiLineBuffer.append(line).append("\n");
        executeMultiLineBuffer();
    }

    private void executeMultiLineBuffer() {
        String code = multiLineBuffer.toString();
        inMultiLineMode = false;
        multiLineBuffer.setLength(0);

        if (!code.trim().isEmpty()) {
            history.add(code);
            try {
                Main.runCode(code);
            } catch (IOException e) {
                System.err.println(YELLOW + "Execution error: " + e.getMessage() + RESET);
            }
        }
    }

    private void executeLine(String line) {
        history.add(line);

        // For REPL, we allow quick expressions and statements
        // Wrap them in a minimal program structure
        String wrappedCode = wrapForExecution(line);

        try {
            Main.runCode(wrappedCode);
        } catch (IOException e) {
            System.err.println(YELLOW + "Error: " + e.getMessage() + RESET);
        }
    }

    private String wrapForExecution(String line) {
        // If line looks like a complete statement, wrap it
        boolean isFrench = Config.getLanguage();

        String header = isFrench
                ? "Algorithme: repl;\nDebut:\n"
                : "Algorithm: repl;\nBegin:\n";

        String footer = isFrench ? "Fin" : "End";

        // Ensure line ends with semicolon
        if (!line.endsWith(";")) {
            line = line + ";";
        }

        return header + "    " + line + "\n" + footer;
    }

    private void handleCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        ReplCommand replCommand = commands.get(cmd);
        if (replCommand != null) {
            replCommand.execute();
        } else {
            System.out.println(YELLOW + "Unknown command: " + command + RESET);
            System.out.println("Type " + CYAN + ".help" + RESET + " to see available commands");
        }
    }

    private void printWelcomeBanner() {
        String banner = Config.getLanguage()
                ? """
            ╔════════════════════════════════════════════════════╗
            ║          AlgoLang - Interpréteur Interactif        ║
            ║                   Version 1.0.0                    ║
            ╚════════════════════════════════════════════════════╝
            
            Mode: Français | Tapez .help pour l'aide | .exit pour quitter
            """
                : """
            ╔════════════════════════════════════════════════════╗
            ║          AlgoLang - Interactive Interpreter        ║
            ║                   Version 1.0.0                    ║
            ╚════════════════════════════════════════════════════╝
            
            Mode: English | Type .help for help | .exit to quit
            """;

        System.out.println(GREEN + banner + RESET);
    }

    private void printGoodbye() {
        String msg = Config.getLanguage()
                ? "\n👋 Au revoir!\n"
                : "\n👋 Goodbye!\n";
        System.out.println(GREEN + msg + RESET);
    }

    private void showHelp() {
        boolean isFrench = Config.getLanguage();

        System.out.println("\n" + BOLD + (isFrench ? "Commandes Disponibles:" : "Available Commands:") + RESET);
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

        printCommandHelp(".help", isFrench ? "Afficher cette aide" : "Show this help");
        printCommandHelp(".exit, .quit", isFrench ? "Quitter le REPL" : "Exit the REPL");
        printCommandHelp(".clear", isFrench ? "Effacer l'écran" : "Clear the screen");
        printCommandHelp(".history", isFrench ? "Afficher l'historique" : "Show command history");
        printCommandHelp(".lang", isFrench ? "Changer de langue" : "Toggle language");
        printCommandHelp(".french", isFrench ? "Passer en français" : "Switch to French");
        printCommandHelp(".english", isFrench ? "Passer en anglais" : "Switch to English");
        printCommandHelp(".example", isFrench ? "Voir des exemples" : "Show examples");
        printCommandHelp(".syntax", isFrench ? "Référence de syntaxe" : "Syntax reference");
        printCommandHelp(".reset", isFrench ? "Réinitialiser l'interpréteur" : "Reset interpreter");

        System.out.println("\n" + BOLD + (isFrench ? "Mode d'Utilisation:" : "Usage:") + RESET);
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

        if (isFrench) {
            System.out.println("  • Une ligne:        " + GRAY + "ecrire(\"Bonjour\");" + RESET);
            System.out.println("  • Multi-lignes:     " + GRAY + "Commence par 'Algorithme:', finit par 'Fin'" + RESET);
            System.out.println("  • Ligne vide:       " + GRAY + "Exécute le code multi-lignes" + RESET);
        } else {
            System.out.println("  • Single line:      " + GRAY + "write(\"Hello\");" + RESET);
            System.out.println("  • Multi-line:       " + GRAY + "Start with 'Algorithm:', end with 'End'" + RESET);
            System.out.println("  • Empty line:       " + GRAY + "Execute multi-line code" + RESET);
        }

        System.out.println();
    }

    private void printCommandHelp(String command, String description) {
        System.out.printf("  %-20s %s\n", YELLOW + command + RESET, GRAY + description + RESET);
    }

    private void clearScreen() {
        // ANSI escape code to clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
        printWelcomeBanner();
    }

    private void showHistory() {
        boolean isFrench = Config.getLanguage();

        if (history.isEmpty()) {
            System.out.println(GRAY + (isFrench ? "(Historique vide)" : "(History is empty)") + RESET);
            return;
        }

        System.out.println("\n" + BOLD + (isFrench ? "Historique:" : "History:") + RESET);
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

        for (int i = 0; i < history.size(); i++) {
            String entry = history.get(i);
            if (entry.contains("\n")) {
                System.out.printf("%s[%d]%s (multi-line)\n", YELLOW, i + 1, RESET);
            } else {
                System.out.printf("%s[%d]%s %s\n", YELLOW, i + 1, RESET, GRAY + entry + RESET);
            }
        }
        System.out.println();
    }

    private void toggleLanguage() {
        Config.setLanguage(!Config.getLanguage());
        String msg = Config.getLanguage()
                ? "✓ Langue changée: Français"
                : "✓ Language changed: English";
        System.out.println(GREEN + msg + RESET);
    }

    private void setLanguage(boolean french) {
        Config.setLanguage(french);
        String msg = french
                ? "✓ Langue changée: Français"
                : "✓ Language changed: English";
        System.out.println(GREEN + msg + RESET);
    }

    private void showExamples() {
        boolean isFrench = Config.getLanguage();

        System.out.println("\n" + BOLD + (isFrench ? "Exemples:" : "Examples:") + RESET);
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

        if (isFrench) {
            System.out.println("\n" + YELLOW + "1. Expression simple:" + RESET);
            System.out.println(GRAY + "   ecrire(\"Bonjour le monde!\");" + RESET);

            System.out.println("\n" + YELLOW + "2. Calcul:" + RESET);
            System.out.println(GRAY + "   ecrire(\"Résultat: \" + (10 + 5));" + RESET);

            System.out.println("\n" + YELLOW + "3. Programme complet:" + RESET);
            System.out.println(GRAY + """
                   Algorithme: exemple;
                   Variables:
                       x : entier;
                   Debut:
                       x <- 42;
                       ecrire("x = " + x);
                   Fin
                   """ + RESET);
        } else {
            System.out.println("\n" + YELLOW + "1. Simple expression:" + RESET);
            System.out.println(GRAY + "   write(\"Hello world!\");" + RESET);

            System.out.println("\n" + YELLOW + "2. Calculation:" + RESET);
            System.out.println(GRAY + "   write(\"Result: \" + (10 + 5));" + RESET);

            System.out.println("\n" + YELLOW + "3. Complete program:" + RESET);
            System.out.println(GRAY + """
                   Algorithm: example;
                   Variables:
                       x : integer;
                   Begin:
                       x <- 42;
                       write("x = " + x);
                   End
                   """ + RESET);
        }

        System.out.println();
    }

    private void showSyntax() {
        boolean isFrench = Config.getLanguage();

        System.out.println("\n" + BOLD + (isFrench ? "Référence de Syntaxe:" : "Syntax Reference:") + RESET);
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

        if (isFrench) {
            System.out.println("\n" + YELLOW + "Types de données:" + RESET);
            System.out.println(GRAY + "  entier, reel, chaine_charactere, caractere, booleen" + RESET);

            System.out.println("\n" + YELLOW + "Opérateurs arithmétiques:" + RESET);
            System.out.println(GRAY + "  + - * /" + RESET);

            System.out.println("\n" + YELLOW + "Opérateurs de comparaison:" + RESET);
            System.out.println(GRAY + "  > < >= <= == !=" + RESET);

            System.out.println("\n" + YELLOW + "Affectation:" + RESET);
            System.out.println(GRAY + "  variable <- valeur;" + RESET);

            System.out.println("\n" + YELLOW + "Affichage:" + RESET);
            System.out.println(GRAY + "  ecrire(expression);" + RESET);
        } else {
            System.out.println("\n" + YELLOW + "Data types:" + RESET);
            System.out.println(GRAY + "  integer, real, string, char, boolean" + RESET);

            System.out.println("\n" + YELLOW + "Arithmetic operators:" + RESET);
            System.out.println(GRAY + "  + - * /" + RESET);

            System.out.println("\n" + YELLOW + "Comparison operators:" + RESET);
            System.out.println(GRAY + "  > < >= <= == !=" + RESET);

            System.out.println("\n" + YELLOW + "Assignment:" + RESET);
            System.out.println(GRAY + "  variable <- value;" + RESET);

            System.out.println("\n" + YELLOW + "Output:" + RESET);
            System.out.println(GRAY + "  write(expression);" + RESET);
        }

        System.out.println();
    }

    private void resetInterpreter() {
        // This would reset the interpreter state
        // For now, just clear history
        history.clear();
        multiLineBuffer.setLength(0);
        inMultiLineMode = false;

        String msg = Config.getLanguage()
                ? "✓ Interpréteur réinitialisé"
                : "✓ Interpreter reset";
        System.out.println(GREEN + msg + RESET);
    }

    // Helper classes
    private static class ReplCommand {
        final String description;
        final Runnable action;

        ReplCommand(String description, Runnable action) {
            this.description = description;
            this.action = action;
        }

        void execute() {
            action.run();
        }
    }

    private static class ExitException extends RuntimeException {}
}
