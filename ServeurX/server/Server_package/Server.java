package Server_package;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Server {
    private static ServerSocket listener;
    static HashMap<String, String> users = new HashMap<>();
    private static final String USER_FILE = "users.txt";

    private static final Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        int serverPort = 0;
        Scanner scanner = new Scanner(System.in);

        loadUsers();

        while (true) {
            System.out.print("Enter the server port (5000-5050): ");
            try {
                serverPort = Integer.parseInt(scanner.nextLine());
                if (serverPort >= 5000 && serverPort <= 5050) {
                    break;
                } else {
                    System.out.println("Port must be between 5000 and 5050. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port format. Please enter a number.");
            }
        }
        scanner.close();

        listener = new ServerSocket(serverPort);
        System.out.format("The server is running on 127.0.0.1:%d%n", serverPort);

        try {
            while (true) {
                Socket clientSocket = listener.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                synchronized (clientHandlers) {
                    clientHandlers.add(clientHandler);
                }
                new Thread(clientHandler).start();  
            }
        } finally {
            listener.close();
        }
    }

    private static void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            System.out.println("No user file found, starting fresh.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]); 
                }
            }
            System.out.println("Users loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    static synchronized void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (var entry : users.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        synchronized (clientHandlers) {
            for (ClientHandler client : clientHandlers) {
                if (client != sender) { 
                    client.sendMessage(message);
                }
            }
        }
    }

}
