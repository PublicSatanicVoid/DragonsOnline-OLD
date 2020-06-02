package mc.dragons.dragons.core.gameobject.loader;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public abstract class GameObjectLoader<T extends GameObject> {
	protected Dragons plugin;
	protected StorageManager storageManager;
	
	protected GameObjectLoader(Dragons instance, StorageManager storageManager) {
		this.plugin = instance;
		this.storageManager = storageManager;
	}
	
	public abstract T loadObject(StorageAccess storageAccess);
}
