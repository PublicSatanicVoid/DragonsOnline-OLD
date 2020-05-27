package mc.dragons.dragons.core.gameobject.loader;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.bukkit.Location;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.region.Region;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;
import mc.dragons.dragons.core.storage.StorageUtil;

public class RegionLoader extends GameObjectRegistry {

	private static RegionLoader INSTANCE;
	private boolean allLoaded = false;
	
	private RegionLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		//loadAll(); // Apparently when we do this it thinks that GameObjectType.REGION is null until construction is completed. So we need to move this out of constructor. Grr lazy loading
	}
	
	public synchronized static RegionLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new RegionLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public Region loadObject(StorageAccess storageAccess) {
		lazyLoadAll();
		return new Region(storageManager, storageAccess);
	}
	
	public Region getRegionByName(String name) {
		lazyLoadAll();
		for(GameObject gameObject : getRegisteredObjects(GameObjectType.REGION)) {
			Region region = (Region) gameObject;
			if(region.getName().equalsIgnoreCase(name)) {
				return region;
			}
		}
		return null;
	}
	
	public Set<Region> getRegionsByLocation(Location loc) {
		lazyLoadAll();
		Set<Region> result = new HashSet<>();
		for(GameObject gameObject : getRegisteredObjects(GameObjectType.REGION)) {
			Region region = (Region) gameObject;
			if(region.contains(loc)) {
				result.add(region);
			}
		}
		return result;
	}
	
	public Set<Region> getRegionsByLocationXZ(Location loc) {
		lazyLoadAll();
		Set<Region> result = new HashSet<>();
		for(GameObject gameObject : getRegisteredObjects(GameObjectType.REGION)) {
			Region region = (Region) gameObject;
			if(region.containsXZ(loc)) {
				result.add(region);
			}
		}
		return result;
	}
	
	public Region registerNew(String name, Location corner1, Location corner2) {
		lazyLoadAll();
		if(corner1.getWorld() != corner2.getWorld()) {
			throw new IllegalArgumentException("Corners must be in the same world");
		}
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.REGION);
		storageAccess.set("name", name);
		storageAccess.set("world", corner1.getWorld().getName());
		storageAccess.set("corner1", StorageUtil.vecToDoc(corner1.toVector()));
		storageAccess.set("corner2", StorageUtil.vecToDoc(corner2.toVector()));
		storageAccess.set("flags", new Document("fullname", "New Region").append("desc", "Description of new region").append("lvmin", "0").append("lvrec", "0").append("showtitle", "false")
				.append("allowhostile", "true").append("pvp", "true").append("pve", "true"));
		storageAccess.set("spawnRates", new Document());
		Region region = new Region(storageManager, storageAccess);
		registeredObjects.add(region);
		return region;
	}
	
	public void loadAll(boolean force) {
		if(allLoaded && !force) return;
		allLoaded = true; // must be here to prevent infinite recursion -> stack overflow -> death
		removeFromRegistry(GameObjectType.REGION);
		storageManager.getAllStorageAccess(GameObjectType.REGION).stream().forEach((storageAccess) -> {
			super.loadObject(storageAccess);	
		});
	}
	
	public void lazyLoadAll() {
		loadAll(false);
	}

}
