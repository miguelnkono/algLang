package io.dream.repl;

import io.dream.Main;
import io.dream.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple REPL without colors - for better compatibility
 */
public class SimpleREPL {

    private final BufferedReader reader;
    private final List<String> history;

    public SimpleREPL() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.history = new ArrayList<>();
    }

    public void start() throws IOException {
        printWelcome();

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();

            if (line == null) break;  // EOF

            line = line.trim();

            if (line.isEmpty()) continue;

            // Handle commands
            if (line.startsWith(".")) {
                if (handleCommand(line)) {
                    break;  // exit command
                }
                continue;
            }

            // Execute code
            history.add(line);
            String wrapped = wrapCode(line);

            try {
                Main.runCode(wrapped);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
    }

    private boolean handleCommand(String cmd) {
        switch (cmd.toLowerCase()) {
            case ".exit":
            case ".quit":
                return true;

            case ".help":
                showHelp();
                break;

            case ".history":
                showHistory();
                break;

            case ".lang":
                toggleLanguage();
                break;

            case ".french":
                Config.setLanguage(true);
                System.out.println("Language: Français");
                break;

            case ".english":
                Config.setLanguage(false);
                System.out.println("Language: English");
                break;

            default:
                System.out.println("Unknown command. Type .help for help");
        }
        return false;
    }

    private void printWelcome() {
        System.out.println("====================================================");
        System.out.println("  AlgoLang Interactive Interpreter");
        System.out.println("  Version 1.0.0");
        System.out.println("====================================================");
        System.out.println();
        System.out.println("Type .help for help | .exit to quit");
        System.out.println();
    }

    private void showHelp() {
        System.out.println();
        System.out.println("Available Commands:");
        System.out.println("  .help      - Show this help");
        System.out.println("  .exit      - Exit the REPL");
        System.out.println("  .quit      - Exit the REPL");
        System.out.println("  .history   - Show command history");
        System.out.println("  .lang      - Toggle language");
        System.out.println("  .french    - Switch to French");
        System.out.println("  .english   - Switch to English");
        System.out.println();
    }

    private void showHistory() {
        if (history.isEmpty()) {
            System.out.println("(History is empty)");
            return;
        }

        System.out.println("\nHistory:");
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, history.get(i));
        }
        System.out.println();
    }

    private void toggleLanguage() {
        Config.setLanguage(!Config.getLanguage());
        System.out.println("Language: " + (Config.getLanguage() ? "Français" : "English"));
    }

    private String wrapCode(String line) {
        boolean isFrench = Config.getLanguage();

        String header = isFrench
                ? "Algorithme: repl;\nDebut:\n"
                : "Algorithm: repl;\nBegin:\n";

        String footer = isFrench ? "Fin" : "End";

        if (!line.endsWith(";")) {
            line = line + ";";
        }

        return header + "    " + line + "\n" + footer;
    }
}
