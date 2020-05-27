package mc.dragons.dragons.core.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.NPCLoader;
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
			double xOffset = Math.signum(Math.random() - 0.5) * (5 + Math.random() * 10);
			double zOffset = Math.signum(Math.random() - 0.5) * (5 + Math.random() * 10);
			double yOffset = 2;
			
			double levelSeed = Math.random();
			int level = (int)Math.ceil(plugin.getServerOptions().getSpawnLevelCap() * levelSeed);
			
			World world = user.p().getWorld();
			Location loc = user.p().getLocation().clone().add(xOffset, yOffset, zOffset);
			for(int i = 0; i < 10; i++) {
				if(loc.getBlock().getType().isSolid()) {
					loc.add(0, 1, 0);
				}
				else break;
			}
			
			Entity e = world.spawnEntity(loc, EntityType.ZOMBIE);
			npcLoader.registerNew(e, ChatColor.RED + "Mean Zombie", 10.0 * level, level, true);
		}
	}
	
}
