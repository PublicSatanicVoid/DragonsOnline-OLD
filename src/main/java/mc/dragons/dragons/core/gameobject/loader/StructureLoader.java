package mc.dragons.dragons.core.gameobject.loader;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.structure.Structure;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class StructureLoader extends GameObjectRegistry {

	private static StructureLoader INSTANCE;
	
	private StructureLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
	}
	
	public synchronized static StructureLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new StructureLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public Structure loadObject(StorageAccess storageAccess) {
		return new Structure(storageManager);
	}
	
	
}
