package cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import engine.FileSystemIO;
import engine.HashingUtility;
import engine.StorageEngine;
import model.BlobNode;
import model.CommitNode;
import model.DirectoryTree;

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
            case "init":
                handleInit(tokens);
                break;
            case "edit":
                handleEdit(tokens);
                break;
            case "commit":
                handleCommit(tokens);
                break;
            case "branch":
                handleBranch(tokens);
                break;
            case "checkout":
                handleCheckout(tokens);
                break;
            case "diff":
                handleDiff(tokens);
                break;
            case "help":
                printHelpMenu();
                break;
            default:
                System.out.println("Error: Unknown command 'bit " + subCommand + "'. Type 'bit help'.");
        }
        System.out.println();
    }

    // --- Sub-command execution stubs (Will wire up to 'engine' layer in later
    // phases) ---

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
            System.out.println("Error: File matching input pattern sequence '" + fileName
                    + "' untracked in this workspace registry.");
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
        String commitMessage = tokens.get(3);
        Path playgroundPath = Paths.get("bit-playground");

        if (!Files.exists(playgroundPath)) {
            System.out.println("No root directory intialized. Please try running 'bit init' first.");
            return;
        }

        try {
            StringBuilder treeContentBuilder = new StringBuilder();
            DirectoryTree currentTree = new DirectoryTree();

            try (Stream<Path> paths = Files.list(playgroundPath)) {
                List<Path> fileList = paths.filter(Files::isRegularFile).toList();

                System.out.println("List of files in directory: ");
                for (Path p : fileList)
                    System.out.println(p.getFileName());

                for (Path filePath : fileList) {
                    String fileName = filePath.getFileName().toString();
                    System.out.println("Filename: " + fileName);
                    // String fileContent = Files.readAllBytes(filePath).toString()
                    String fileContent = Files.readString(filePath);
                    System.out.println("File content: " + fileContent);

                    String blobHash = HashingUtility.hashString(fileContent);
                    System.out.println("BlobHash for " + fileName + ": " + blobHash);

                    if (!storageEngine.containsObjectHash(blobHash))
                        storageEngine.saveObject(
                                blobHash, new BlobNode(blobHash, fileContent));

                    currentTree.addEntry(fileName, blobHash);
                    treeContentBuilder.append(fileName).append(":").append(blobHash).append(";");
                }
                System.out.println("Root tree hash: " + treeContentBuilder.toString());
            }

            String treeRootHash = HashingUtility.hashString(treeContentBuilder.toString());
            currentTree.setHash(treeRootHash);
            storageEngine.saveObject(treeRootHash, currentTree);

            String activeBranch = storageEngine.getHeadPointer();
            System.out.println("Active branch: " + activeBranch);
            String parentCommitHash = storageEngine.getCommitHashFromBranch(activeBranch);
            System.out.println("Parent commit hash: " + parentCommitHash);
            List<String> parents = new ArrayList<>();
            if (parentCommitHash != null) {
                parents.add(parentCommitHash);
            }
            String commitContentString = commitMessage + treeRootHash + System.currentTimeMillis() + parents.toString();
            System.out.println("Commit content string: " + commitContentString);

            String commitHash = HashingUtility.hashString(commitContentString);
            System.out.println("Commit hash: " + commitHash);

            CommitNode commit = new CommitNode(commitHash, commitMessage, treeRootHash, parents);
            storageEngine.saveCommit(commitHash, commit);

            storageEngine.updateBranchPointer(activeBranch, commitHash);
            storageEngine.getTrieEngine().insert(commitHash);

            System.out.println("[- Commit Successful -]");
            System.out.println("Saved snapshot hash reference: " + commitHash);
            System.out.println("Root Merkle Tree verification ID: " + treeRootHash);

            storageEngine.printStorageMetrics();
        } catch (IOException e) {
            System.out.println("Fatal Error during commit processing: " + e.getMessage());
        }
    }

    private void handleBranch(List<String> tokens) {
        if (tokens.size() != 3) {
            System.out.println("Invalid syntax for checkout command. No options allowed. Please use bit branch <branch-name>.");
        }

        String newBranchName = tokens.get(2);
        if (newBranchName.length() == 2 && newBranchName.charAt(0) == '-') {
            System.out.println("Invalid syntax for checkout command. No options allowed. Please use bit branch <branch-name>.");
            return;
        }

        if (storageEngine.branchExists(newBranchName)) {
            System.out.println(
                    "Branch with name '" + newBranchName + "' already exists. Try creating a branch with a new name!");
            return;
        }

        String activeBranch = storageEngine.getHeadPointer();
        String activeCommitHash = storageEngine.getCommitHashFromBranch(activeBranch);

        if (activeCommitHash == null) {
            System.out.println("Cannot create a branch in an empty repository. Make a commit first.");
            return;
        }

        storageEngine.updateBranchPointer(newBranchName, activeCommitHash);
        storageEngine.getTrieEngine().insert(newBranchName);
        System.out.println("Created branch '" + newBranchName + "' pointing stably to commit: " + activeCommitHash);
    }

    private void handleCheckout(List<String> tokens) {
        if (tokens.size() != 3) {
            System.out.println("Invalid syntax for checkout command. No options allowed. Please use bit checkout <branch-name>.");
        }

        String target = tokens.get(2);
        if (target.length() == 2 && target.charAt(0) == '-') {
            System.out.println("Invalid syntax for checkout command. No options allowed. Please use bit checkout <branch-name>.");
            return;
        }

        List<String> matches = storageEngine.getTrieEngine().searchPrefix(target);
        if (matches.size() == 0) {
            System.out.println("No branch or commit starting with prefix '" + target + "' exists in the system.");
        }

        String resolvedTarget = matches.get(0);
        String targetCommitHash = null;

        if (storageEngine.branchExists(resolvedTarget)) {
            storageEngine.setHeadPointer(resolvedTarget);
            targetCommitHash = storageEngine.getCommitHashFromBranch(resolvedTarget);
            System.out.println("Switched context to branch '" + resolvedTarget + "'");
        } else {
            storageEngine.setHeadPointer(resolvedTarget);
            targetCommitHash = resolvedTarget;
            System.out.println("Switched context to explicit commit snapshot reference: " + targetCommitHash);
        }

        if (targetCommitHash == null) {
            System.out.println("Warning: Target state has no snapshots recorded yet. Working directory untouched.");
            return;
        }

        CommitNode targetCommit = storageEngine.getCommit(targetCommitHash);
        String rootTreeHash = targetCommit.getRootTreeHash();
        DirectoryTree rooTree = (DirectoryTree) storageEngine.getObject(rootTreeHash);
        System.out.println("Restoring physical files inside './bit-playground/' to match target snapshot layout...");

        try {
            Map<String, String> entries = rooTree.getEntries();
            for (Map.Entry<String, String> e : entries.entrySet()) {
                String fileName = e.getKey();
                String blobHash = e.getValue();

                BlobNode blobNode = (BlobNode) storageEngine.getObject(blobHash);
                String previousTextContent = blobNode.getTextContent();

                Path filePath = Paths.get("bit-playground", fileName);
                Files.writeString(filePath, previousTextContent);
            }

            System.out.println("Successfully rolled back working directory timeline layout state!");
            storageEngine.printStorageMetrics();
        } catch (IOException e) {
            System.out.println("Fatal Error reconstructing file snapshots to disk: " + e.getMessage());
        }
    }

    private void handleDiff(List<String> tokens) {
        System.out.println(
                "[Modular Phase 1]: Accessing LRU Cache before running Dynamic Programming line calculations...");
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
