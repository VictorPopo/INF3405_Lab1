package Server_package;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String username;
    private String clientAddress;
    private int clientPort;
    private PrintWriter out;
    private static final String MESSAGE_LOG_FILE = "chat_history.txt";

    private static final Set<ClientHandler> clients = new HashSet<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientAddress = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
    }

    /*
     * params: void
     * returns: void
     * Main function to handle all issues related to one specific client.
     * Handles authentication, message broadcasting and logging
     * */
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            this.out = out;

            synchronized (clients) {
                clients.add(this);
            }

            out.println("Enter username:");
            username = in.readLine();

            out.println("Enter password:");
            String password = in.readLine();
            String hashedPassword = hashPassword(password);

            synchronized (Server.users) {
                if (Server.users.containsKey(username)) {
                    if (Server.users.get(username).equals(hashedPassword)) {
                        out.println("Login successful! You can now chat.");
                    } else {
                        out.println("Incorrect password. Connection closed.");
                        socket.close();
                        return;
                    }
                } else {
                    Server.users.put(username, hashedPassword);
                    Server.saveUsers();
                    out.println("Account created successfully! You can now chat.");
                }
            }

            sendLastMessages(out);

            String message;
            while ((message = in.readLine()) != null) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss").format(new Date());
                String formattedMessage = String.format("[%s - %s:%d - %s]: %s", 
                    username, clientAddress, clientPort, timestamp, message);

                System.out.println(formattedMessage);
                logMessage(formattedMessage);
                Server.broadcastMessage(formattedMessage,this);
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close socket.");
            }
            synchronized (clients) {
                clients.remove(this);
            }
        }
    }
    /*
     * params: message: string - Message to print
     * returns: void
     * Prints out a message to the user
     * */
    public void sendMessage(String message) {
        out.println(message);
    }

    /*
     * params: message: string - Message to log
     * returns: void
     * Logs a message in the appropriate file
     * */
    private void logMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGE_LOG_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving chat history: " + e.getMessage());
        }
    }
    
    /*
     * params: out: PrintWriter - Text output stream
     * returns: void
     * Retrieves messages from the appropriated text file and displays them to the user
     * */
	private void sendLastMessages(PrintWriter out) {
        try (BufferedReader reader = new BufferedReader(new FileReader(MESSAGE_LOG_FILE))) {
            String line;
            ArrayList<String> messages = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                messages.add(line);
            }


            int start = Math.max(0, messages.size() - 15);
            for (int i = start; i < messages.size(); i++) {
                String message = messages.get(i);
                out.println(message);  
            }
            out.println("END_OF_HISTORY");
        } catch (IOException e) {
            System.out.println("Error reading chat history: " + e.getMessage());
        }
    }
	/*
	 * params: password: string - Password of a user
	 * returns : string
	 * Handles hashing of the password of a new user
	 * */
    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
