package com.example.controller;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.nio.file.Paths;
import java.util.*;

import java.nio.file.Files;


import static spark.Spark.*;

public class DictionaryController {

    private static final String DICTIONARY_FOLDER = "JavaQuery/dictionary1_json";

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /hello
        get("/hello", (req, res) -> {
            res.type("application/json");
            return "{\"message\": \"Hello, World!\"}";
        });

        // Endpoint GET /search/dictionary/:word
        get("/search/dictionary/:word", (req, res) -> {
            String word = req.params(":word");
            Map<String, Integer> result = searchWord(word);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/dictionary/or/:word1/:word2
        get("/search/dictionary/or/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            Map<String, Integer> resultWord1 = searchWord(word1);
            Map<String, Integer> resultWord2 = searchWord(word2);

            // Combine books from both words without duplicates
            Map<String, Integer> combinedResult = new HashMap<>(resultWord1);
            resultWord2.forEach((book, count) -> combinedResult.merge(book, count, Integer::sum));

            res.type("application/json");
            return gson.toJson(combinedResult);
        });

        // Endpoint GET /search/dictionary/and/:word1/:word2
        get("/search/dictionary/and/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            Map<String, Integer> resultWord1 = searchWord(word1);
            Map<String, Integer> resultWord2 = searchWord(word2);

            // Find intersection of books
            Map<String, Integer> intersectionResult = new HashMap<>();
            for (String book : resultWord1.keySet()) {
                if (resultWord2.containsKey(book)) {
                    intersectionResult.put(book, resultWord1.get(book) + resultWord2.get(book));
                }
            }

            res.type("application/json");
            return gson.toJson(intersectionResult);
        });
    }

    /**
     * Searches for the given word in the dictionary JSON files.
     *
     * @param word The word to search for.
     * @return A map of books and their occurrence counts.
     */
    private Map<String, Integer> searchWord(String word) {
        Map<String, Integer> result = new HashMap<>();
        String fileName = word.substring(0, 1).toLowerCase() + ".json"; // Get the appropriate file based on the first letter
        String filePath = Paths.get(DICTIONARY_FOLDER, fileName).toString();

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath))); // Read file content
            JSONObject json = new JSONObject(content);

            if (json.has(word)) {
                JSONObject bookData = json.getJSONObject(word);
                for (String book : bookData.keySet()) {
                    result.put(book, bookData.getInt(book));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing file: " + filePath + " - " + e.getMessage());
        }

        return result;
    }

}
