package mc.dragons.core.tasks;

import org.bukkit.scheduler.BukkitRunnable;

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
public class AutoSaveTask extends BukkitRunnable {
	private Dragons plugin;
	private GameObjectRegistry registry;
	
	public AutoSaveTask(Dragons instance) {
		this.plugin = instance;
		this.registry = instance.getGameObjectRegistry();
	}
	
	@Override
	public void run() {
		run(false);
	}
	
	public void run(boolean forceSave) {
		if(!plugin.getServerOptions().isAutoSaveEnabled() && !forceSave) return;
		plugin.getLogger().info("Auto-Saving...");
		for(GameObject gameObject : registry.getRegisteredObjects(GameObjectType.USER, GameObjectType.NPC)) {
			gameObject.autoSave();
		}
	}
}
