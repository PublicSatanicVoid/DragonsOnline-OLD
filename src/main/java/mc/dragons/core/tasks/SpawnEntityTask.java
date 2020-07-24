package mc.dragons.core.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
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
	private final double SPAWN_RADIUS = 15;

	private Dragons plugin;
	//private GameObjectRegistry registry;
	private NPCLoader npcLoader;
	private RegionLoader regionLoader;
	
	public SpawnEntityTask(Dragons instance) {
		this.plugin = instance;
		//this.registry = instance.getGameObjectRegistry();
		this.npcLoader = (NPCLoader) GameObjectType.NPC.<NPC>getLoader();
		this.regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
	}
	
	@Override
	public void run() {
		if(!plugin.getServerOptions().isCustomSpawningEnabled()) return;
		long start = System.currentTimeMillis();
		for(User user : UserLoader.allUsers()) {
			if(user.getPlayer() == null) continue;
			if(user.getPlayer().getGameMode() == GameMode.CREATIVE || user.getPlayer().getGameMode() == GameMode.SPECTATOR) continue;
			
			World world = user.getPlayer().getWorld();
			
			int cap = -1;
			
			Location center = user.getPlayer().getLocation();
			Set<Region> regions = regionLoader.getRegionsByLocation(center);
			Map<String, Double> spawnRates = new HashMap<>();
			Vector min = center.toVector();
			Vector max = center.toVector();
			boolean nospawn = false;
			for(Region region : regions) {
				if(Boolean.valueOf(region.getFlags().getString("nospawn"))) {
					nospawn = true;
					break;
				}
				for(Entry<String, Double> entry : region.getSpawnRates().entrySet()) {
					if(entry.getValue() > spawnRates.getOrDefault(entry.getKey(), 0.0)) {
						spawnRates.put(entry.getKey(), entry.getValue());
					}
				}
				int regionCap = Integer.valueOf(region.getFlags().getString("spawncap"));
				if(regionCap < cap && regionCap != -1 || cap == -1) {
					cap = regionCap;
				}
				min = Vector.getMinimum(min, region.getMin().toVector());
				max = Vector.getMaximum(max, region.getMax().toVector());
			}
			
			if(nospawn) continue;
			
			// We only approximate the spawn cap due to performance considerations.
			int entityCount = 0;
			if(cap != -1) {
				double searchRadius = Math.max(min.distance(center.toVector()), max.distance(center.toVector()));
				for(@SuppressWarnings("unused") Entity e : user.getPlayer().getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
					entityCount++;
				}
			}
			
			if(entityCount > cap && cap != -1) continue;
			
			for(Entry<String, Double> spawnRate : spawnRates.entrySet()) {
				boolean spawn = Math.random() <= spawnRate.getValue() / 100;
				if(spawn) {
					double xOffset = Math.signum(Math.random() - 0.5) * (5 + Math.random() * SPAWN_RADIUS);
					double zOffset = Math.signum(Math.random() - 0.5) * (5 + Math.random() * SPAWN_RADIUS);
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
				
				entityCount++;
				if(entityCount > cap && cap != -1) break;
			}
		}
		long end = System.currentTimeMillis();
		LOGGER.fine("Ran entity spawn task in " + (end - start) + "ms");
	}
	
}
