package httpServer;
import java.io.*;
import java.net.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class Handler implements Runnable {
	Socket socket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	
	// The version of HTTP being handled by this handler
	private String version;
	
	// The location of the file to be retrieved
	private String location;
	
	private File file;
	
	// The command given by the request
	private String command;
	
	// The statuscode being returned to the client
	private String statusCode;
	
	// The array of supported commands with which the command will be compared.
	private static String[] commands = new String[] {"HEAD", "GET", "PUT", "POST"};
	
	
	/**
	 * Create a new Handler with the given socket as its socket.
	 * @param 	socket
	 * 			The socket for this Handler
	 */
	public Handler(Socket socket) { 
		this.socket = socket; 
		setIFC();
		setOTC();
	}
	
	/**
	 * Set this Handlers inFromClient to a BufferedReader 
	 * created using this Handlers socket.
	 */
	private void setIFC() {
		try {
			this.inFromClient = new BufferedReader(new
					InputStreamReader (getSocket().getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get this Handlers inFromClient.
	 * @return	A BufferedReader that reads the clients input.
	 */
	private BufferedReader getIFC() {
		return this.inFromClient;
	}
	
	/**
	 * Set this Handlers outToClient to a DataOutputStream
	 * created using this Handlers socket.
	 */
	private void setOTC() {
		try {
			this.outToClient = new DataOutputStream
			(getSocket().getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get this Handlers outToClient
	 * @return	A DataOutputStream that writes output 
	 * 			back to the client.
	 */
	private DataOutputStream getOTC() {
		return this.outToClient;
	}
	
	/**
	 * Return this Handlers socket.
	 * @return Socket
	 */
	private Socket getSocket() {
		return this.socket;
	}
	/**
	 * Run this handler. Whenever a client sends an HTTP request 
	 * to the server, the server will create and run a new Handler 
	 * in a new thread while it keeps listening for other requests itself.
	 * The Handler will then handle the clients request.
	 */
	@Override
	public void run()
	{
		try { 
			handleRequest(receiveClientInput());
			/*String clientSentence = getIFC().readLine();
			System.out.println("Received: " + clientSentence);
			String capsSentence = clientSentence.toUpperCase() + '\n';
			getOTC().writeBytes("HTTP/1.1 200 OK\n");
			//getOTC().writeBytes("Content-Length: 1318\n");
			getOTC().writeBytes("Content-Type: text/html\r\n\r\n");

			getOTC().writeBytes("<!doctype html><html><head><title>Example Domain</title><meta charset=\"utf-8\" /><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" /><style type=\"text/css\">body {background-color: #f0f0f2;margin: 0;padding: 0;font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;}div {width: 600px;margin: 5em auto;padding: 50px;background-color: #fff;border-radius: 1em;}a:link, a:visited {color: #38488f;text-decoration: none;}@media (max-width: 700px) {body {background-color: #fff;}div {width: auto;margin: 0 auto;border-radius: 0;padding: 1em;}}</style></head><body><div><h1>Example Domain</h1><p>This domain is established to be used for illustrative examples in documents. You may use this domain in examples without prior coordination or asking for permission.</p><p><a href=\"http://www.iana.org/domains/example\">More information...</a></p></div></body></html>");
			getSocket().close();*/
		} catch (Exception e){}
	}
	
	/**
	 * Handle an HTTP/1.1 request. The given String request will be split 
	 * according to whitespaces and then each part of the request will be set.
	 * @param 	request
	 * 			The HTTP/1.1 request to be handled.
	 */
	private void handleRequest(String request) {
		// TODO Auto-generated method stub
		try {
		String[] parsedReq = parseRequest(request);
		setCommand(parsedReq[0]);
		//System.out.println("Command parsed: " + parsedReq[0]);
		setVersion(parsedReq[2]);
		//System.out.println("Version parsed: " + parsedReq[2]);
		setLocation(parsedReq[1]);
		setFile(getLocation());
		//System.out.println("Location parsed: " + parsedReq[1]);
		receiveAddedInput();
		respond();
		} catch (Exception e) {}
		
	}

	private void setFile(String location) {
		// TODO Auto-generated method stub
		if (location != null) {
		this.file = new File(location);
		}
	}
	
	private File getFile() {
		 return this.file;
	}

	/**
	 * Formulate and send a responce to the Client 
	 * who issued this Handlers request.
	 */
	private void respond() {
		// TODO Auto-generated method stub
		try {
			getOTC().writeBytes(getHeaders());
			getOTC().writeByte(13);
			getOTC().writeByte(10);
			getOTC().writeByte(13);
			getOTC().writeByte(10);
			FileInputStream fis = new FileInputStream(getFile());
			byte[] b = new byte[(int) getFile().length()];
			fis.read(b);
			getOTC().write(b);
			//getOTC().writeChars("<!doctype html><html><head><title>Example Domain</title><meta charset=\"utf-8\" /><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" /><style type=\"text/css\">body {background-color: #f0f0f2;margin: 0;padding: 0;font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;}div {width: 600px;margin: 5em auto;padding: 50px;background-color: #fff;border-radius: 1em;}a:link, a:visited {color: #38488f;text-decoration: none;}@media (max-width: 700px) {body {background-color: #fff;}div {width: auto;margin: 0 auto;border-radius: 0;padding: 1em;}}</style></head><body><div><h1>Example Domain</h1><p>This domain is established to be used for illustrative examples in documents. You may use this domain in examples without prior coordination or asking for permission.</p><p><a href=\"http://www.iana.org/domains/example\">More information...</a></p></div></body></html>\r\n\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the headers used in the response to 
	 * the request being handled by this Handler.
	 * @return A string containing the headers for 
	 * this Handlers response.
	 */
	private String getHeaders() {
		//TODO: add more headers.
		String headers = getStatusCode() + getDate() + getContentType();
		if (getContentLength() != null) {
			headers += getContentLength();
		}
		return headers;
	}
	
	/**
	 * Return the Content-Length header for this Handlers response.
	 * @return	A String containing the Content-Length header
	 */
	private String getContentLength() {
		if (getFile() != null) {
			return "Content-Length: " + getFile().length();
		} else return null;
	}

	/**
	 * Return the Content-Type header for this Handlers response.
	 * @return	A String containing the Content-Type header
	 */
	private String getContentType() {
		//TODO: jsoup om door file te gaan? / iets bijhouden dat elke file labelt
		// default
		return "Content-Type: text/html\n";
	}

	/**
	 * Return the Date header for this Handlers response.
	 * @return	A String containing the Date header
	 */
	private String getDate() {
		// TODO juiste format.
		final Date currentTime = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return "GMT time: " + sdf.format(currentTime) + "\n";
	}
	
	/**
	 * Set this Handlers responses statusCode.
	 * @param 	string
	 * 			A String containing the status code.
	 */
	private void setStatusCode(String string) {
		this.statusCode = string;
	}
	
	/**
	 * Return this Handlers statusCode.
	 * @return A String containing the statusCode
	 */
	private String getStatusCode() {
		return this.statusCode;
	}

	/**
	 * Check whether the given location loc is a valid 
	 * file location for this server.
	 * @param 	loc
	 * 			A string containing the location to check.
	 * @return 	Returns true if and only if the given location 
	 * 			is valid. False otherwise.
	 * @throws IOException 
	 */
	public boolean isValidLocation(String loc) {
		InputStream is;
		if (loc.equals("/")) {
			return true;
		}
		
		else if (loc.startsWith("/")){
			loc = "src/ServerPages/" + loc.substring(1);
		}
		try {
			if ((is = new FileInputStream(loc)) != null) {
				is.close();
				return true;
			}
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Receive extra input given by the client. 
	 * This is only necessary for PUT and POST requests.
	 */
	private void receiveAddedInput() {
		// TODO Auto-generated method stub
		switch (getCommand()) {
			case "GET":
				break;
			case "HEAD":
				break;
			case "POST":
				//TODO: set string here
				break;
			case "PUT":
				//TODO: set string here
				break;
		}
				
	}

	/**
	 * Set this Handlers HTTP version to the version
	 *  contained in the given string.
	 * @param 	string
	 * 			A string containing the version of 
	 * 			the request passed on to this handler
	 * @throws 	IOException if the given string containing 
	 * 			the version is not equal to HTTP/1.1
	 */
	private void setVersion(String string) throws IOException {
		if (!string.equals("HTTP/1.1")) {
			throw new IOException("Only HTTP/1.1 is supported.");
		} else {
			this.version = string;
		}
	}
	
	/**
	 * Return this handlers HTTP version.
	 * @return	A String containing the version of this Handler.
	 */
	private String getVersion() {
		return this.version;
	}

	/**
	 * Set this Handlers location to the given location.
	 * Also sets the statuscode to OK or Not Found 
	 * according to whether the given location is valid.
	 * @param 	string
	 * 			A String containing the location of 
	 * 			the file to be fetched by this handler.
	 */
	private void setLocation(String string) {
		//TODO: support more codes.
		try {
			if (!getIFC().readLine().startsWith("Host:")){
				setStatusCode(getVersion() + " 400 Bad Request\n");
				this.location = null;
			}
			else if (!isValidLocation(string)) {
				setStatusCode(getVersion() + " 404 Not Found\n");
				this.location = null;
			} else {
				setStatusCode(getVersion() + " 200 OK\n");
				this.location = "src/ServerPages/" + string.split("/")[1];
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	/**
	 * Return this Handlers location.
	 * @return	A String containing the location of 
	 * 			the file to be fetched by this Handler.
	 */
	private String getLocation() {
		return this.location;
	}

	/**
	 * Set this Handlers command to the given string.
	 * @param 	string
	 * 			A String containing the command contained 
	 * 			in the request being handled by this Handler.
	 * @throws 	IOException if the given string is not a supported command.
	 */
	private void setCommand(String string) throws IOException {
		if (isSupportedCommand(string))
			this.command = string;
		else {
			throw new IOException("Invalid command, only GET, HEAD, PUT and POST are supported.");
		}
	}
	
	/**
	 * Check whether the given command is supported by this Handler.
	 * @param 	command
	 * 			A String containing the command to be checked.
	 * @return	True if and only if the given command is equal to 
	 * 			one of the supported commands in the static 
	 * 			String array commands. False otherwise.
	 */
	private boolean isSupportedCommand(String command) {
		for (String s: commands) if (s.equals(command)) return true;
		return false;
	}
	
	/**
	 * Return this Handlers command.
	 * @return	A String containing the command 
	 * 			being handled by this Handler.
	 */
	private String getCommand() {
		return this.command;
	}

	/**
	 * Split the given request String based on whitespaces.
	 * @param 	request
	 * 			The request to be split.
	 * @return	A String array containing the different 
	 * 			parts of the HTTP request.
	 */
	private String[] parseRequest(String request) {
		return request.split(" ");
	}

	/**
	 * Read input from the client using this Handlers 
	 * inFromClient BufferedReader.
	 * @return	A string containing the Clients input.
	 * @throws 	IOException If this Handler could not read 
	 * 			any data from its inFromClient BufferedReader
	 */
	public String receiveClientInput() throws IOException {
		try {
			return getIFC().readLine();
		} catch (IOException e) {
			System.out.print("Handler: Error while trying to get input from client.");
			throw new IOException();
		}
	}
}
