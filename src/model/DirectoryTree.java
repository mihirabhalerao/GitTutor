package model;

import java.util.HashMap;
import java.util.Map;

public class DirectoryTree implements MerkleNode {
    private String hash;
    private final Map<String, String> entries;

    public DirectoryTree() {
        this.entries = new HashMap<>();
        this.hash = "";
    }

    @Override
    public String getHash() {
        return this.hash;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void addEntry(String filename, String objectHash) {
        this.entries.put(filename, objectHash);
    }

    public Map<String, String> getEntries() {
        return this.entries;
    }
}
