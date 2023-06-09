package org.example;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter : ");
        String prompt = scanner.nextLine();
        ChatGPT chatGPT = new ChatGPT("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");
        String response = chatGPT.Chat(prompt);
        System.out.println(response);
    }
}