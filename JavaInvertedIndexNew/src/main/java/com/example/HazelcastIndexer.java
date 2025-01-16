package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class HazelcastIndexer {

    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, Map<Integer, Integer>> invertedIndex;
    private static final Pattern punctuationPattern = Pattern.compile("[^a-zA-Z0-9 ]");
    private static final String STOPWORDS = "a,an,the,and,or,not,is,of,in,on,to,by"; // Podstawowe stopwords

    public HazelcastIndexer() {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(true);

        // Inicjalizacja Hazelcast
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
        this.invertedIndex = hazelcastInstance.getMap("invertedIndex");
    }

    public void buildIndex(String inputFile) {
        try (FileReader reader = new FileReader(inputFile)) {
            JSONArray books = new JSONArray(new Scanner(reader).useDelimiter("\\Z").next());

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                int docId = book.getInt("id");

                if (book.has("url")) {
                    String url = book.getString("url");
                    System.out.println("Fetching text from URL: " + url);
                    String text = fetchTextFromUrl(url);
                    if (text != null) {
                        indexDocument(docId, text);
                    } else {
                        System.out.println("Failed to fetch text for book ID: " + docId);
                    }
                } else {
                    System.out.println("Warning: Book with id " + docId + " has no 'url' field.");
                }
            }

            System.out.println("Indexing completed in Hazelcast.");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void indexDocument(int docId, String text) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            word = punctuationPattern.matcher(word).replaceAll(""); // Usunięcie interpunkcji
            if (!word.isEmpty() && !STOPWORDS.contains(word)) {
                invertedIndex.lock(word); // Hazelcast zapewnia mechanizm blokowania
                try {
                    // Pobierz istniejącą mapę dla danego słowa lub utwórz nową
                    Map<Integer, Integer> wordData = invertedIndex.getOrDefault(word, new ConcurrentHashMap<>());

                    // Zwiększ licznik dla danego docId
                    wordData.merge(docId, 1, Integer::sum);

                    // Zapisz zaktualizowaną mapę do Hazelcast
                    invertedIndex.put(word, wordData);
                } finally {
                    invertedIndex.unlock(word);
                }
            }
        }
    }


    private String fetchTextFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false); // Nie automatycznie, ręcznie obsługujemy przekierowania
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                String newUrl = conn.getHeaderField("Location");
                System.out.println("Redirected to: " + newUrl);
                return fetchTextFromUrl(newUrl); // Rekurencyjne pobieranie z nowego URL
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to fetch text. HTTP response code: " + responseCode);
                return null;
            }

            StringBuilder text = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append(" ");
                }
            }
            return text.toString();
        } catch (IOException e) {
            System.err.println("Error fetching text from URL: " + e.getMessage());
            return null;
        }
    }

    public Map<Integer, Integer> search(String word) {
        return invertedIndex.getOrDefault(word.toLowerCase(), Map.of());
    }

    public void printIndex() {
        for (Map.Entry<String, Map<Integer, Integer>> entry : invertedIndex.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    public void shutdown() {
        hazelcastInstance.shutdown();
    }
}
