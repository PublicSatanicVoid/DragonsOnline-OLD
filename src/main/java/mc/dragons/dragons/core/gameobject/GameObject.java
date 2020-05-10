package mc.dragons.dragons.core.gameobject;

import java.util.UUID;

/**
 * Represents an object in the game. This could be an NPC,
 * an item, a structure, etc.
 * 
 * @author Rick
 *
 */
public abstract class GameObject {
	private GameObjectType type;
	private UUID uuid;
	private String name;
	
	public final GameObjectType getType() {
		return type;
	}
	
	public final UUID getUUID() {
		return uuid;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return name;
	}
	
}
