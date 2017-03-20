package httpServer;
import java.io.*;
import java.net.*;

public class Server implements Runnable {
	private int port;

	// TODO: multithreaded to support multiple clients. test in client test... servertest?
	public Server(int port) {
		this.port = port;
	}
	
	private int getPort() {
		return this.port;
	}

	@Override
	public void run() {
		ServerSocket welcomeSocket;
		//System.out.println("Server running.");
		try {
			welcomeSocket = new ServerSocket(getPort());
			while (true) {
				//System.out.println("Server running.");
				Socket connectionSocket = welcomeSocket.accept();
				if (connectionSocket != null) {
					Handler h = new Handler(connectionSocket);
					Thread thread = new Thread(h);
					thread.start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
} 

