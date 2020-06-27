package mc.dragons.core.storage;

import java.util.Set;
import java.util.UUID;

import org.bson.Document;

import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;

/**
 * Controls access to persistent storage. This is the only place that should
 * instantiate {@link mc.dragons.core.storage.StorageAccess} objects.
 * 
 * @author Rick
 *
 */
public interface StorageManager {
	public StorageAccess getStorageAccess(GameObjectType objectType, UUID objectUUID);
	public StorageAccess getStorageAccess(GameObjectType objectType, Document search);
	
	public Set<StorageAccess> getAllStorageAccess(GameObjectType objectType);
	
	public StorageAccess getNewStorageAccess(GameObjectType objectType);
	public StorageAccess getNewStorageAccess(GameObjectType objectType, UUID objectUUID);
	public StorageAccess getNewStorageAccess(GameObjectType objectType, Document initialData);
	
	public void storeObject(GameObject gameObject);
	public void removeObject(GameObject gameObject);
	
	public void push(GameObjectType objectType, Document selector, Document update);
}
