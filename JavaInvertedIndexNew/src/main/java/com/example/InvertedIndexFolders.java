package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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

import java.io.File;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvertedIndexFolders {
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
                    //System.out.println("Added word: " + word + " for document ID: " + docId); // Debug output
                } finally {
                    lock.unlock();
                }
            }
        }
    }


    public void buildIndex(String inputFile) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

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

    public void serialize() {
        File directory = new File("/app/dictionary");

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                //System.out.println("Folder 'dictionary' został utworzony.");
            } else {
                System.out.println("Nie udało się utworzyć folderu 'dictionary'.");
            }
        } else {
            deleteDirectory(directory);
            if (directory.mkdirs()) {
                //System.out.println("Folder 'dictionary' został utworzony.");
            } else {
                System.out.println("Nie udało się utworzyć folderu 'dictionary'.");
            }

        }

        for (Map.Entry<String, Map<Integer, Integer>> outerEntry : index.entrySet()) {
            String word = outerEntry.getKey();

            //System.out.println("Word: " + word);

            char firstLetter = word.charAt(0);

            //System.out.println("Pierwsza litera: " + firstLetter);

            String fileName = "/app/dictionary/" + firstLetter + ".json";
            File file = new File(fileName);


            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        //System.out.println("Plik " + fileName + " został utworzony.");
                    } else {
                        System.out.println("Nie udało się utworzyć pliku " + fileName + ".");
                    }
                } catch (IOException e) {
                    System.out.println("Wystąpił błąd podczas tworzenia pliku: " + e.getMessage());
                }
            } else {
                //System.out.println("Plik " + fileName + " już istnieje.");
            }

            JSONObject mainJson = new JSONObject();


            JSONObject wordJson = new JSONObject(outerEntry.getValue());

            mainJson.put(word, wordJson);


            try (FileWriter file2 = new FileWriter(fileName, true)) { // 'true' oznacza tryb dopisywania
                file2.write(mainJson.toString(4));
                file2.write(System.lineSeparator()); // Dodanie nowej linii po każdym wpisie, jeśli potrzebne
                //System.out.println("Inverted index appended to " + fileName);
            } catch (IOException e) {
                System.out.println("Error appending to index: " + e.getMessage());
            }
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
                    deleteDirectory(file); // Usuń zawartość folderu rekurencyjnie
                }
            }
        }
        return dir.delete(); // Usuń pusty folder
    }
}