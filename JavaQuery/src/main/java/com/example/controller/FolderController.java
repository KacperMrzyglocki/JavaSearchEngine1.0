package com.example.controller;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static spark.Spark.*;

public class FolderController {

    private static final String FOLDERS_ROOT = "/app/assets/folders";

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /search/folder/:word
        get("/search/folder/:word", (req, res) -> {
            String word = req.params(":word");
            List<String> result = searchFolder(word);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/folder/or/:word1/:word2
        get("/search/folder/or/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchFolder(word1);
            List<String> resultWord2 = searchFolder(word2);

            // Combine results without duplicates
            Set<String> combinedResult = new HashSet<>(resultWord1);
            combinedResult.addAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(combinedResult));
        });

        // Endpoint GET /search/folder/and/:word1/:word2
        get("/search/folder/and/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchFolder(word1);
            List<String> resultWord2 = searchFolder(word2);

            // Find intersection of results
            Set<String> intersectionResult = new HashSet<>(resultWord1);
            intersectionResult.retainAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(intersectionResult));
        });
    }

    /**
     * Searches for the given word in the folder-based JSON structure.
     *
     * @param word The word to search for.
     * @return A list of book IDs where the word occurs.
     */
    private List<String> searchFolder(String word) {
        List<String> result = new ArrayList<>();
        String firstLetter = word.substring(0, 1).toLowerCase();
        String secondPart = word.length() > 1 ? word.substring(1, 2).toLowerCase() : "_";
        String filePath = Paths.get(FOLDERS_ROOT, firstLetter, secondPart + ".json").toString();

        try {
            // Read the file line by line since each line is a separate JSON object
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            for (String line : lines) {
                JSONObject json = new JSONObject(line.trim());
                if (json.has(word)) {
                    JSONObject bookData = json.getJSONObject(word);
                    result.addAll(bookData.keySet());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing file: " + filePath + " - " + e.getMessage());
        }

        return result;
    }
}
