package Server_package;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {
    private static ServerSocket Listener;

    public static void main(String[] args) throws Exception {

        int clientNumber = 0;

        String serverAddress = "127.0.0.1";
        int serverPort = 0;

        // Saisie du port par l'utilisateur
        Scanner scanner = new Scanner(System.in);
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

        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        Listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
        try {

            while (true) {

                new ClientHandler(Listener.accept(), clientNumber++).start();
            }
        } finally {
            Listener.close();
        }
    }
}
