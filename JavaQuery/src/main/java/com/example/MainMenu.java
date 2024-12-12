package com.example;

import com.hazelcast.core.IExecutorService;
import org.json.JSONObject;

import java.util.Map;
import java.util.Scanner;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class MainMenu {
    private static final InvertedIndexFolders index = new InvertedIndexFolders();
    private static final Scanner scanner = new Scanner(System.in);
    private static final HazelcastInstance hazelcastInstance = 
    HazelcastConfig.getHazelcastInstance();

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Search books");
            System.out.print("?> ");
            int choice = -1;
            // wait for the input to be attached
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume the newline character
            } catch (Exception e) {
                System.out.println("No input detected.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            switch (choice) {
                case 1:
                    IExecutorService executorService = hazelcastInstance.getExecutorService("default");
                    executorService.submit(new IndexTask());
                    break;
                default:
                    exit=true;
            }
        }
    }

    static class IndexTask implements Runnable {
        @Override
        public void run() {
           boolean searchCompleted = false;
                    while (!searchCompleted) {
                        System.out.print("Enter search term (or press Enter to cancel): ");
                        String term = scanner.nextLine().trim();

                        if (term.isEmpty()) {
                            System.out.println("Search term cannot be empty. Please try again.");
                        } else {
                            char firstLetter = term.charAt(0);
                            char secondLetter = term.length() > 1 ? term.charAt(1) : '_';
                            String fileName = "/app/dictionary/" + firstLetter + "/" + secondLetter + ".json";
                            InvertedIndexFolders deserializedIndex = new InvertedIndexFolders();
                            deserializedIndex.deserialize(fileName);

                            Map<Integer, Integer> results = deserializedIndex.search(term);
                            if (results.isEmpty()) {
                                System.out.println("No results found for \"" + term + "\".");
                            } else {
                                System.out.println("Results: " + results);
                            }
                            searchCompleted = true;
                        }
                    }
        }
    }
}
