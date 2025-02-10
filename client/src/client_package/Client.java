package client_package;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static Socket socket;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter the server IP: ");
        String serverAddress = scanner.nextLine();
        
        int port;
        while (true) {
            System.out.print("Enter the server port (5000-5050): ");
            try {
                port = Integer.parseInt(scanner.nextLine());
                if (port >= 5000 && port <= 5050) {
                    break;
                } else {
                    System.out.println("Port must be between 5000 and 5050.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        socket = new Socket(serverAddress, port);
        System.out.println("Connected to server on " + serverAddress + ":" + port);

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.print(in.readLine()); 
            String username = scanner.nextLine();
            out.println(username);

            System.out.print(in.readLine()); 
            String password = scanner.nextLine();
            out.println(password);

            String response = in.readLine();
            System.out.println(response);

            if (response.contains("Connection closed")) {
                return;
            }

            System.out.println("Chat History:");
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.equals("END_OF_HISTORY")) {
                    break; 
                }
                System.out.println(serverMessage);
            }

            System.out.println("You can now start typing your messages.");
            new Thread(() -> {
                try {
                    String messageFromServer;
                    while ((messageFromServer = in.readLine()) != null) {
                        System.out.println(messageFromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting the chat.");
                    break; 
                }
                out.println(message); 
            }
        } finally {
            socket.close();
            scanner.close();
        }
    }
}
