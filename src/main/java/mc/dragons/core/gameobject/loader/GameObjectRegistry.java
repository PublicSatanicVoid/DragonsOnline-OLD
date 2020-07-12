package mc.dragons.core.gameobject.loader;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.storage.StorageManager;

/**
 * Responsible for maintaining a master record of all registered
 * game objects. Provides top-level loading and registration features
 * and is subclassed by {@link mc.dragons.core.gameobject.GameObjectType}
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
	
	/**
	 * Subclasses should override this to register a
	 * new instance of their object. This superclass
	 * method will not register a new object.
	 * 
	 * Some subclasses may require additional
	 * parameters for instantiation, in which case
	 * this method will not be overridden.
	 * 
	 * @return null
	 */
	public GameObject registerNew() {
		return null;
	}
	
	public Set<GameObject> getRegisteredObjects() {
		return registeredObjects;
	}

	public Set<GameObject> getRegisteredObjects(final GameObjectType... types) {
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
		registeredObjects.remove(gameObject);
	}
	
	public void removeFromRegistry(GameObjectType type) {
		registeredObjects.removeIf(obj -> obj.getType() == type);
	}
}
