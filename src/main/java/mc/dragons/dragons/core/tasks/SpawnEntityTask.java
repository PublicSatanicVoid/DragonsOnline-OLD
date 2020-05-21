package mc.dragons.dragons.core.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.player.User;
import net.md_5.bungee.api.ChatColor;

/**
 * Repeating task that spawns region-based entities
 * around players.
 * 
 * @author Rick
 *
 */
public class SpawnEntityTask {

	private static SpawnEntityTask INSTANCE;

	private Dragons plugin;
	private GameObjectRegistry registry;
	private NPCLoader npcLoader;
	
	private SpawnEntityTask(Dragons instance) {
		this.plugin = instance;
		this.registry = instance.getGameObjectRegistry();
		this.npcLoader = (NPCLoader)GameObjectType.NPC.getLoader();
	}
	
	public synchronized static SpawnEntityTask getInstance(Dragons pluginInstance) {
		if(INSTANCE == null) {
			INSTANCE = new SpawnEntityTask(pluginInstance);
		}
		return INSTANCE;
	}
	
	public void run() {
		if(!plugin.getServerOptions().isCustomSpawningEnabled()) return;
		for(GameObject gameObject : registry.getRegisteredObjects(GameObjectType.PLAYER)) {
			User user = (User)gameObject;
			double xOffset = (Math.random() > 0.5 ? 1 : -1) * (5 + Math.random() * 10);
			double zOffset = (Math.random() > 0.5 ? 1 : -1) * (5 + Math.random() * 10);
			double yOffset = 2;
			
			double levelSeed = Math.random();
			int level = 0;
			if(levelSeed > 0.9) level = 4;
			else if(levelSeed > 0.5) level = 3;
			else if(levelSeed > 0.4) level = 2;
			else level = 1;
			
			World world = user.p().getWorld();
			Location loc = user.p().getLocation().clone().add(xOffset, yOffset, zOffset);
			
			Entity e = world.spawnEntity(loc, EntityType.ZOMBIE);
			npcLoader.registerNew(e, ChatColor.RED + "Mean Zombie", 20.0 * level, level);
		}
	}
	
}
