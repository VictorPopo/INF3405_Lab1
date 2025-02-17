package client_package;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    
    /*
     * params: args: Array<string>
     * returns: void
     * Main function for client side
     * Handles validation and connection to the appropriated server
     * Creates new thread and socket per client
     * Handles disconnection from server 
     * */

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

                        System.out.print("\n" + messageFromServer + "\n");

                        System.out.print("[You - 127.0.0.1:64593 - " 
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss")) 
                            + "]: ");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            while (true) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss"));
                
                System.out.print("\n[You - 127.0.0.1:64593 - " + timestamp + "]: ");

                String message = scanner.nextLine().trim(); 

                if (message.isEmpty()) {
                    System.out.println("Error: Empty message is not allowed. Please enter a valid message.");
                    continue;
                }

                if (message.split("\\s+").length > 200) {
                    System.out.println("Error: Your message exceeds 200 words. Please enter a shorter message.");
                    continue;
                }

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
