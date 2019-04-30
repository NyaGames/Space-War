package spacewar;

import static org.junit.Assert.assertTrue;
import java.net.*; 
import java.io.*; 
import java.util.*; 
import java.net.InetAddress; 

import java.util.concurrent.atomic.AtomicReference;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SpacewarGameTest {
	
	private SpacewarGame game = SpacewarGame.INSTANCE;
	private ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	public static void startServer() {
		Application.main(new String[] { "--server.port=9000" });
	}

	@Test
	public void testConnection() throws Exception {

		WebSocketClient ws = new WebSocketClient();
		InetAddress localhost = InetAddress.getLocalHost(); 
		ws.connect("ws://" + localhost.getHostAddress().trim() + ":9000/spacewar");
		ws.disconnect();
	}

	@Test
	public void testJoin() throws Exception {

		AtomicReference<String> firstMsg = new AtomicReference<String>();

		WebSocketClient ws = new WebSocketClient();
		InetAddress localhost = InetAddress.getLocalHost(); 
		
		ws.onMessage((session, msg) -> {
			System.out.println("TestMessage: " + msg);
			firstMsg.compareAndSet(null, msg);
		});
					
		ws.connect("ws://"+ localhost.getHostAddress().trim() + ":9000/spacewar");
		System.out.println("Connected");
		Thread.sleep(1000);
		String msg = firstMsg.get();

		assertTrue("The fist message should contain 'join', but it is " + msg, msg.contains("JOIN"));
		ws.disconnect();
	}
	
	@Test
	public void testChat() throws Exception {
		/*
		AtomicReference<String> firstMsg = new AtomicReference<String>();

		WebSocketClient ws = new WebSocketClient();
		InetAddress localhost = InetAddress.getLocalHost(); 
		ObjectNode msgg = mapper.createObjectNode();
		msgg.put("event", "CHAT");
		msgg.put("name", "Test");
		msgg.put("message", "Prueba");
		for(Player participant : game.getPlayers()) {
			participant.getSession().sendMessage(new TextMessage(msgg.toString()));
		}		
		ws.onMessage((session, msg) -> {
			System.out.println("CHAT: " + msgg);
			firstMsg.compareAndSet(null, msg);
		});
		
		ws.connect("ws://"+ localhost.getHostAddress().trim() + ":9000/spacewar");
		System.out.println("Connected");
		Thread.sleep(1000);

		assertTrue("Er gato se ha matato", msgg.get("event").equals("CHAT"));
		ws.disconnect();
		*/
	}
}
