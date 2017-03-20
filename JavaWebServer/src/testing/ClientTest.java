package testing;

import httpClient.Client;
import httpServer.Server;
import httpServer.Handler;
import static org.junit.Assert.*;

import org.junit.Test;


public class ClientTest {

	@Test
	public void test() {
		Server myServer = new Server(1024); 
		//Client myClient = new Client();
		//myClient.scanHTMLLine("<img src=\"/images/M_images/livemarks.png\" alt=\"feed-image\"  /> <span>RSS Feeds van onze website ontvangen</span></a>");
		Thread s = new Thread(myServer);
		//Thread c = new Thread(myClient);
		s.start();
		while (true) {
		}
		//try {
			//myClient.handleUserInput("HTTPClient GET localhost 80");
		//} catch (Exception e) {
			//e.printStackTrace();
		//}
		//assert(myClient.getPath("http://www.tribalwars.com/index.html").equals("/index.html"));
		//assert(myClient.getPath("https://www.tribalwars.com/index.html/test").equals("/index.html/test"));
		//try {
		//	assert(myClient.getHost("http://www.tribalwars.nl/index.html/test").equals("www.tribalwars.nl"));
		//} catch (Exception e1) {

		//}
		//try {
			//myClient.handleUserInput("HTTPClient GET localhost 80");
		//	myClient.handleUserInput("HTTPClient GET http://www.kttcnijlen-bevel.be/okjlhhbn 80");
		//} catch (Exception e) {}
	}
}
