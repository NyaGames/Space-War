package spacewar;

import java.util.Random;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player extends Spaceship {

	private final WebSocketSession session;
	private Room room;
	private final int playerId;
	private final String shipType;
	
	private final int initialHp = 100;
	private final int maxAmmo = 8;
	private final float maxBoost = 10;
	
	private String name;
	private int hp = initialHp;
	private int ammo = maxAmmo;
	private float boost = maxBoost;
	private int punctuation = 0;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getHp() {
		return hp;
	}

	public void hit(int hitDamage) {
		hp -= hitDamage;
	}

	public int getAmmo() {
		return ammo;
	}

	public void setAmmo(int ammo) {
		this.ammo = ammo;
	}

	public float getBoost() {
		return boost;
	}

	public void setBoost(float boost) {
		this.boost = boost;
	}

	public int getInitialHp() {
		return initialHp;
	}

	public int getMaxAmmo() {
		return maxAmmo;
	}

	public float getMaxBoost() {
		return maxBoost;
	}

	public Player(int playerId, WebSocketSession session) {
		this.playerId = playerId;
		this.session = session;
		this.shipType = this.getRandomShipType();
	}

	public int getPlayerId() {
		return this.playerId;
	}

	public WebSocketSession getSession() {
		return this.session;
	}

	public void sendMessage(String msg) throws Exception {
		this.session.sendMessage(new TextMessage(msg));
	}

	public String getShipType() {
		return shipType;
	}

	private String getRandomShipType() {
		String[] randomShips = { "blue", "darkgrey", "green", "metalic", "orange", "purple", "red" };
		String ship = (randomShips[new Random().nextInt(randomShips.length)]);
		ship += "_0" + (new Random().nextInt(5) + 1) + ".png";
		return ship;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public int getPunctuation() {
		return punctuation;
	}

	public void addPunctuation(int punctuation) {
		this.punctuation += punctuation;
	}
}
