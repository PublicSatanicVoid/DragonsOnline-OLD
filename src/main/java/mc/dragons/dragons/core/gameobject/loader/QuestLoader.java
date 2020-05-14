package mc.dragons.dragons.core.gameobject.loader;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.quest.Quest;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class QuestLoader extends GameObjectRegistry {

	private static QuestLoader INSTANCE;
	
	private QuestLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
	}
	
	public synchronized static QuestLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new QuestLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public Quest loadObject(StorageAccess storageAccess) {
		return new Quest(storageManager);
	}
	
}
