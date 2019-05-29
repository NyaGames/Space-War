package spacewar;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatHandler extends TextWebSocketHandler {
	
	private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private ConcurrentMap<String, AtomicInteger> numMsgs = new ConcurrentHashMap<>();
	
	private ObjectMapper json = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	
	static class ChatMessage {
		String name;
		String message;
	}
	
	public ChatHandler(Map<String, Player> players) {
		for(Player player : players.values()) {
			this.sessions.put(player.getSession().getId(), player.getSession());
		}
	}

	public void updateChat(Map<String, Player> players) {
		this.sessions.clear();
		for(Player player : players.values()) {
			this.sessions.put(player.getSession().getId(), player.getSession());
		}
	}
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		System.out.println("Message received: " + message.getPayload());
		ChatMessage msg = json.readValue(message.getPayload(), ChatMessage.class);
		
		countMessage(session, msg.name);
		
		sendOtherParticipants(session, msg);
	}

	private void countMessage(WebSocketSession session, String name) {
		
		AtomicInteger numMsgsSession = numMsgs.computeIfAbsent(session.getId(), k -> new AtomicInteger());
		int count = numMsgsSession.incrementAndGet();
		
		System.out.println("User "+name+" has sent "+count+" messages");
		
	}

	private void sendOtherParticipants(WebSocketSession session, ChatMessage msg) throws IOException {

		String jsonMsg = json.writeValueAsString(msg);
		
		System.out.println("Message sent: " + jsonMsg);
				
		for(WebSocketSession participant : sessions.values()) {
			if(!participant.getId().equals(session.getId())) {
				participant.sendMessage(new TextMessage(jsonMsg));
			}
		}
	}

}
