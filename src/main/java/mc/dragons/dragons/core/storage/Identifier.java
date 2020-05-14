package mc.dragons.dragons.core.storage;

import java.util.UUID;

import org.bson.Document;

import mc.dragons.dragons.core.gameobject.GameObjectType;

/**
 * Uniquely identifies a {@link mc.dragons.dragons.core.gameobject.GameObject}
 * by its UUID and {@link mc.dragons.dragons.core.gameobject.GameObjectType}.
 * 
 * @author Rick
 *
 */
public class Identifier {
	private Document identifierData;
	
	public Identifier(GameObjectType type, UUID uuid) {
		identifierData = new Document("type", type.toString()).append("_id", uuid);
	}
	
	public Document getDocument() {
		return identifierData;
	}
	
	public GameObjectType getType() {
		return GameObjectType.get(identifierData.getString("type"));
	}
	
	public UUID getUUID() {
		return (UUID)identifierData.get("_id");
	}
}
