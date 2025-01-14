package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.NoSuchElementException;


public class MainMenu {
    private static final Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        System.out.println("System will wait for an input");
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Crawl books from Gutendex");
            System.out.print("?> ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (NoSuchElementException e) {
                System.out.println("No input device found.");
                exit = true;
            }
            
            switch (choice) {
                case 1:
                    System.out.print("Enter keywords (or press Enter for none): ");
                    String query = scanner.nextLine().trim();
                    query = query.isEmpty() ? null : query;

                    System.out.print("Enter language (or press Enter for default 'en'): ");
                    String language = scanner.nextLine().trim();
                    language = language.isEmpty() ? "en" : language;
                    
                     Crawler.fetchBooks(query, language, null, null, "/app/books.json");
                    break;
                default:
                    exit = true;
            }
        }
        scanner.close();
    }
}
