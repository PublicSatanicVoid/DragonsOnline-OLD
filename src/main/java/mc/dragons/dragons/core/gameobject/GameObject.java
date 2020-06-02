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
	protected Document localData;
	
	/**
	 * Initialize a new game object, i.e. one which does not
	 * yet exist in the database.
	 * 
	 * @param type
	 * @param storageManager
	 */
	protected GameObject(GameObjectType type, StorageManager storageManager) {
		this(storageManager, storageManager.getNewStorageAccess(type));
	}
	
	/**
	 * Initialize an existing game object from type, UUID, 
	 * and storage manager.
	 * @param type
	 * @param uuid
	 * @param storageManager
	 */
	protected GameObject(GameObjectType type, UUID uuid, StorageManager storageManager) {
		this.storageManager = storageManager;
		this.storageAccess = storageManager.getStorageAccess(type, uuid);
		this.localData = new Document();
	}
	
	/**
	 * Initialize an existing game object from storage manager
	 * and storage access.
	 * 
	 * @param storageManager
	 * @param storageAccess
	 */
	protected GameObject(StorageManager storageManager, StorageAccess storageAccess) {
		this.storageManager = storageManager;
		this.storageAccess = storageAccess;
		this.localData = new Document();
	}
	
	/**
	 * Set the game object's persistent data. This data is backed
	 * by the persistent data store.
	 * 
	 * @param key
	 * @param value
	 */
	protected void setData(String key, Object value) {
		storageAccess.set(key, value);
	}
	
	/**
	 * Update the game object's persistent data. This data is
	 * backed by the persistent data store.
	 * @param document
	 */
	protected void update(Document document) {
		storageAccess.update(document);
	}
	
	/**
	 * Retrieve persistent data from the object.
	 * 
	 * @param key
	 * @return
	 */
	protected Object getData(String key) {
		return storageAccess.get(key);
	}
	
	/**
	 * Returns the type of the game object.
	 * 
	 * @return
	 */
	public final GameObjectType getType() {
		return storageAccess.getIdentifier().getType();
	}
	
	/**
	 * Returns the UUID of the game object.
	 * 
	 * @apiNote In game objects representing players,
	 * this UUID is equal to the UUID of the player.
	 * 
	 * @return
	 */
	public final UUID getUUID() {
		return storageAccess.getIdentifier().getUUID();
	}
	
	/**
	 * Returns the {@link mc.dragons.dragons.core.storage.StorageAccess}
	 * of this game object.
	 * 
	 * @return
	 */
	public StorageAccess getStorageAccess() {
		return storageAccess;
	}
	
	/**
	 * Returns the game object's data. This should ONLY
	 * be used for reading data, as the returned Document
	 * is NOT directly backed by the persistent data store.
	 * 
	 * @return
	 */
	public Document getData() {
		return storageAccess.getDocument();
	}
	
	/**
	 * Returns the identifier for this game object, composed
	 * of its type and UUID.
	 * 
	 * @return
	 */
	public Identifier getIdentifier() {
		return storageAccess.getIdentifier();
	}
	
	/**
	 * Performs any auto-save functionality on the game object.
	 * Subclasses should override this to implement any periodic
	 * saving, such as time lived.
	 */
	public void autoSave() {
		
	}
	
	/**
	 * Returns the game object's local data. This data is
	 * NOT backed by a persistent data store, and may be
	 * safely modified through this method.
	 * 
	 * @return
	 */
	public Document getLocalData() {
		return localData;
	}
	
	/**
	 * Compares game object identifiers to determine whether this
	 * object and the specified object are equal.
	 * 
	 * @param gameObject
	 * @return
	 */
	public boolean equals(GameObject gameObject) {
		if(gameObject == null) return false;
		return this.getType() == gameObject.getType() && this.getUUID().equals(gameObject.getUUID());
	}
}
