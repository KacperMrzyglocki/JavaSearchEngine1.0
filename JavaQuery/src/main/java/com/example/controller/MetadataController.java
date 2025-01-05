package com.example.controller;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static spark.Spark.*;

public class MetadataController {

    private static final String METADATA_FILE = "/app/assets/books.json";

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /search/metadata/:id
        get("/search/metadata/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Map<String, Object> result = searchMetadataById(id);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/metadata/author/:author
        get("/search/metadata/author/:author", (req, res) -> {
            String author = req.params(":author");
            List<Map<String, Object>> result = filterMetadataByAuthor(author);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/metadata/language/:language
        get("/search/metadata/language/:language", (req, res) -> {
            String language = req.params(":language");
            List<Map<String, Object>> result = filterMetadataByLanguage(language);
            res.type("application/json");
            return gson.toJson(result);
        });

        // Endpoint GET /search/metadata/title/:title
        get("/search/metadata/title/:title", (req, res) -> {
            String title = req.params(":title");
            List<Map<String, Object>> result = filterMetadataByTitle(title);
            res.type("application/json");
            return gson.toJson(result);
        });
    }

    /**
     * Searches for the metadata based on the book ID.
     *
     * @param id The ID of the book.
     * @return The metadata of the book.
     */
    private Map<String, Object> searchMetadataById(int id) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(METADATA_FILE)));
            JSONArray books = new JSONArray(content);

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                if (book.getInt("id") == id) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", book.getInt("id"));
                    result.put("title", book.getString("title"));
                    result.put("authors", book.getString("authors"));
                    result.put("languages", book.getString("languages"));
                    result.put("url", book.getString("url"));
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing metadata file: " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * Filters metadata by author (case-insensitive).
     *
     * @param author The author to search for.
     * @return A list of books by the given author.
     */
    private List<Map<String, Object>> filterMetadataByAuthor(String author) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(METADATA_FILE)));
            JSONArray books = new JSONArray(content);

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                String bookAuthor = book.getString("authors").toLowerCase();
                if (bookAuthor.contains(author.toLowerCase())) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("id", book.getInt("id"));
                    metadata.put("title", book.getString("title"));
                    metadata.put("authors", book.getString("authors"));
                    metadata.put("languages", book.getString("languages"));
                    metadata.put("url", book.getString("url"));
                    result.add(metadata);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing metadata file: " + e.getMessage());
        }
        return result;
    }

    /**
     * Filters metadata by language.
     *
     * @param language The language to filter by.
     * @return A list of books in the given language.
     */
    private List<Map<String, Object>> filterMetadataByLanguage(String language) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(METADATA_FILE)));
            JSONArray books = new JSONArray(content);

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                if (book.getString("languages").equalsIgnoreCase(language)) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("id", book.getInt("id"));
                    metadata.put("title", book.getString("title"));
                    metadata.put("authors", book.getString("authors"));
                    metadata.put("languages", book.getString("languages"));
                    metadata.put("url", book.getString("url"));
                    result.add(metadata);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing metadata file: " + e.getMessage());
        }
        return result;
    }

    /**
     * Filters metadata by title (case-insensitive).
     *
     * @param title The title fragment to search for.
     * @return A list of books whose titles contain the given fragment.
     */
    private List<Map<String, Object>> filterMetadataByTitle(String title) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(METADATA_FILE)));
            JSONArray books = new JSONArray(content);

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                String bookTitle = book.getString("title").toLowerCase();
                if (bookTitle.contains(title.toLowerCase())) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("id", book.getInt("id"));
                    metadata.put("title", book.getString("title"));
                    metadata.put("authors", book.getString("authors"));
                    metadata.put("languages", book.getString("languages"));
                    metadata.put("url", book.getString("url"));
                    result.add(metadata);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading or parsing metadata file: " + e.getMessage());
        }
        return result;
    }
}
