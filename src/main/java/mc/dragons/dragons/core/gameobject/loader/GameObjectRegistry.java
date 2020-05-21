package mc.dragons.dragons.core.gameobject.loader;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Responsible for maintaining a master record of all registered
 * game objects. Provides top-level loading and registration features
 * and is subclassed by {@link mc.dragons.dragons.core.gameobject.GameObjectType}
 * -specific loaders, which are called by this class as appropriate.
 * 
 * @author Rick
 *
 */
public class GameObjectRegistry {
	
	protected Dragons plugin;
	protected StorageManager storageManager;
	
	protected Set<GameObject> registeredObjects;
	
	public GameObjectRegistry(Dragons instance, StorageManager storageManager) {
		this.plugin = instance;
		this.storageManager = storageManager;
		this.registeredObjects = new HashSet<>();
	}
	
	public GameObject loadObject(StorageAccess storageAccess) {
		String type = (String) storageAccess.get("type");
		GameObjectType objType = GameObjectType.get(type);
		GameObject gameObject = objType.getLoader().loadObject(storageAccess);
		registeredObjects.add(gameObject);
		return gameObject;
	}
	
	/**
	 * Subclasses should override this to register a
	 * new instance of their object. This superclass
	 * method will not register a new object.
	 * 
	 * Some subclasses may require additional
	 * parameters for instantiation, in which case
	 * this method will not be overriden.
	 * 
	 * @return null
	 */
	public GameObject registerNew() {
		return null;
	}
	
	public Set<GameObject> getRegisteredObjects() {
		return registeredObjects;
	}
	
	public Set<GameObject> getRegisteredObjects(GameObjectType... types) {
		return registeredObjects.stream()
				.filter(obj -> {
					for(GameObjectType type : types) {
						if(type == obj.getType()) {
							return true;
						}
					}
					return false;
				})
				.collect(Collectors.toSet());
	}

	public void removeFromDatabase(GameObject gameObject) {
		storageManager.removeObject(gameObject);
		
	}
}
