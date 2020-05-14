package mc.dragons.dragons.core.gameobject.loader;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class ItemLoader extends GameObjectRegistry {

	private static ItemLoader INSTANCE;
	
	private ItemLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
	}
	
	public synchronized static ItemLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new ItemLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public Item loadObject(StorageAccess storageAccess) {
		return new Item(storageManager);
	}
	
	public void registerObject(Item item) {
		
	}

}
