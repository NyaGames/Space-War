package spacewar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Room extends TextWebSocketHandler{
	
	public enum GameMode{
		NONE, PVP, BATTLEROYALE
	}

	private String name;
	//private SpacewarGame game;
	public GameMode mode;
	private boolean gameStarted = false;	

	
	public Room(String name, int mode) {
		this.name = name;
		this.mode = GameMode.values()[mode];
		//game = SpacewarGame.INSTANCE;		
	}
	
	/*public SpacewarGame getGame() {
		return game;
	}*/
	
	
	public void joinPlayer(Player player) {
		if(gameStarted) return;
		
		/*game.addPlayer(player);
		switch(mode) {
			case PVP:
				if(game.getNumPlayer().equals(2)) {
					startGame();
				}
				break;
			case BATTLEROYALE:
				if(game.getNumPlayer().equals(3)) {
					startGame();
				}
				break;
		}*/

		players.put(player.getName(), player);
		switch(mode) {
		case PVP:
			if(players.size() == 2) {
				System.out.println("Let's jugar uno pa uno");
				startGame();
			}
			break;
		case BATTLEROYALE:
			if(players.size() == 3) {
				System.out.println("Let's jugar todos para todos");
				startGame();
			}
			break;
	}
	}
	
	public void startGame() {
		//game.startGameLoop();
		//game.broadcast("START GAME");
		this.startGameLoop();
		this.broadcast("START GAME");
		gameStarted = true;
	}
	
	//voy a probar esto aunque pablo me mate a lo mejor
	private final static int FPS = 30;
	private final static long TICK_DELAY = 1000 / FPS;
	public final static boolean DEBUG_MODE = true;
	public final static boolean VERBOSE_MODE = true;

	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// GLOBAL GAME ROOM
	public Map<String, Player> players = new ConcurrentHashMap<>();
	private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>();
	private AtomicInteger numPlayers = new AtomicInteger();
	private ChatHandler chatHandler = new ChatHandler(players);
	public void addPlayer(Player player) {
		players.put(player.getSession().getId(), player);

		int count = numPlayers.getAndIncrement();
		
		chatHandler.updateChat(players);
	}

	public Collection<Player> getPlayers() {
		return players.values();
	}

	public void removePlayer(Player player) {
		players.remove(player.getSession().getId());

		int count = this.numPlayers.decrementAndGet();
		if (count == 0) {
			this.stopGameLoop();
		}
		chatHandler.updateChat(players);
	}
	
	public AtomicInteger getNumPlayer() {
		return numPlayers;
	}

	public void addProjectile(int id, Projectile projectile) {
		projectiles.put(id, projectile);
	}
	

	public Collection<Projectile> getProjectiles() {
		return projectiles.values();
	}

	public void removeProjectile(Projectile projectile) {
		players.remove(projectile.getId(), projectile);
	}

	public void startGameLoop() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopGameLoop() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}

	public void broadcast(String message) {
		for (Player player : getPlayers()) {
			try {
				player.getSession().sendMessage(new TextMessage(message.toString()));
			} catch (Throwable ex) {
				System.err.println("Execption sending message to player " + player.getSession().getId());
				ex.printStackTrace(System.err);
				this.removePlayer(player);
			}
		}
	}

	private void tick() {
		ObjectNode json = mapper.createObjectNode();
		ArrayNode arrayNodePlayers = mapper.createArrayNode();
		ArrayNode arrayNodeProjectiles = mapper.createArrayNode();

		long thisInstant = System.currentTimeMillis();
		Set<Integer> bullets2Remove = new HashSet<>();
		boolean removeBullets = false;

		try {	
			// Update bullets and handle collision
			for (Projectile projectile : getProjectiles()) {
				projectile.applyVelocity2Position();

				// Handle collision
				for (Player player : getPlayers()) {
					if ((projectile.getOwner().getPlayerId() != player.getPlayerId()) && player.intersect(projectile)) {
						// System.out.println("Player " + player.getPlayerId() + " was hit!!!");
						projectile.setHit(true);
						player.hit(projectile.getDamage());
						break;
					}
				}

				ObjectNode jsonProjectile = mapper.createObjectNode();
				jsonProjectile.put("id", projectile.getId());

				if (!projectile.isHit() && projectile.isAlive(thisInstant)) {
					jsonProjectile.put("posX", projectile.getPosX());
					jsonProjectile.put("posY", projectile.getPosY());
					jsonProjectile.put("facingAngle", projectile.getFacingAngle());
					jsonProjectile.put("isAlive", true);
				} else {
					removeBullets = true;
					bullets2Remove.add(projectile.getId());
					jsonProjectile.put("isAlive", false);
					if (projectile.isHit()) {
						jsonProjectile.put("isHit", true);
						jsonProjectile.put("posX", projectile.getPosX());
						jsonProjectile.put("posY", projectile.getPosY());
					}
				}
				arrayNodeProjectiles.addPOJO(jsonProjectile);
			}

			if (removeBullets)
				this.projectiles.keySet().removeAll(bullets2Remove);
			
			// Update players
			for (Player player : getPlayers()) {
				player.calculateMovement();

				ObjectNode jsonPlayer = mapper.createObjectNode();
				jsonPlayer.put("id", player.getPlayerId());
				jsonPlayer.put("name", player.getName());
				jsonPlayer.put("hp", player.getHp());
				jsonPlayer.put("boost", player.getBoost());
				jsonPlayer.put("ammo", player.getAmmo());
				jsonPlayer.put("shipType", player.getShipType());
				jsonPlayer.put("posX", player.getPosX());
				jsonPlayer.put("posY", player.getPosY());
				jsonPlayer.put("facingAngle", player.getFacingAngle());
				arrayNodePlayers.addPOJO(jsonPlayer);
			}

			json.put("event", "GAME STATE UPDATE");
			json.putPOJO("players", arrayNodePlayers);
			json.putPOJO("projectiles", arrayNodeProjectiles);
			System.out.println("Enviando cosas de moverse");
			this.broadcast(json.toString());
		} catch (Throwable ex) {

		}
	}

	public void handleCollision() {

	}
	
}
