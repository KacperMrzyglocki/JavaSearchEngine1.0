package com.example.controller;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.nio.file.Paths;
import java.util.*;
import java.nio.file.Files;
import java.io.IOException;

import static spark.Spark.*;

public class NormalController {

    private static final String INDEX_FILE_PATH = "/app/assets/inverted_index.json";

    public void registerRoutes() {
        Gson gson = new Gson();

        get("/hello", (req, res) -> {
            return "Hello world1";
        });

        // Endpoint GET /search/normal/:word
        get("/search/normal/:word", (req, res) -> {
            String word = req.params(":word");
            List<String> result = searchWord(word);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/normal/or/:word1/:word2
        get("/search/normal/or/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchWord(word1);
            List<String> resultWord2 = searchWord(word2);

            // Combine books from both words without duplicates
            Set<String> combinedResult = new HashSet<>(resultWord1);
            combinedResult.addAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(combinedResult));  // Returning the combined result
        });

        // Endpoint GET /search/normal/and/:word1/:word2
        get("/search/normal/and/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchWord(word1);
            List<String> resultWord2 = searchWord(word2);

            // Find intersection of books
            Set<String> intersectionResult = new HashSet<>(resultWord1);
            intersectionResult.retainAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(intersectionResult));  // Returning the intersection result
        });
    }

    /**
     * Searches for the given word in the inverted index JSON file.
     *
     * @param word The word to search for.
     * @return A list of books containing the word.
     */
    private List<String> searchWord(String word) {
        List<String> result = new ArrayList<>();

        try {
            // Read the entire inverted index JSON file
            String content = new String(Files.readAllBytes(Paths.get(INDEX_FILE_PATH)));
            JSONObject json = new JSONObject(content);

            // If the word exists in the JSON file, get the books containing the word
            if (json.has(word)) {
                JSONObject bookData = json.getJSONObject(word);
                // Add the book IDs (keys) to the result list
                for (String bookId : bookData.keySet()) {
                    result.add(bookId);  // You can modify this to return book names if available
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading or parsing the file: " + e.getMessage());
        }

        return result;
    }
}
