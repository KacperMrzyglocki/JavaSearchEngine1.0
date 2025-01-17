package com.example.controller;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class StatsController {

    private static final String BOOKS_FILE = "/app/assets/books.json";

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /stats/numberbooks
        get("/stats/numberbooks", (req, res) -> {
            int numberOfBooks = countBooks();
            res.type("application/json");
            return gson.toJson(Collections.singletonMap("numberOfBooks", numberOfBooks));
        });

        // Endpoint GET /stats/booksByLanguage
        get("/stats/booksByLanguage", (req, res) -> {
            Map<String, List<Integer>> booksByLanguage = groupBooksBy("languages");
            res.type("application/json");
            return gson.toJson(booksByLanguage);
        });

        // Endpoint GET /stats/booksByAuthor
        get("/stats/booksByAuthor", (req, res) -> {
            Map<String, List<Integer>> booksByAuthor = groupBooksBy("authors");
            res.type("application/json");
            return gson.toJson(booksByAuthor);
        });

        // Endpoint GET /stats/topLanguage
        get("/stats/topLanguage", (req, res) -> {
            String topLanguage = findTopBy("languages");
            res.type("application/json");
            return gson.toJson(Collections.singletonMap("topLanguage", topLanguage));
        });

        // Endpoint GET /stats/topAuthor
        get("/stats/topAuthor", (req, res) -> {
            String topAuthor = findTopBy("authors");
            res.type("application/json");
            return gson.toJson(Collections.singletonMap("topAuthor", topAuthor));
        });
    }

    /**
     * Counts the number of books in the books.json file.
     *
     * @return The number of books.
     */
    private int countBooks() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(BOOKS_FILE)));
            JSONArray booksArray = new JSONArray(content);
            return booksArray.length();
        } catch (Exception e) {
            System.err.println("Error reading file: " + BOOKS_FILE + " - " + e.getMessage());
            return 0;
        }
    }

    /**
     * Groups book IDs by the specified key (e.g., "languages" or "authors").
     *
     * @param key The key to group by.
     * @return A map where the key is the specified attribute and the value is a list of book IDs.
     */
    private Map<String, List<Integer>> groupBooksBy(String key) {
        Map<String, List<Integer>> groupedBooks = new HashMap<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(BOOKS_FILE)));
            JSONArray booksArray = new JSONArray(content);

            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                String groupKey = book.getString(key);
                int bookId = book.getInt("id");

                groupedBooks.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(bookId);
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + BOOKS_FILE + " - " + e.getMessage());
        }
        return groupedBooks;
    }

    /**
     * Finds the most common attribute (e.g., language or author) in the books.json file.
     *
     * @param key The key to analyze (e.g., "languages" or "authors").
     * @return The most common value for the specified key.
     */
    private String findTopBy(String key) {
        Map<String, Long> countMap = new HashMap<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(BOOKS_FILE)));
            JSONArray booksArray = new JSONArray(content);

            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                String value = book.getString(key);
                countMap.put(value, countMap.getOrDefault(value, 0L) + 1);
            }

            return countMap.entrySet().stream()
                    .max(Comparator.comparingLong(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error reading file: " + BOOKS_FILE + " - " + e.getMessage());
            return null;
        }
    }
}
