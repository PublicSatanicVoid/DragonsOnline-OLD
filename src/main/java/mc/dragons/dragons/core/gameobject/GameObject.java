package mc.dragons.dragons.core.gameobject;

import java.util.UUID;

import org.bson.Document;

import mc.dragons.dragons.core.storage.Identifier;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Represents an object in the game. This could be an NPC,
 * an item, a structure, etc.
 * 
 * <p>An {@link org.bson.Document} representation can be fetched
 * via the getData() method, and represents enough of the object's
 * data to fully construct it at any given time. This data may be
 * stored in some form or another of persistent storage, and should
 * only use basic data types like String, double, etc. or sub-Documents.
 * 
 * <p>Subclasses should ensure that all changes to fields are reflected
 * in the data using the protected setData() and getData() methods.
 * 
 * @author Rick
 *
 */
public abstract class GameObject {
	
	protected StorageManager storageManager;
	protected StorageAccess storageAccess;
	
	protected GameObject(GameObjectType type, StorageManager storageManager) {
		this(type, UUID.randomUUID(), storageManager);
	}
	
	protected GameObject(GameObjectType type, UUID uuid, StorageManager storageManager) {
		this.storageManager = storageManager;
		this.storageAccess = storageManager.getStorageAccess(type, uuid);
	}
	
	protected void setData(String key, Object value) {
		storageAccess.set(key, value);
	}
	
	protected void update(Document document) {
		storageAccess.update(document);
	}
	
	protected Object getData(String key) {
		return storageAccess.get(key);
	}
	
	public final GameObjectType getType() {
		return storageAccess.getIdentifier().getType();
	}
	
	public final UUID getUUID() {
		return storageAccess.getIdentifier().getUUID();
	}
	
	public StorageAccess getStorageAccess() {
		return storageAccess;
	}
	
	public Document getData() {
		return storageAccess.getDocument();
	}
	
	public Identifier getIdentifier() {
		return storageAccess.getIdentifier();
	}
	
	public void autoSave() {
		
	}
}
