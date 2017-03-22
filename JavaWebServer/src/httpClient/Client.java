package httpClient;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Client implements Runnable{
	private static String[] commands = new String[] {"HEAD", "GET", "PUT", "POST"};
	
	private BufferedReader inFromUser;
	private HTMLWriter writer;

	private String contentType;
	private boolean chunked;

	private String host;
	private String path;
	private int port;
	
	private LinkedHashSet<String> foundImages = new LinkedHashSet<String>();
	private ArrayList<String> foundImageLocations = new ArrayList<String>();

	private int contentLength;

	private Socket socket;

	private byte[] file;

	private byte[] headers;

	private String userFile;

	private String userHeaders;
	
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
	
	private LinkedHashSet<String> getImageLocs() {
		return this.foundImages;
	}

	/**
	 * Check if the given input is correct and 
	 * execute the command if it is.
	 * If not correct, new input will be asked for.
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
		if (input[1].equals("PUT") || input[1].equals("POST")) {
			String inputString = "";
			System.out.println("Client: Please enter headers.");
			while (!inputString.endsWith("\r\n\r\n")) {
				inputString += new String(new BufferedReader(new InputStreamReader(System.in)).readLine()) + "\r\n";
			}
			setUserHeaders(inputString);
			inputString = "";
			System.out.println("Client: Please enter body to " + input[1].toLowerCase());
			while (!inputString.endsWith("\r\n\r\n")) {
				inputString += new String(new BufferedReader(new InputStreamReader(System.in)).readLine()) + "\r\n";
			}
			setUserFile(inputString);
		}
		try {
			setPort(Integer.parseInt(input[3]));
			setSocket(new Socket(getHost(), getPort()));
			executeCommand(getSocket(), input);
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
	public void handleUserInput(Socket sock, String s) throws Exception {
		String[] input = parseUserInput(s);
		while(!isCorrectInput(input)) {
			input = parseUserInput(requestInput());
		}
		setHost(input[2]);
		setPath(input[2]);

		try {
			executeCommand(getSocket(), input);
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
	
	private Socket getSocket() {
		// TODO Auto-generated method stub
		return this.socket;
	}

	private void setSocket(Socket socket) {
		// TODO Auto-generated method stub
		this.socket = socket;
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
		//BufferedInputStream bis = new BufferedInputStream(is);
		//InputStreamReader inFromServer = new InputStreamReader(is, "UTF-8");

		outToServer.writeBytes(input[1] + " " + getPath() + " HTTP/1.1\r\n");
		outToServer.writeBytes("Host: " + getHost() + "\r\n\r\n");
		readFromServer(is);
		BufferedReader br = getBR(getHeaders());
		String newLoc = checkStatusCode(br);
		checkHeaders(br);

			switch (input[1]) {
			case "GET":
				if (newLoc != null) {
					System.out.println("Client: Redirecting...");
					redirectRequest(newLoc, input);
				} else {
				byte[] completeResponse = readFullResponse(is, getFile());
				String type = getContentType();
				if (type.equals("text/html")) handleGetHTMLCommand(completeResponse);
				else if (type.startsWith("image")) handleGetImageCommand(completeResponse);
				}
				break;
			case "HEAD": 
				break;
			case "PUT": 
				outToServer.writeBytes(getUserHeaders());
				outToServer.writeBytes(getUserFile());
				break;
			case "POST":
				outToServer.writeBytes(getUserHeaders());
				outToServer.writeBytes(getUserFile());
				break;
			}
		//clientSocket.close();
	}
	
	private String getUserFile() {
		// TODO Auto-generated method stub
		return this.userFile;
	}

	private String getUserHeaders() {
		// TODO Auto-generated method stub
		return this.userHeaders;
	}

	private void readFromServer(InputStream is) throws IOException {
		byte[] bytes = new byte[1024];
		byte[] headers = new byte[1];
		byte[] file = new byte[1];
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = is.read(bytes);

		boolean end = false;
		int i = 0;
		while (!end) {
			if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i+3] == 10) {
				//System.out.println("index " + i);
				headers = Arrays.copyOfRange(bytes, 0, i + 4);
				file = Arrays.copyOfRange(bytes, i + 4, count);
				end = true;
			}
			i++;
		}
		setHeaders(headers);
		setFile(file);
	}
	
	private void setFile(byte[] file) {
		// TODO Auto-generated method stub
		this.file = file;
	}
	
	private byte[] getFile() {
		return this.file;
	}

	private void setHeaders(byte[] headers) {
		// TODO Auto-generated method stub
		this.headers = headers;
	}
	
	private byte[] getHeaders() {
		return this.headers;
	}
	
	private BufferedReader getBR(byte[] headers) {
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headers)));
	}

	private byte[] readFullResponse(InputStream is, byte[] file) throws IOException {
		// TODO Auto-generated method stub
		byte[] response = new byte[getContentLength()];
		response = Arrays.copyOf(file, getContentLength());
		int count = file.length;
		while (count != getContentLength()) {
			count += is.read(response, count, getContentLength() - count);
		}
		return response;
	}

	private void setContentLength(int parseInt) {
		this.contentLength = parseInt;
	}
	
	private int getContentLength() {
		if (this.contentLength <= 0) {
			return file.length;
		}
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
	
	private void handleGetHTMLCommand(byte[] bytes) throws Exception {
		String response = "";
		setWriter(getHost() + getPath());
		getWriter().setWriting(true);
		getWriter().write(new String(bytes, "UTF-8"));
		getWriter().close();
		System.out.println("Server: " + new String(bytes, "UTF-8"));
		BufferedReader br = getBR(bytes);
		
		while(!(response = br.readLine()).endsWith("</HTML>") && !response.endsWith("</html>")) {
			scanHTMLLine(response);
		}
		System.out.println("Client: HTML File scanned for images.");

		ArrayList<String> locations = new ArrayList<String>(getImageLocs());
		for (String location: locations) {
			getImageLocs().remove(location);
			System.out.println("fetching image: " + "HTTPClient GET " +  getHost() + location + " " + getPort());
			handleUserInput(getSocket(), "HTTPClient GET " + getHost() + location + " " + getPort());
		}
	}
	
	private void handleGetImageCommand(byte[] bytes) throws Exception {
		File file = new File("src/savedHTMLFiles/" + getHost() + 
				getPath().substring(0, getPath().lastIndexOf("/")));
		file.mkdirs();
		OutputStream os = new FileOutputStream("src/savedHTMLFiles/" + getHost() + getPath());	
		os.write(bytes);
		os.close();
		System.out.println("Client: Image fetched.");
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
	private String checkStatusCode(BufferedReader br) throws Exception{
		String response;
		//System.out.println("Checking status code.");
		if ((response = br.readLine()).split(" ")[1].charAt(0) == '3') {
			System.out.println("Server: " + response);
			while (!response.startsWith("Location:")) {
				response = br.readLine();
			}
			System.out.println("Server: " + response);
			return response.split(" ")[1];
		}
		System.out.println("Server: " + response);
		//System.out.println("Done checking status code.");
		return null;
	}

	/**
	 * Used for retrieving information from headers.
	 * @param inFromServer
	 * @throws Exception
	 */
	private void checkHeaders(BufferedReader br) throws Exception {
		String response;
		while (!(response = br.readLine()).equals("")) {
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
		//System.out.println("Headers checked.");
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
		//return inFromUser.rea();
	}
	
	public void readFromUser(String s) throws IOException {
		String[] input = parseUserInput(s);
		/*System.out.println("GIEF INPUT");
		byte[] bytes = new byte[1024];
		byte[] headers = new byte[1];
		byte[] file = new byte[1];
		int count = is.read(bytes);

		boolean end = false;
		int i = 0;
		while (!end) {
			if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i+3] == 10) {
				headers = Arrays.copyOfRange(bytes, 0, i + 4);
				file = Arrays.copyOfRange(bytes, i + 4, count);
				end = true;
			}
			i++;
		}
		setUserHeaders(headers);
		setUserFile(file);*/
	}
		
	
	private void setUserFile(String inputString) {
		// TODO Auto-generated method stub
		this.userFile = inputString;
	}

	private void setUserHeaders(String inputString) {
		// TODO Auto-generated method stub
		this.userHeaders = inputString;
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
	
	public void setHost(String uri) throws Exception{
			this.host = getHost(uri);
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
	
	public void scanHTMLLine(String line) {
		while (line.contains("<img") || line.contains("<IMG")) {
			if (line.contains("src=\"")) {
			System.out.println("Client: image detected");
			//System.out.println(line);
			System.out.println("Client: Location = " + line.split("src=\"")[1].split("\"")[0]);
			this.foundImages.add("/" + line.split("src=\"")[1].split("\"")[0]);
			if (line.contains("lowsrc=\"")) {
				//System.out.println(line.split("lowsrc=\"")[1].split("\"")[0]);
				System.out.println("Client: image detected");
				System.out.println("Client: Location = " + line.split("lowsrc=\"")[1].split("\"")[0]);
				this.foundImages.add("/" + line.split("lowsrc=\"")[1].split("\"")[0]);
				line = line.substring(line.indexOf("lowsrc=\"") + 5);
			}
			else {line = line.substring(line.indexOf("src=\"") + 5);}
			} else if (line.contains("SRC=\"")) {
				//System.out.println("Client: image detected");
				//System.out.println(line.split("SRC=\"")[1].split("\"")[0]);
				//this.foundImageLocations.add("/" + line.split("SRC=\"")[1].split("\"")[0]);
				line = line.substring(line.indexOf("SRC=\"") + 5);
			}


			//String newLoc = getPath(line.split("\"")[1]);
			//System.out.println(newLoc);
			//System.out.println(line.split(newLoc)[0] + newLoc + line.split(newLoc)[1]);
			//System.out.println("<img src=\"" + newLoc + line.substring(line.indexOf('"',line.indexOf('"') + 1)));
			//return "<img src=\"" + newLoc + line.substring(line.indexOf('"',line.indexOf('"') + 1));
		}
	}
}
