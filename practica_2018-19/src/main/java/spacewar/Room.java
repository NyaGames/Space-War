package spacewar;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Room extends TextWebSocketHandler{
	
	public enum GameMode{
		PVP, BATTLEROYALE
	}

	private String name;
	private SpacewarGame game;
	private GameMode mode;
	private boolean gameStarted = false;	

	
	public Room(String name, int mode) {
		this.name = name;
		this.mode = GameMode.values()[mode];
		game = SpacewarGame.INSTANCE;		
	}
	
	public SpacewarGame getGame() {
		return game;
	}
	
	
	public void joinPlayer(Player player) {
		if(gameStarted) return;
		
		game.addPlayer(player);
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
		}
	}
	
	public void startGame() {
		game.startGameLoop();
		game.broadcast("START GAME");
		gameStarted = true;
	}
	
}
