package mc.dragons.dragons.core.gameobject.loader;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class NPCLoader extends GameObjectRegistry {
	
	private static NPCLoader INSTANCE;
	
	private NPCLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
	}
	
	public synchronized static NPCLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new NPCLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public NPC loadObject(StorageAccess storageAccess) {
		return new NPC(storageManager);
	}
	
}
