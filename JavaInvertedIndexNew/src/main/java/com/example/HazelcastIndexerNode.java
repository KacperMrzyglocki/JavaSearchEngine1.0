package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class HazelcastIndexerNode {

    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^a-zA-Z0-9 ]");
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "not", "is", "of", "in", "on", "to", "by" // basic stopwords
    ));

    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        // Hazelcast Configuration
        Config config = new Config();

        // Ustawienia sieciowe Hazelcast
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPublicAddress("192.168.1.44:5701"); // Publiczny adres IP węzła

        networkConfig.getInterfaces()
                .setEnabled(true)
                .addInterface("192.168.1.*"); // Dostosuj do swojej sieci lokalnej

        networkConfig.getJoin().getTcpIpConfig()
                .addMember("192.168.1.44") // Adres głównego węzła
                .addMember("192.168.1.194") // Adres drugiego węzła
                .setEnabled(true);

        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        // Dodatkowe właściwości dla Dockera
        System.setProperty("hazelcast.local.localAddress", "192.168.1.44");

        // Tworzenie instancji Hazelcast
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        IMap<String, Map<Integer, Integer>> invertedIndex = hazelcastInstance.getMap("invertedIndex");

        // Ścieżka do pliku wejściowego
        String inputFile = "/app/assets/books.json"; // Plik wejściowy JSON

        // Budowanie indeksu
        buildIndex(inputFile, invertedIndex);

        // Informacja o zakończeniu
        System.out.println("Indexing completed. Index is stored in Hazelcast cluster.");
    }


    private static void buildIndex(String inputFile, IMap<String, Map<Integer, Integer>> invertedIndex) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (FileReader reader = new FileReader(inputFile)) {
            JSONArray books = new JSONArray(new Scanner(reader).useDelimiter("\\Z").next());

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                int docId = book.getInt("id");
                String url = book.getString("url");

                futures.add(CompletableFuture.runAsync(() -> {
                    String text = fetchTextFromUrl(url);
                    if (text != null) {
                        indexDocument(docId, text, invertedIndex);
                    } else {
                        System.out.println("No text found for document ID: " + docId); // Debug output
                    }
                }, executorService));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }

            executorService.shutdown();
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Error building index: " + e.getMessage());
        }
    }

    private static void indexDocument(int docId, String text, IMap<String, Map<Integer, Integer>> invertedIndex) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            word = PUNCTUATION_PATTERN.matcher(word).replaceAll("");
            if (!word.isEmpty() && !STOPWORDS.contains(word)) {
                invertedIndex.lock(word);
                try {
                    Map<Integer, Integer> wordData = invertedIndex.getOrDefault(word, new ConcurrentHashMap<>());
                    wordData.merge(docId, 1, Integer::sum);
                    invertedIndex.put(word, wordData);
                } finally {
                    invertedIndex.unlock(word);
                }
            }
        }
    }

    private static String fetchTextFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);  // Ważne: ustawiamy na false, aby ręcznie obsłużyć przekierowanie
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                // Obsługa przekierowania
                String newUrl = conn.getHeaderField("Location");
                System.out.println("Redirected to: " + newUrl);  // Debugging: pokazanie nowego URL
                return fetchTextFromUrl(newUrl);  // Rekursywnie pobierz tekst z nowego URL
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                // Jeśli kod odpowiedzi to 200, pobieramy tekst
                BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append("\n");
                }
                reader.close();
                return text.toString();
            } else {
                System.out.println("Error fetching URL: " + urlString + ", Response code: " + responseCode);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error fetching text from URL: " + e.getMessage());
            return null;
        }
    }

}
