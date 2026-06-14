package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieEngine {
    private static class TrieNode {
        private final Map<Character, TrieNode> children;
        private String completeWord;

        public TrieNode() {
            this.children = new HashMap<>();
            this.completeWord = null;
        }
    }

    private final TrieNode root;

    public TrieEngine() {
        this.root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode ptr = root;
        for (char ch : word.toCharArray()) {
            if (!ptr.children.containsKey(ch)) ptr.children.put(ch, new TrieNode());
            ptr = ptr.children.get(ch);
        }
        ptr.completeWord = word;
    }

    public List<String> searchPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode ptr = root;
        for (char ch : prefix.toCharArray()) {
            if (!ptr.children.containsKey(ch)) return results;
            ptr = ptr.children.get(ch);
        }
        collectAllWords(ptr, results);
        return results;
    }

    private void collectAllWords(TrieNode root, List<String> words) {
        if (root.completeWord != null) words.add(root.completeWord);
        for (TrieNode child : root.children.values()) {
            collectAllWords(child, words);
        }
    }
}
