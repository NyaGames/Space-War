package spacewar;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import spacewar.Room.GameMode;

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
				player.setName(node.get("username").asText());
				msg.put("username", player.getName());
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
			case "NEW ROOM":
				System.out.println("Nueva sala: "+node.get("name").asText());
				if(rooms.containsKey(node.get("name").asText())) {
					System.out.println(("Error"));
				}
				CreateRoom(node.get("name").asText(), node.get("mode").asInt());
				joinRoom(player, node.get("name").asText());
				System.out.println(rooms.keySet());
				break;
			case "JOIN ROOM":
				joinRoom(player, node.get("name").asText());
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
							break;
						}else if(room.mode == GameMode.BATTLEROYALE && room.players.size() < 3) {
							msg.put("room", s);
							joinRoom(player, s);
							break;
						}	
					}
				}
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
			case "GET ROOMS":
				msg.put("event", "GET ROOMS");
				for(String s : rooms.keySet()) {
					msg.put("key", s);
				}
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
			case "MATCHMAKING":
				int count = -1;
				int necesidades = -1;
				for(String s : rooms.keySet()) {
					Room room = GetRoom(s);
					if(room.players.containsKey(player.getName())){
						count = room.players.size();
						if(room.mode == GameMode.PVP) necesidades = 2;
						else if(room.mode == GameMode.PVP) necesidades = 3;
						break;
					}
				}
				msg.put("event", "MATCHMAKING");
				msg.put("count", count);
				msg.put("necesidades", necesidades);
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
			case "UPDATE MOVEMENT":
				player.loadMovement(node.path("movement").get("thrust").asBoolean(),
						node.path("movement").get("brake").asBoolean(),
						node.path("movement").get("rotLeft").asBoolean(),
						node.path("movement").get("rotRight").asBoolean());
				if (node.path("bullet").asBoolean()) {
					Projectile projectile = new Projectile(player, this.projectileId.incrementAndGet());
					player.getRoom().getGame().addProjectile(projectile.getId(), projectile);
				}
				break;
			case "CHAT":			
				for(Player participant : player.getRoom().getGame().getPlayers()) {
					if(!participant.getSession().getId().equals(session.getId())) {
						participant.getSession().sendMessage(message);
					}
				}		
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
		player.getRoom().getGame().removePlayer(player);

		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "REMOVE PLAYER");
		msg.put("id", player.getPlayerId());
		player.getRoom().getGame().broadcast(msg.toString());
	}

	
	public Room GetRoom(String roomName) {
		return rooms.get(roomName);
	}
	
	public boolean CreateRoom(String roomName, int mode) {
		if(rooms.contains(roomName)) return false;
		
		Room room = new Room(roomName, mode);
		rooms.put(roomName, room);
		return true;
	}
	
	public void joinRoom(Player player, String roomName) {
		rooms.get(roomName).joinPlayer(player);
		System.out.println(rooms.get(roomName).players.keySet());
	}
}
