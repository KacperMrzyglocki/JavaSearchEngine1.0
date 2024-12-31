package com.example.controller;

import com.google.gson.Gson;

import static spark.Spark.*;

import java.util.*;

public class FolderController {

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /search/folder/:word
        get("/search/folder/:word", (req, res) -> {
            String word = req.params(":word");
            List<String> result = searchFolder(word);  // Currently returns empty list
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/folder/or/:word1/:word2
        get("/search/folder/or/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchFolder(word1);  // Currently returns empty list
            List<String> resultWord2 = searchFolder(word2);  // Currently returns empty list

            // Combine results without duplicates
            Set<String> combinedResult = new HashSet<>(resultWord1);
            combinedResult.addAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(combinedResult));  // Returning the combined result
        });

        // Endpoint GET /search/folder/and/:word1/:word2
        get("/search/folder/and/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchFolder(word1);  // Currently returns empty list
            List<String> resultWord2 = searchFolder(word2);  // Currently returns empty list

            // Find intersection of results
            Set<String> intersectionResult = new HashSet<>(resultWord1);
            intersectionResult.retainAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(intersectionResult));  // Returning the intersection result
        });
    }

    /**
     * This method simulates the search functionality in a folder.
     * Currently, it returns an empty list for every search.
     *
     * @param word The word to search for.
     * @return A list of results (empty list for now).
     */
    private List<String> searchFolder(String word) {
        // This method is just a placeholder and returns an empty list for now.
        return new ArrayList<>();
    }
}
