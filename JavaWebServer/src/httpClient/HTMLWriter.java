package httpClient;

import java.io.*;

public class HTMLWriter {
	
	private String fileName;
	private Boolean writing;
	private PrintWriter pWriter;
	private BufferedWriter writer;
	private Client client;
	
	public HTMLWriter(Client client, String fileName) {
		setClient(client);
		setFileName(fileName);
		setWriting(false);
		setPWriter(getFileName());
		setWriter(getPWriter());
	}
	
	private void setClient(Client client) {
		this.client = client;
	}
	
	private Client getClient() {
		return this.client;
	}

	private void setPWriter(String fileName) {
		//System.out.println(fileName);

		try {
			File file;
			if (fileName.lastIndexOf("/") == -1 || fileName.lastIndexOf("/") == fileName.length() - 1) {
				fileName += "/index.html";
			}
			file = new File("src/savedHTMLFiles/" + fileName.substring(0, fileName.lastIndexOf("/")));
			file.mkdirs();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			this.pWriter = new PrintWriter("src/savedHTMLFiles/" + fileName, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private PrintWriter getPWriter() {
		return this.pWriter;
	}
	
	private void setWriter(PrintWriter fWriter) {
		this.writer = new BufferedWriter(fWriter);
	}
	
	private BufferedWriter getWriter() {
		return this.writer;
	}

	public void setWriting(boolean b) {
		this.writing = b;
	}
	
	public Boolean isWriting() {
		return this.writing;
	}
	
	private void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	private String getFileName() {
		return this.fileName;
	}
	
	public void write(String line) {
		if (isWriting()) {
			try {
				getWriter().write(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	public void close() {
		try {
			getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
