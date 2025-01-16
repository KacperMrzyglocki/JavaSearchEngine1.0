package com.example.controller;

import com.example.HazelcastClientConnection;
import com.google.gson.Gson;

import static spark.Spark.*;

import java.util.*;

public class HazelcastController {

    private final HazelcastClientConnection hazelcastClient;
    private final Gson gson;

    public HazelcastController(HazelcastClientConnection hazelcastClient) {
        this.hazelcastClient = hazelcastClient;
        this.gson = new Gson();
    }

    public void registerRoutes() {
        // Endpoint GET /search/hazelcast/:word
        get("/search/hazelcast/:word", (req, res) -> {
            String word = req.params(":word");
            Map<Integer, Integer> result = hazelcastClient.searchWord(word);  // Wyszukiwanie w istniejącym klastrze
            res.type("application/json");

            if (result == null) {
                return gson.toJson(Collections.emptyMap());  // Zwrócenie pustej mapy jeśli nie znaleziono słowa
            }

            return gson.toJson(result);  // Zwrócenie wyników wyszukiwania w formacie JSON
        });

        // Endpoint GET /search/hazelcast/or/:word1/:word2
        get("/search/hazelcast/or/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            Map<Integer, Integer> resultWord1 = hazelcastClient.searchWord(word1);
            Map<Integer, Integer> resultWord2 = hazelcastClient.searchWord(word2);

            // Łączenie wyników bez duplikatów (łączy mapy wyników)
            Map<Integer, Integer> combinedResult = new HashMap<>();
            if (resultWord1 != null) {
                combinedResult.putAll(resultWord1);
            }
            if (resultWord2 != null) {
                resultWord2.forEach((docId, count) ->
                        combinedResult.merge(docId, count, Integer::sum)
                );
            }

            res.type("application/json");
            return gson.toJson(combinedResult);  // Zwraca połączone wyniki
        });

        // Endpoint GET /search/hazelcast/and/:word1/:word2
        get("/search/hazelcast/and/:word1/:word2", (req, res) -> {
            String word1 = req.params(":word1");
            String word2 = req.params(":word2");
            Map<Integer, Integer> resultWord1 = hazelcastClient.searchWord(word1);
            Map<Integer, Integer> resultWord2 = hazelcastClient.searchWord(word2);

            // Znajdowanie przecięcia wyników
            Map<Integer, Integer> intersectionResult = new HashMap<>();
            if (resultWord1 != null && resultWord2 != null) {
                resultWord1.forEach((docId, count1) -> {
                    if (resultWord2.containsKey(docId)) {
                        intersectionResult.put(docId, Math.min(count1, resultWord2.get(docId)));
                    }
                });
            }

            res.type("application/json");
            return gson.toJson(intersectionResult);  // Zwraca przecięcie wyników
        });
    }
}
