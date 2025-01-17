package com.example.controller;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.google.gson.Gson;

import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class HazelcastController {

    private final HazelcastInstance hazelcastClient;
    private final IMap<String, Map<Integer, Integer>> invertedIndex;

    public HazelcastController() {
        // Configure Hazelcast client
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("192.168.1.44", "192.168.1.194");

        this.hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
        this.invertedIndex = hazelcastClient.getMap("invertedIndex");
    }

    public void registerRoutes() {
        Gson gson = new Gson();

        // Endpoint GET /documents/:words
        // Example: /documents/word1+word2+word3
        get("/documents/:words", (req, res) -> {
            String wordsParam = req.params(":words");
            List<String> words = Arrays.asList(wordsParam.split("\\+")); // Split by '+'

            // Search documents containing the words
            Map<Integer, Integer> results = searchDocuments(words);

            res.type("application/json");
            return gson.toJson(results);
        });
    }

    /**
     * Searches for documents containing any of the given words in the inverted index.
     *
     * @param words List of words to search for.
     * @return A map of document IDs and their combined word counts.
     */
    private Map<Integer, Integer> searchDocuments(List<String> words) {
        Map<Integer, Integer> resultMap = new HashMap<>();

        for (String word : words) {
            Map<Integer, Integer> wordResults = invertedIndex.get(word.toLowerCase());
            if (wordResults != null) {
                for (Map.Entry<Integer, Integer> entry : wordResults.entrySet()) {
                    resultMap.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
        }

        return resultMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Sort by word count descending
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
