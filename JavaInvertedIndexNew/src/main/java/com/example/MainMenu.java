package com.example;

import java.util.Scanner;

public class MainMenu {
    private static final Scanner scanner = new Scanner(System.in);
    private static final InvertedIndexFolders index = new InvertedIndexFolders(); // Lokalny indeks
    private static final HazelcastIndexer hazelcastIndexer = new HazelcastIndexer(); // Hazelcast indeks

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("===== Main Menu =====");
            System.out.println("1. Index books locally");
            System.out.println("2. Index books in Hazelcast");
            System.out.println("3. Search word in Hazelcast");
            System.out.println("4. Print Hazelcast index");
            System.out.println("0. Exit");
            System.out.print("?> ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    index.buildIndex("/app/books.json");
                    index.serialize();
                    System.out.println("Local indexing completed.");
                    break;

                case 2:
                    System.out.println("Indexing books in Hazelcast...");
                    hazelcastIndexer.buildIndex("/app/books.json");
                    System.out.println("Hazelcast indexing completed.");
                    break;

                case 3:
                    System.out.print("Enter word to search in Hazelcast: ");
                    String word = scanner.nextLine();
                    System.out.println("Occurrences in Hazelcast: " + hazelcastIndexer.search(word));
                    break;

                case 4:
                    System.out.println("Printing Hazelcast index...");
                    hazelcastIndexer.printIndex();
                    break;

                case 0:
                    exit = true;
                    hazelcastIndexer.shutdown();
                    System.out.println("Exiting application...");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
