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

/* Se encarga de mandar y recibir los mensajes que se mandan por el chat. Básicamente tiene una referencia a los jugadores y 
 * cuando uno de los jugadores manda un mensaje, este mensaje se manda al resto de jugadores.*/

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

	//Actualiza el mapa de jugadores del chat con el nuevo que se pasa como parámetro		
	public void updateChat(Map<String, Player> players) {
		this.sessions.clear();
		for(Player player : players.values()) {
			this.sessions.put(player.getSession().getId(), player.getSession());
		}
	}
	
	//Se llama cuando se recibe un mensaje. Se pasan como parámetos la sesión que manda el mensaje y el mensaje como tal, y después se pide que se mande al 
	//resto de jugadores.
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
		
		System.out.println("User "+name+" has sent "+ count +" messages");
		
	}

	//Manda un mensaje a todas las sesiones. Se pasa como parametro la sesion a la que no hay que mandatle el mensaje, y el mensaje como tal
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
