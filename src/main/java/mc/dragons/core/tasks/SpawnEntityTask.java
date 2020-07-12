package mc.dragons.core.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.User;

/**
 * Repeating task that spawns region-based entities
 * around players.
 * 
 * @author Rick
 *
 */
public class SpawnEntityTask extends BukkitRunnable {

	private Logger LOGGER = Dragons.getInstance().getLogger();

	private Dragons plugin;
	private GameObjectRegistry registry;
	private NPCLoader npcLoader;
	private RegionLoader regionLoader;
	
	public SpawnEntityTask(Dragons instance) {
		this.plugin = instance;
		this.registry = instance.getGameObjectRegistry();
		this.npcLoader = (NPCLoader) GameObjectType.NPC.<NPC>getLoader();
		this.regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
	}
	
	@Override
	public void run() {
		if(!plugin.getServerOptions().isCustomSpawningEnabled()) return;
		long start = System.currentTimeMillis();
		for(GameObject gameObject : registry.getRegisteredObjects(GameObjectType.USER)) {
			User user = (User) gameObject;
			if(user.getPlayer() == null) continue;
			if(user.getPlayer().getGameMode() == GameMode.CREATIVE) continue;
			
			World world = user.getPlayer().getWorld();
			
			Set<Region> regions = regionLoader.getRegionsByLocationXZ(user.getPlayer().getLocation());
			Map<String, Double> spawnRates = new HashMap<>();
			for(Region region : regions) {
				for(Entry<String, Double> entry : region.getSpawnRates().entrySet()) {
					if(entry.getValue() > spawnRates.getOrDefault(entry.getKey(), 0.0)) {
						spawnRates.put(entry.getKey(), entry.getValue());
					}
				}
			}
			for(Entry<String, Double> spawnRate : spawnRates.entrySet()) {
				boolean spawn = Math.random() <= spawnRate.getValue() / 100;
				if(spawn) {
					double xOffset = Math.signum(Math.random() - 0.5) * (5 + Math.random() * 10);
					double zOffset = Math.signum(Math.random() - 0.5) * (5 + Math.random() * 10);
					double yOffset = 0;
					Location loc = user.getPlayer().getLocation().add(xOffset, yOffset, zOffset);
					for(int i = 0; i < 50; i++) {
						if(loc.getBlock().getType().isSolid()) {
							loc.add(0, 1, 0);
						}
						else break;
					}
					npcLoader.registerNew(world, loc, spawnRate.getKey());
					
				}
			}
		}
		long end = System.currentTimeMillis();
		LOGGER.fine("Ran entity spawn task in " + (end - start) + "ms");
	}
	
}
