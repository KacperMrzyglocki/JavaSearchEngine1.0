package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;
import java.util.Scanner;

public class HazelcastQueryNode {
    public static void main(String[] args) {
        // Hazelcast Client Configuration
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("192.168.1.44", "192.168.1.194");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        IMap<String, Map<Integer, Integer>> invertedIndex = client.getMap("invertedIndex");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Query Node is connected. Enter words to search:");
        while (true) {
            System.out.print("Search: ");
            String word = scanner.nextLine().toLowerCase();
            if (word.equals("exit")) {
                break;
            }

            Map<Integer, Integer> results = invertedIndex.get(word);
            if (results != null && !results.isEmpty()) {
                System.out.println("Results for '" + word + "': " + results);
            } else {
                System.out.println("No results found for '" + word + "'.");
            }
        }

        client.shutdown();
        System.out.println("Query Node disconnected.");
    }
}