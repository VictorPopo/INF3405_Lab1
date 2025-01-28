package Server_package;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
public class Server {
	private static ServerSocket Listener;

	public static void main(String[] args) throws Exception {

			int clientNumber = 0;

			String serverAddress = "127.0.0.1";
			int serverPort = 5000;
			
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress,
		serverPort);
		try {
			
			while (true) {
			
				new ClientHandler(Listener.accept(), clientNumber++).start();
			}
		} finally {
			Listener.close();
		} } }
