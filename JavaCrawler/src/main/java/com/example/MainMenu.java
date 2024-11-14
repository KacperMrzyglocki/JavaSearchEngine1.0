package com.example;

import java.util.Scanner;

public class MainMenu {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Crawl books from Gutendex");
            System.out.print("?> ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter keywords (or press Enter for none): ");
                    String query = scanner.nextLine().trim();
                    query = query.isEmpty() ? null : query;

                    System.out.print("Enter language (or press Enter for default 'en'): ");
                    String language = scanner.nextLine().trim();
                    language = language.isEmpty() ? "en" : language;

                    Crawler.fetchBooks(query, language, null, null, "/app/books.json");
                    System.out.print("Done");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
