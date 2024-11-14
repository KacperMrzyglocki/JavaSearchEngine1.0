package com.example;

import java.util.Map;
import java.util.Scanner;

public class MainMenu {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Search books");
            System.out.print("?> ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    boolean searchCompleted = false;
                    while (!searchCompleted) {
                        InvertedIndex deserializedIndex = InvertedIndex.deserialize("/app/inverted_index.json");
                        System.out.print("Enter search term (or press Enter to cancel): ");
                        String term = scanner.nextLine().trim();

                        if (term.isEmpty()) {
                            System.out.println("Search term cannot be empty. Please try again.");
                        } else {
                            Map<Integer, Integer> results = deserializedIndex.search(term);
                            if (results.isEmpty()) {
                                System.out.println("No results found for \"" + term + "\".");
                            } else {
                                System.out.println("Results: " + results);
                            }
                            searchCompleted = true;
                        }
                    }
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
