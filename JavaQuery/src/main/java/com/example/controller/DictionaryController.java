package com.example.controller;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static spark.Spark.*;

public class DictionaryController {

    private static final String DICTIONARY_FOLDER = "JavaQuery/assets/dictionary";

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /search/dictionary/:word
        get("/search/dictionary/:word", (req, res) -> {
            String word = req.params(":word");
            List<String> result = searchWord(word);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/dictionary/or/:word1/:word2
        get("/search/dictionary/or/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchWord(word1);
            List<String> resultWord2 = searchWord(word2);

            // Combine books from both words without duplicates
            Set<String> combinedResult = new HashSet<>(resultWord1);
            combinedResult.addAll(resultWord2);

            res.type("application/json");
            return gson.toJson(new ArrayList<>(combinedResult));
        });

        // Endpoint GET /search/dictionary/and/:word1/:word2
        get("/search/dictionary/and/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            List<String> resultWord1 = searchWord(word1);
            List<String> resultWord2 = searchWord(word2);

            // Find intersection of books
            resultWord1.retainAll(resultWord2);

            res.type("application/json");
            return gson.toJson(resultWord1);
        });
    }

    /**
     * Searches for the given word in the dictionary JSON files.
     *
     * @param word The word to search for.
     * @return A list of book IDs where the word occurs.
     */
    private List<String> searchWord(String word) {
        List<String> result = new ArrayList<>();
        String fileName = word.substring(0, 1).toLowerCase() + ".json"; // Get the appropriate file based on the first letter
        String filePath = Paths.get(DICTIONARY_FOLDER, fileName).toString();

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath))); // Read file content
            JSONObject json = new JSONObject(content);

            if (json.has(word)) {
                JSONObject bookData = json.getJSONObject(word);
                result.addAll(bookData.keySet());
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing file: " + filePath + " - " + e.getMessage());
        }

        return result;
    }
}
