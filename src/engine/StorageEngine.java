package engine;

import java.util.HashMap;

import model.CommitNode;
import model.MerkleNode;

public class StorageEngine {
    private final HashMap<String, MerkleNode> objectDatabase;
    private final HashMap<String, CommitNode> commitDatabase;
    private final HashMap<String, String> branchPointers;
    private final TrieEngine trieEngine;
    private String headPointer;

    public StorageEngine() {
        this.objectDatabase = new HashMap<>();
        this.commitDatabase = new HashMap<>();
        this.branchPointers = new HashMap<>();
        this.trieEngine = new TrieEngine();
        this.headPointer = "main";
        
        this.trieEngine.insert("commit");
        this.trieEngine.insert("checkout");
        this.trieEngine.insert("branch");
        this.trieEngine.insert("diff");

        // remove these seeds when commit functionality is ready
        this.trieEngine.insert("welcome.txt");
        this.trieEngine.insert("todo.md");

        // Setup initial default branch pointer state
        this.branchPointers.put("main", null);
        this.trieEngine.insert("main");
    }


    // --- Database Operations API Methods ---

    public void saveObject(String hash, MerkleNode node) {
        objectDatabase.put(hash, node);
    }

    public MerkleNode getObject(String hash) {
        return objectDatabase.get(hash);
    }

    public boolean containsObjectHash(String hash) {
        return objectDatabase.containsKey(hash);
    }

    public void saveCommit(String hash, CommitNode commit) {
        commitDatabase.put(hash, commit);
    }

    public CommitNode getCommit(String hash) {
        return commitDatabase.get(hash);
    }

    public void updateBranchPointer(String branchName, String commitHash) {
        branchPointers.put(branchName, commitHash);
    }

    public String getCommitHashFromBranch(String branchName) {
        return branchPointers.get(branchName);
    }

    public boolean branchExists(String branchName) {
        return branchPointers.containsKey(branchName);
    }

    public String getHeadPointer() {
        return headPointer;
    }

    public void setHeadPointer(String headPointer) {
        this.headPointer = headPointer;
    }

    public TrieEngine getTrieEngine() {
        return this.trieEngine;
    }   
    
    public void printStorageMetrics() {
        System.out.println("--- Bit Storage Engine Metrics ---");
        System.out.println("Total Tracked Objects (Blobs/Trees): " + objectDatabase.size());
        System.out.println("Total Active Commits in Graph:      " + commitDatabase.size());
        System.out.println("Active Branch Pointers:            " + branchPointers.keySet());
        System.out.println("Current HEAD Context Location:      " + headPointer);
        System.out.println("----------------------------------");
    }
}
