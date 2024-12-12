package com.example;

import java.util.Scanner;

public class MainMenu {
    private static final Scanner scanner = new Scanner(System.in);
    private static final InvertedIndex index = new InvertedIndex();

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Index books");
            System.out.print("?> ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    long startTime = System.currentTimeMillis();
                    index.buildIndex("/app/books.json");
                    long endTime = System.currentTimeMillis();

                    startTime = System.currentTimeMillis();
                    index.serialize("/app/inverted_index.json");
                    endTime = System.currentTimeMillis();

                    System.out.println("Time taken to build and serialize index: " + (endTime - startTime) + " ms");
                    System.out.println("Books updated and index built.");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
