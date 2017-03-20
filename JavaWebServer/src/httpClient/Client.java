package httpClient;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client implements Runnable{
	private static String[] commands = new String[] {"HEAD", "GET", "PUT", "POST"};
	
	private BufferedReader inFromUser;
	private HTMLWriter writer;

	private String contentType;
	private boolean chunked;

	private String host;
	private String path;
	private int port;
	
	private ArrayList<String> foundImageLocations = new ArrayList<String>();

	private int contentLength;
	
	public Client() {
		this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
		setChunked(false);
	}
	
	private void setWriter(String fileName) {
		this.writer = new HTMLWriter(this, fileName);
	}
	
	private HTMLWriter getWriter() {
		return this.writer;
	}
	
	private ArrayList<String> getImageLocs() {
		return this.foundImageLocations;
	}

	/**
	 * Check if the given input is correct and 
	 * execute the command if it is.
	 * If not correct, new input while be asked for.
	 * @param 	s
	 * 			A String containing the input.
	 * @throws 	Exception
	 */
	public void handleUserInput(String s) throws Exception {
		String[] input = parseUserInput(s);
		while(!isCorrectInput(input)) {
			input = parseUserInput(requestInput());
		}
		setHost(input[2]);
		setPath(input[2]);

		
		Socket clientSocket;
		try {
			setPort(Integer.parseInt(input[3]));
			clientSocket = new Socket(getHost(), getPort());
			executeCommand(clientSocket, input);
		} catch (NumberFormatException e) {
			System.out.println("Client: It appears you have entered "
					+ "an incorrect port number.\n");
			this.run();
		} catch (UnknownHostException e) {
			System.out.println("Client: It appears the requested "
					+ "URI does not exist,\nClient: or you are "
					+ "not connected to the internet.\n");
			this.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private int getPort() {
		return this.port;
	}

	private void setPort(int parseInt) {
		this.port = parseInt;
	}

	/**
	 * Split the users input into its different parameters.
	 * @param 	s
	 * 			The String containing the input of the user.
	 * @return	A String array containing the different parameters of the input.
	 */
	public String[] parseUserInput(String s) {
		return s.split(" ");
	}
	
	//TODO: Exception
	//TODO: Check if chunked response, length of read, if chunked, read footers
	//TODO: checken op charset.
	/**
	 * Execute the given input with the given clientSocket.
	 * @param 	clientSocket
	 * 			The socket used to communicate with a server.
	 * @param 	input
	 * 			A string array containing the parsed user input.
	 * @throws Exception
	 */
	public void executeCommand(Socket clientSocket, String[] input) throws Exception {
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		InputStream is = clientSocket.getInputStream();
		BufferedReader inFromServer = new BufferedReader(new 
				InputStreamReader(is, "UTF-8"));
		outToServer.writeBytes(input[1] + " " + getPath() + " HTTP/1.1\r\n");
		outToServer.writeBytes("Host: " + getHost() + "\r\n\r\n");
		switch (input[1]) {
		case "GET":
			String newLoc;
			if ((newLoc = checkStatusCode(inFromServer)) == null) {
				checkHeaders(inFromServer);
				if (getContentType().equals("text/html")) handleGetHTMLCommand(inFromServer);
				else if (getContentType().equals("image/png")) handleGetImageCommand(is);
			} else {
				System.out.println("Client: Redirecting...");
				redirectRequest(newLoc, input);
			}
			break;
		case "HEAD": 
			handleHeadCommand(inFromServer);
			break;
		case "PUT": 
			handlePutCommand(inFromServer);
			break;
		case "POST":
			handlePostCommand();
			break;
		}
		//clientSocket.close();
	}
	
	/**
	 * Used for retrieving information from headers.
	 * @param inFromServer
	 * @throws Exception
	 */
	private void checkHeaders(BufferedReader inFromServer) throws Exception {
		String response;
		while (!(response = inFromServer.readLine()).equals("")) {
			if (response.startsWith("Content-Type:")) {
				setContentType(response.split(" ")[1].split(";")[0]);
			}
			if (response.startsWith("Transfer-Encoding: chunked")) {
				setChunked(true);
			}
			if (response.startsWith("Content-Length:")) {
				setContentLength(Integer.parseInt(response.split(" ")[1]));
			}
			System.out.println("Server: " + response);
		}
		System.out.println("Server: " + response);
	}

	private void setContentLength(int parseInt) {
		this.contentLength = parseInt;
	}
	
	private int getContentLength() {
		return this.contentLength;
	}

	private void setChunked(boolean b) {
		this.chunked = b;
	}
	
	private boolean isChunked() {
		return this.chunked;
	}

	private void setContentType(String s) {
		this.contentType = s;
	}
	
	private String getContentType() {
		return this.contentType;
	}

	//TODO: together with writing server.
	private void handlePostCommand() {
		// TODO Auto-generated method stub
		System.out.println("Client: Please enter the data to post.");
	}

	//TODO: together with writing server.
	private void handlePutCommand(BufferedReader inFromServer) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Client: Please enter the data to put.");
	}

	//TODO: save input
	private void handleHeadCommand(BufferedReader inFromServer) throws Exception{
		String response = " ";
		while(!response.equals("")) {
			response = inFromServer.readLine();
			System.out.println("Server: " + response);
		}
	}
	
	private void handleGetHTMLCommand(BufferedReader inFromServer) throws Exception {
		String response = "";
		setWriter(getHost());
		while(!response.endsWith("</HTML>") && !response.endsWith("</html>")) {
			response = inFromServer.readLine();
			if (response.startsWith("<")) getWriter().setWriting(true);
			System.out.println("Server: " + response);
			response = scanHTMLLine(response);
			getWriter().write(response);
		}
		if (isChunked()) {
			while (!response.equals("")) {
				response = inFromServer.readLine();
				System.out.println("Server: " + response);
			}
		}
		getWriter().close();
		for (String location: getImageLocs()) {
			handleUserInput("HTTPClient GET " + getHost() + location + " "+ getPort());
		}
	}
	
	private void handleGetImageCommand(InputStream is) throws Exception {
		OutputStream os = new FileOutputStream("src/savedHTMLFiles/" + getPath().replace("/", "_"));
		byte[] image = new byte[getContentLength()];
		int length;
		while ((length = is.read(image)) != -1) {

		}
		os.write(image);
		System.out.println("wtf " + is.read(image, 0, getContentLength()));
		//System.out.println(image);

		//os.write(image);
		//os.close();
		//is.close();
	}
	
	/**
	 * Check the status code sent by the server. This function is called 
	 * to read the first line of the servers response.
	 * @param 	inFromServer
	 * 			The BufferedReader used to read the output of the server.
	 * @return	If the status code is of type 3, a string containing the 
	 * 			new location will be returned. Else null is returned.
	 * @throws Exception
	 */
	private String checkStatusCode(BufferedReader inFromServer) throws Exception{
		String response;
		if ((response = inFromServer.readLine()).split(" ")[1].charAt(0) == '3') {
			System.out.println("Server: " + response);
			while (!response.startsWith("Location:")) {
				response = inFromServer.readLine();
			}
			System.out.println("Server: " + response);
			return response.split(" ")[1];
		}
		System.out.println("Server: " + response);
		return null;
	}

	/**
	 * Redirect the previous GET request from the user 
	 * to another location. This function is called when 
	 * the server responds with a status code of type 3.
	 * @param 	newLoc
	 * 			The new location to which a socket should be set up.
	 * @param 	port
	 * 			The port for the new socket.
	 * @throws Exception
	 */
	private void redirectRequest(String newLoc, String[] input) throws Exception {
		handleUserInput(input[0] + " " + input[1] + " " + newLoc + " " + input[3]);
	}

	
	/**
	 * Check whether the given String array input is correct.
	 * @param 	input
	 * 			A string array containing the different parts of a users command.
	 * @return	True if and only if the given array consists of 
	 * 			4 elements and the given command is correct. False otherwise.
	 */
	public boolean isCorrectInput(String[] input) {
		if (input.length != 4) {
			System.out.println("Client: Please make sure your command "
					+ "contains 4 arguments.");
			return false;
		}
		if (isCorrectCommand(input[1])) {
			return true;
		}
		System.out.println("Client: Incorrect command, only GET, HEAD, PUT and POST "
				+ "are supported by this HTTP Client.");
		return false;
	}
	
	/**
	 * Check whether the given command is supported by this client.
	 * @param 	command
	 * 			The command to check.
	 * @return	True if and only if the command is supported.
	 * 			The command is one of the following:  GET, PUT, HEAD, POST.
	 * 			Returns false otherwise.
	 */
	public boolean isCorrectCommand(String command) {
		for (String i: commands) if (i.equals(command)) return true;
		return false;
	}
	
	/**
	 * @return	A String containing the input of the user.
	 * @throws Exception
	 */
	public String requestInput() throws Exception {
		System.out.println("Client: Please enter your command.");
		return inFromUser.readLine();
	}
	
	/**
	 * Return a String containing the path of the given URI.
	 * @param 	uri
	 * 			The URI given in the command for the client.
	 * @return	The path to the file location used for the HTTP request.
	 */
	public String getPath(String uri) {
		if (uri.contains("://")) {
			uri = uri.substring(uri.indexOf(':') + 3);
		}
		if (uri.indexOf('/') == -1) {
			return "/";
		}
		return uri.substring(uri.indexOf('/'));
	}
	
	public void setHost(String uri) {
		try {
			this.host = getHost(uri);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getHost() {
		return this.host;
	}
	
	public void setPath(String uri) {
		this.path = getPath(uri);
	}
	
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Return a String containing the host of the given URI.
	 * @param 	uri
	 * 			The URI given in the command for the client.
	 * @return	The host used for the HTTP request.
	 * @throws Exception 
	 */
	public String getHost(String uri) throws Exception {
		if (uri.startsWith("https")) {
			System.out.println("Client: Sorry https is not supported by this client.");
			throw new Exception();
		}
		if (uri.contains("://")) {
			uri = uri.substring(uri.indexOf(':') + 3);
		}
		if (uri.indexOf('/') == -1) {
			return uri;
		}
		return uri.substring(0, uri.indexOf('/'));
	}

	@Override
	public void run() {
		try {
			handleUserInput(requestInput());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String scanHTMLLine(String line) {
		if (line.contains("<img")) {
			System.out.println("Client: image detected");
			System.out.println(getPath(line.split("\"")[1]));
			this.foundImageLocations.add(getPath(line.split("\"")[1]));
			String newLoc = getPath(line.split("\"")[1]).replaceAll("/","_");
			System.out.println(newLoc);
			System.out.println("<img src=\"" + newLoc + line.substring(line.indexOf('"',line.indexOf('"') + 1)));
			return "<img src=\"" + newLoc + line.substring(line.indexOf('"',line.indexOf('"') + 1));
		}
		return line;
	}
}
