package mc.dragons.dragons.core.gameobject.npc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.bukkit.Location;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.item.ItemClass;
import mc.dragons.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.region.Region;

public class LootTable {
	private Document lootTable;
	
	private static RegionLoader regionLoader;
	private static ItemClassLoader itemClassLoader;
	private static ItemLoader itemLoader;
	
	public LootTable(NPCClass npcClass) {
		this.lootTable = (Document) npcClass.getStorageAccess().get("lootTable");
		
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
			itemClassLoader = (ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader();
			itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
		}
	}
	
	public Set<Item> getDrops(Location loc) {
		if(lootTable == null) return new HashSet<>();
		
		Set<Region> regions = regionLoader.getRegionsByLocation(loc);
		Set<Item> drops = new HashSet<>();
		
		for(Region region : regions) {
			Document regionLoots = (Document) lootTable.get(region.getName());
			if(regionLoots == null) continue;
			for(Entry<String, Object> loot : regionLoots.entrySet()) {
				double chance = (double) loot.getValue();
				if(Math.random() < chance / 100) {
					ItemClass itemClass = itemClassLoader.getItemClassByClassName(loot.getKey());
					Item item = itemLoader.registerNew(itemClass);
					drops.add(item);
				}
			}
		}
		
		return drops;
	}
	
	public Map<String, Map<String, Double>> asMap() {
		if(lootTable == null) return new HashMap<>();
		
		Map<String, Map<String, Double>> result = new HashMap<>();
		
		for(Entry<String, Object> regions : lootTable.entrySet()) {
			Map<String, Double> regionItemChances = new HashMap<>();
			String regionName = regions.getKey();
			Document chances = (Document) regions.getValue();
			for(Entry<String, Object> itemChance : chances.entrySet()) {
				regionItemChances.put(itemChance.getKey(), (double) itemChance.getValue());
			}
			result.put(regionName, regionItemChances);
		}
		
		return result;
	}
}
