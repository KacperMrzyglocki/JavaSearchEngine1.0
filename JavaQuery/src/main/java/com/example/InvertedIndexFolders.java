package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;


public class InvertedIndexFolders {
    //not used
    private final Map<String, Map<Integer, Integer>> index = new ConcurrentHashMap<>();
    private final int executors = 20;

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
                index.computeIfAbsent(word, k -> new ConcurrentHashMap<>()).merge(docId, 1, Integer::sum);
            }
        }
    }

    public void buildIndex(String inputFile) {
        ExecutorService executorService = Executors.newFixedThreadPool(executors);

        try (FileReader reader = new FileReader(inputFile)) {
            JSONArray books = new JSONArray(new Scanner(reader).useDelimiter("\\Z").next());

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                int docId = book.getInt("id");

                // Get the appropriate text URL from the book directly
                futures.add(CompletableFuture.runAsync(() -> {
                    String url = getTextUrl(book);
                    String text = fetchTextFromUrl(url);
                    if (text != null) {
                        addDocument(docId, text); // Add the document
                    } else {
                        System.out.println("No text found for document ID: " + docId); // Debug output
                    }
                }, executorService));
            }

            for (Future<?> future : futures) {
                future.get(); // Wait for all tasks to complete
            }

            executorService.shutdown();

            System.out.println("Index built successfully.");
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private String getTextUrl(JSONObject book) {
        // Print the book JSON object for debugging
        //System.out.println("Book JSON: " + book.toString());

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
                //System.out.println("Redirected to: " + newUrl);
                return fetchTextFromUrl(newUrl);  // Recursively fetch from the new URL
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Error fetching URL: " + urlString + ", Response code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append(" ");
            }
            reader.close();
            return text.toString();
        } catch (IOException e) {
            System.out.println("Error fetching text from URL: " + e.getMessage());
            return null;
        }
    }
    public void serialize() {
        File directory = new File("app/dictionary");

        if (directory.exists())
            deleteDirectory(directory);

        if (!directory.mkdirs())
            System.out.println("Failed to create 'dictionary' directory.");

        ExecutorService executorService = Executors.newFixedThreadPool(executors); // Use fixed thread pool for better performance

        List<Callable<Void>> tasks = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, Integer>> outerEntry : index.entrySet()) {
            tasks.add(() -> {
                serializeEntry(outerEntry);
                return null;
            });
        }

        try {
            executorService.invokeAll(tasks); // Invoke all tasks concurrently
        } catch (InterruptedException e) {
            System.out.println("Serialization interrupted: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }

        System.out.println("Serialization completed successfully.");
    }


    private void serializeEntry(Map.Entry<String, Map<Integer, Integer>> outerEntry) {
        String word = outerEntry.getKey();

        char firstLetter = word.charAt(0);
        char secondLetter = word.length() > 1 ? word.charAt(1) : '_'; // Handle single character words
        String fileName = "app/dictionary/"
                + firstLetter + "\\" + secondLetter + ".json";

        File file = new File(fileName);

        try {
            Files.createDirectories(file.getParentFile().toPath());
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
        }

        JSONObject mainJson = new JSONObject();
        JSONObject wordJson = new JSONObject(outerEntry.getValue());
        mainJson.put(word, wordJson);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(mainJson.toString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to index: " + e.getMessage());
        }
    }

    public void deserialize(String inputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            StringBuilder currentJson = new StringBuilder();
            String line;
            String currentKey = null;
            boolean isInsideObject = false;

            // Wczytywanie pliku linia po linii
            while ((line = reader.readLine()) != null) {
                // Usuwamy białe znaki na początku i końcu wiersza
                line = line.trim();

                if (line.contains("}}")) {
                    currentJson.append(line);
                    if (currentKey != null) {
                        // Zamiast dodawać String, tworzysz obiekt JSONObject
                        JSONObject wordJson = new JSONObject(currentJson.toString());
                        // Zaktualizuj index
                        for (String word : wordJson.keySet()) {
                            JSONObject wordData = wordJson.getJSONObject(word);
                            for (String docIdStr : wordData.keySet()) {
                                int docId = Integer.parseInt(docIdStr);
                                int count = wordData.getInt(docIdStr);
                                index.computeIfAbsent(word, k -> new ConcurrentHashMap<>()).merge(docId, count, Integer::sum);
                            }
                        }
                    }
                    // Resetowanie zmiennych do przetwarzania kolejnego obiektu
                    currentJson.setLength(0);
                    currentKey = null;
                    isInsideObject = false;
                } else if (line.contains("{")) {
                    // Początek obiektu, ustawiamy flagę
                    isInsideObject = true;
                    currentJson.append(line);
                    currentKey = line.trim().split(":")[0].replaceAll("[^a-zA-Z0-9]", "");
                } else if (isInsideObject) {
                    // Dodajemy kolejne linie do bieżącego obiektu
                    currentJson.append(line);
                }
            }

            System.out.println("Index deserialized successfully.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public Map<Integer, Integer> search(String query) {
        return index.getOrDefault(query.toLowerCase(), Collections.emptyMap());
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file); // Recursively delete directory contents
                }
            }
        }
        return dir.delete(); // Usuń pusty folder
    }
}