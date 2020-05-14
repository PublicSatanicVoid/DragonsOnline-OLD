package mc.dragons.dragons.core.storage;

import java.util.Set;
import java.util.UUID;

import org.bson.Document;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;

/**
 * Controls access to persistent storage. This is the only place that should
 * instantiate {@link mc.dragons.dragons.core.storage.StorageAccess} objects.
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
}
