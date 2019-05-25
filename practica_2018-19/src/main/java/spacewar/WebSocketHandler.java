package spacewar;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import spacewar.Room.GameMode;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler{

	private ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
	
	private static final String PLAYER_ATTRIBUTE = "PLAYER";
	private ObjectMapper mapper = new ObjectMapper();
	private AtomicInteger playerId = new AtomicInteger(0);
	private AtomicInteger projectileId = new AtomicInteger(0);	
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Player player = new Player(playerId.incrementAndGet(), session);
		session.getAttributes().put(PLAYER_ATTRIBUTE, player);
		
		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "JOIN");
		msg.put("id", player.getPlayerId());
		msg.put("shipType", player.getShipType());
		player.getSession().sendMessage(new TextMessage(msg.toString()));
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		try {
			JsonNode node = mapper.readTree(message.getPayload());
			ObjectNode msg = mapper.createObjectNode();
			Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);

			switch (node.get("event").asText()) {
			case "JOIN":					
				msg.put("event", "JOIN");
				msg.put("id", player.getPlayerId());
				msg.put("shipType", player.getShipType());	
				
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
			case "NEW ROOM":
				System.out.println("Nueva sala: "+node.get("name").asText());
				
				if(rooms.containsKey(node.get("name").asText())) {
					System.out.println(("Error"));
				}
				
				//Crea la sala y añade al jugador
				CreateRoom(node.get("name").asText(), node.get("mode").asInt());
				joinRoom(player, node.get("name").asText());
				
				//Asigna la sala al jugador
				player.setRoom(GetRoom(node.get("name").asText()));
				
				System.out.println(rooms.keySet());
				
				sendRooms(player);
				break;
			case "LOGIN":
				msg.put("event", "LOGIN");
				player.setName(node.get("username").asText());
				msg.put("username", player.getName());		
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
			case "JOIN ROOM":
				joinRoom(player, node.get("roomName").asText());
				msg.put("event", "JOIN ROOM");
				msg.put("room", node.get("roomName").asText());
				
				player.setRoom(rooms.get(node.get("roomName").asText()));
				player.getSession().sendMessage(new TextMessage(msg.toString()));				
				break;
			case "JOIN RANDOM ROOM":
				msg.put("event", "JOIN RANDOM ROOM");
				for(String s : rooms.keySet()) {
					Room room = GetRoom(s);
					if(!room.players.containsKey(player.getName())){
						System.out.println(s);
						if(room.mode == GameMode.PVP && room.players.size() < 2) {
							msg.put("room", s);
							joinRoom(player, s);
							player.setRoom(room);
							break;
						}else if(room.mode == GameMode.BATTLEROYALE && room.players.size() < 3) {
							msg.put("room", s);
							joinRoom(player, s);
							player.setRoom(room);
							break;
						}	
					}
				}
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;			
			case "GET ROOMS":	
				sendRooms(player);				
				break;
			case "MATCHMAKING":				
				Room r = player.getRoom();
				r.tryToStart();				
				break;
			case "UPDATE MOVEMENT":
				player.loadMovement(node.path("movement").get("thrust").asBoolean(),
						node.path("movement").get("brake").asBoolean(),
						node.path("movement").get("rotLeft").asBoolean(),
						node.path("movement").get("rotRight").asBoolean());
				if (node.get("bullet").asBoolean()) {
					if(player.getAmmo() > 0 && player.getAmmo() <= player.getMaxAmmo()) {
						Projectile projectile = new Projectile(player, this.projectileId.incrementAndGet());
						//player.getRoom().getGame().addProjectile(projectile.getId(), projectile);					
						player.getRoom().addProjectile(projectile.getId(), projectile);
						player.setAmmo(player.getAmmo()-1);
					}else {
						player.setAmmo(player.getMaxAmmo());
					}
				}
				break;
			case "CHAT":		
				for(Player participant : player.getRoom().getPlayers()) {
					if(!participant.getSession().getId().equals(session.getId())) {
						participant.getSession().sendMessage(message);
					}
				}
				break;
			case "GET PUNCTUATION":
				sendPunctuations(player);
				break;
			default:
				break;
			}

		} catch (Exception e) {
			System.err.println("Exception processing message " + message.getPayload());
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);
		//player.getRoom().getGame().removePlayer(player);
		player.getRoom().removePlayer(player);
		
		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "REMOVE PLAYER");
		msg.put("id", player.getPlayerId());
		//player.getRoom().getGame().broadcast(msg.toString());;
		player.getRoom().broadcast(msg.toString());
	}

	
	public Room GetRoom(String roomName) {
		return rooms.get(roomName);
	}
	
	public Room CreateRoom(String roomName, int mode) {
		if(rooms.contains(roomName)) return null;
		
		Room room = new Room(roomName, mode);
		rooms.put(roomName, room);
		return room;
	}
	
	public void sendRooms(Player player) throws IOException {
		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "GET ROOMS");					
		ArrayNode avaibleRooms = msg.putArray("avaibleRooms");
		Collection<Room> allRooms = rooms.values();
		for(Room room : allRooms) {
			if(/*Mirar que la room tiene salas libres*/true) {
				ObjectNode roomNode = mapper.createObjectNode();
				roomNode.put("key", room.NAME);
				roomNode.put("gameMode", room.getModeName());
				roomNode.put("numPlayers", room.getNumPlayer());		
				
				avaibleRooms.add(roomNode);
			}					
		}						

		player.getSession().sendMessage(new TextMessage(msg.toString()));
	}
	
	public synchronized void sendPunctuations(Player player) throws IOException {
		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "GET PUNCTUATION");					
		ArrayNode punctuations = msg.putArray("punctuations");		
		for(Player p : player.getRoom().getPlayers()) {			
			ObjectNode playerNode = mapper.createObjectNode();
			playerNode.put("player", p.getName());
			playerNode.put("punctuation", p.getPunctuation());				
			
			punctuations.add(playerNode);							
		}							
		System.out.println(msg.toString());
		player.getSession().sendMessage(new TextMessage(msg.toString()));
	}
	
	public void joinRoom(Player player, String roomName) {
		rooms.get(roomName).joinPlayer(player);
	}
}
