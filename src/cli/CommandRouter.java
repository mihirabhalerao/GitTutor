package cli;

import java.util.List;

import engine.FileSystemIO;
import engine.StorageEngine;

public class CommandRouter {
    private final StorageEngine storageEngine;
    private final FileSystemIO fileSystemIO;

    public CommandRouter() {
        this.storageEngine = new StorageEngine();
        this.fileSystemIO = new FileSystemIO();
    }
    public void handleInput(String input) {
        List<String> tokens = CommandParser.tokenize(input); 
        String baseCommand = tokens.get(0);
        if (!baseCommand.equalsIgnoreCase("bit")) {
            System.out.println("Error: Command must start with 'bit'. Example: 'bit init'.");
            return;
        }

        if (tokens.size() < 2) {
            System.out.println("Error: Missing sub-command option. Try 'bit init' or 'bit help'.");
            return;
        }

        routeSubCommand(tokens.get(1), tokens);
    }

    private void routeSubCommand(String subCommand, List<String> tokens) {
        switch (subCommand.toLowerCase()) {
            case "init":     handleInit(tokens);     break;
            case "edit":     handleEdit(tokens);     break;
            case "commit":   handleCommit(tokens);   break;
            case "branch":   handleBranch(tokens);   break;
            case "checkout": handleCheckout(tokens); break;
            case "diff":     handleDiff(tokens);     break;
            case "help":     printHelpMenu();        break;
            default:
                System.out.println("Error: Unknown command 'bit " + subCommand + "'. Type 'bit help'.");
        }
        System.out.println();
    }

    // --- Sub-command execution stubs (Will wire up to 'engine' layer in later phases) ---

    private void handleInit(List<String> tokens) {
        fileSystemIO.initalizePlayground();
    }

    private void handleEdit(List<String> tokens) {
        if (tokens.size() < 3) {
            System.out.println("Error: Specify a file name. Example: bit edit note.txt");
            return;
        }
        String fileName = tokens.get(2);
        List<String> matches = storageEngine.getTrieEngine().searchPrefix(fileName);

        if (matches.isEmpty()) {
            System.out.println("Error: File matching input pattern sequence '" + fileName + "' untracked in this workspace registry.");
            return;
        }
        String resolvedFileName = matches.get(0);
        fileSystemIO.openNativeEditor(resolvedFileName);
    }

    private void handleCommit(List<String> tokens) {
        if (tokens.size() < 4 || !tokens.get(2).equals("-m")) {
            System.out.println("Error: Syntax invalid. Use: bit commit -m \"message\"");
            return;
        }
        System.out.println("[Modular Phase 1]: Routing snapshot content to GraphManager and Merkle tree layers...");
    }

    private void handleBranch(List<String> tokens) {
        if (tokens.size() < 3) return;
        System.out.println("[Modular Phase 1]: Registering new string key sequence inside TrieEngine...");
    }

    private void handleCheckout(List<String> tokens) {
        if (tokens.size() < 3) return;
        System.out.println("[Modular Phase 1]: Verifying pointer matches via Trie before triggering disk rewrite...");
    }

    private void handleDiff(List<String> tokens) {
        System.out.println("[Modular Phase 1]: Accessing LRU Cache before running Dynamic Programming line calculations...");
    }

    private void printHelpMenu() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  bit init               - Setup a new workspace directory.");
        System.out.println("  bit edit <file>        - Fire external process hook editor.");
        System.out.println("  bit commit -m \"msg\"    - Compute Merkle changes and log commit.");
        System.out.println("  bit branch <name>      - Construct new vertex edge tracking tag.");
        System.out.println("  bit checkout <target>  - Traverse history graph nodes.");
        System.out.println("  bit diff               - Run LCS DP matrix comparison mapping.");
    }
}
