package com.example;

import java.util.Scanner;

public class MainMenu {
    private static final Scanner scanner = new Scanner(System.in);
    private static final InvertedIndexFolders index = new InvertedIndexFolders();

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("===== Main Menu =====");
            System.out.println("1. Index books locally");
            System.out.print("?> ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    index.buildIndex("/app/assets/books.json");
                    index.serialize();
                    System.out.println("Local indexing completed.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
