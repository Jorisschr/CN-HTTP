import java.io.*;
import java.net.*;

public class Server {

	public static void main(String[] args) throws Exception {
		System.out.println("SERVER: starting...");
		ServerSocket welcomeSocket = new ServerSocket(6791);
		 while(true) {
			System.out.println("SERVER: Waiting for connection "
						+ "on port 6791...");
			Socket connectionSocket = welcomeSocket.accept();
			System.out.println("Socket accepted.");
			BufferedReader inFromClient = new BufferedReader(new
				InputStreamReader (connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream
					(connectionSocket.getOutputStream());
			String clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);
			String capsSentence = clientSentence.toUpperCase() + '\n';
			 outToClient.writeBytes(capsSentence);
		}
	}

}
