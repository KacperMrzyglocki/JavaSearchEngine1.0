package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class InvertedIndex {
    private final Map<String, Map<Integer, Integer>> index = new ConcurrentHashMap<>();
    private static final Set<String> stopwords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "not", "is", "of", "in", "on", "to", "by" // basic stopwords
    ));
    private static final Pattern punctuationPattern = Pattern.compile("[^a-zA-Z0-9 ]");
    private static final ReentrantLock lock = new ReentrantLock();

    public void addDocument(int docId, String text) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            word = punctuationPattern.matcher(word).replaceAll("");
            if (!word.isEmpty() && !stopwords.contains(word)) {
                lock.lock();
                try {
                    index.computeIfAbsent(word, k -> new ConcurrentHashMap<>()).merge(docId, 1, Integer::sum);
                    System.out.println("Added word: " + word + " for document ID: " + docId); // Debug output
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public Map<Integer, Integer> search(String query) {
        return index.getOrDefault(query.toLowerCase(), Collections.emptyMap());
    }

    public void buildIndex(String inputFile) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        try (FileReader reader = new FileReader(inputFile)) {
            JSONArray books = new JSONArray(new Scanner(reader).useDelimiter("\\Z").next());

            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                int docId = book.getInt("id");

                // Get the appropriate text URL from the book directly
                String url = getTextUrl(book);

                tasks.add(() -> {
                    String text = fetchTextFromUrl(url);
                    if (text != null) {
                        addDocument(docId, text); // Add the document
                    } else {
                        System.out.println("No text found for document ID: " + docId); // Debug output
                    }
                });
            }

            for (Runnable task : tasks) {
                executorService.submit(task);
            }

            executorService.shutdown();
            while (!executorService.isTerminated()) { }

            System.out.println("Index built successfully.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private String getTextUrl(JSONObject book) {
        // Print the book JSON object for debugging
        System.out.println("Book JSON: " + book.toString());

        // Check if the "url" key exists directly in the book object
        if (book.has("url")) {
            // Return the URL directly
            return book.getString("url");
        } else {
            // Log an error message if no URL is found
            System.out.println("No URL found for book ID: " + book.getInt("id")); // Debug output
        }

        return null; // Return null if no URL is found
    }

    private String fetchTextFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                String newUrl = conn.getHeaderField("Location");
                System.out.println("Redirected to: " + newUrl);
                return fetchTextFromUrl(newUrl);  // Recursively fetch from the new URL
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Error fetching URL: " + urlString + ", Response code: " + responseCode);
                return null;
            }

            Scanner sc = new Scanner(url.openStream());
            StringBuilder text = new StringBuilder();
            while (sc.hasNext()) {
                text.append(sc.nextLine()).append(" ");
            }
            sc.close();
            return text.toString();
        } catch (IOException e) {
            System.out.println("Error fetching text from URL: " + e.getMessage());
            return null;
        }
    }

    public void serialize(String outputFile) {
        try (FileWriter file = new FileWriter(outputFile)) {
            JSONObject json = new JSONObject(index);
            file.write(json.toString(4));
            System.out.println("Inverted index serialized to " + outputFile);
        } catch (IOException e) {
            System.out.println("Error serializing index: " + e.getMessage());
        }
    }

    public static InvertedIndex deserialize(String inputFile) {
        InvertedIndex invertedIndex = new InvertedIndex();
        try (FileReader reader = new FileReader(inputFile)) {
            JSONObject json = new JSONObject(new Scanner(reader).useDelimiter("\\Z").next());
            for (String word : json.keySet()) {
                JSONObject occurrences = json.getJSONObject(word);
                Map<Integer, Integer> docCounts = new ConcurrentHashMap<>();
                for (String docId : occurrences.keySet()) {
                    docCounts.put(Integer.parseInt(docId), occurrences.getInt(docId));
                }
                invertedIndex.index.put(word, docCounts);
            }
            System.out.println("Inverted index deserialized from " + inputFile);
        } catch (IOException e) {
            System.out.println("Error deserializing index: " + e.getMessage());
        }
        return invertedIndex;
    }
}
