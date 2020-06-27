package mc.dragons.core.tasks;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;

/**
 * Repeating task that saves frequently-updated
 * {@link mc.dragons.core.gameobject.GameObject}s
 * to the database.
 * 
 * @author Rick
 *
 */
public class AutoSaveTask {
	
	private static AutoSaveTask INSTANCE;

	private Dragons plugin;
	private GameObjectRegistry registry;
	
	private AutoSaveTask(Dragons instance) {
		this.plugin = instance;
		this.registry = instance.getGameObjectRegistry();
	}
	
	public synchronized static AutoSaveTask getInstance(Dragons pluginInstance) {
		if(INSTANCE == null) {
			INSTANCE = new AutoSaveTask(pluginInstance);
		}
		return INSTANCE;
	}
	
	public void run(boolean forceSave) {
		if(!plugin.getServerOptions().isAutoSaveEnabled() && !forceSave) return;
		plugin.getLogger().info("Auto-Saving...");
		for(GameObject gameObject : registry.getRegisteredObjects(GameObjectType.USER, GameObjectType.NPC)) {
			gameObject.autoSave();
		}
	}
}
