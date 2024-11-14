package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Crawler {
    public static void fetchBooks(String query, String author, String title, String language, String outputFile) {
        String baseUrl = "https://gutendex.com/books";
        Map<String, String> params = new HashMap<>();

        if (query != null) params.put("search", query);
        if (author != null) params.put("author", author);
        if (title != null) params.put("title", title);
        if (language != null) params.put("languages", language);

        StringBuilder urlWithParams = new StringBuilder(baseUrl + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlWithParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (urlWithParams.charAt(urlWithParams.length() - 1) == '&') {
            urlWithParams.setLength(urlWithParams.length() - 1);
        }

        System.out.println("Fetching data from URL: " + urlWithParams);

        try {
            URL url = new URL(urlWithParams.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != 200) {
                System.out.println("Failed to fetch data. Response code: " + conn.getResponseCode());
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            System.out.println("Raw JSON response: " + json);

            JSONObject jsonObject = new JSONObject(json.toString());
            JSONArray books = jsonObject.optJSONArray("results");

            if (books == null || books.isEmpty()) {
                System.out.println("No books found with the given criteria.");
            } else {
                JSONArray filteredBooks = new JSONArray();
                for (int i = 0; i < books.length(); i++) {
                    JSONObject book = books.getJSONObject(i);
                    JSONObject bookInfo = new JSONObject();
                    bookInfo.put("id", book.optInt("id"));
                    bookInfo.put("title", book.optString("title"));

                    JSONArray authorsArray = book.optJSONArray("authors");
                    String authors = "";
                    if (authorsArray != null) {
                        authors = authorsArray.toList().stream()
                                .map(obj -> ((Map<String, String>) obj).get("name"))
                                .collect(Collectors.joining(", "));
                    }
                    bookInfo.put("authors", authors);

                    JSONArray languagesArray = book.optJSONArray("languages");
                    String languages = "";
                    if (languagesArray != null) {
                        languages = languagesArray.toList().stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                    }
                    bookInfo.put("languages", languages);

                    String urlText = book.optJSONObject("formats").optString("text/plain; charset=us-ascii", "No URL");
                    bookInfo.put("url", urlText);

                    filteredBooks.put(bookInfo);
                }

                try (FileWriter file = new FileWriter(outputFile)) {
                    file.write(filteredBooks.toString(4));
                    System.out.println("Books saved to " + outputFile);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
