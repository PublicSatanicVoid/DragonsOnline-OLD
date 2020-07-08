package mc.dragons.core.gameobject.loader;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.floor.Floor;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

public class FloorLoader extends GameObjectLoader<Floor> {
	
	private static FloorLoader INSTANCE;
	
	private GameObjectRegistry masterRegistry;
	private Map<String, Floor> worldNameToFloor;
	private Map<String, Floor> floorNameToFloor;
	private boolean allLoaded = false;
	
	private FloorLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		masterRegistry = instance.getGameObjectRegistry();
		worldNameToFloor = new HashMap<>();
		floorNameToFloor = new HashMap<>();
	}
	
	public static synchronized FloorLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new FloorLoader(instance, storageManager);
		}
		return INSTANCE;
	}

	@Override
	public Floor loadObject(StorageAccess storageAccess) {
		lazyLoadAll();
		Floor floor = new Floor(storageManager, storageAccess, false); // Only need to specify if superflat the first time around
		masterRegistry.getRegisteredObjects().add(floor);
		worldNameToFloor.put(floor.getWorldName(), floor);
		floorNameToFloor.put(floor.getFloorName(), floor);
		return floor;
	}
	
	public Floor registerNew(String floorName, String worldName, String displayName, int levelMin, boolean superflat) {
		lazyLoadAll();
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.FLOOR, new Document("floorName", floorName)
				.append("worldName", worldName)
				.append("displayName", displayName)
				.append("levelMin", levelMin));
		
		Floor floor = new Floor(storageManager, storageAccess, superflat);
		masterRegistry.getRegisteredObjects().add(floor);
		worldNameToFloor.put(worldName, floor);
		floorNameToFloor.put(floorName, floor);
		return floor;
	}
	
	public Floor fromWorldName(String worldName) {
		lazyLoadAll();
		return worldNameToFloor.get(worldName);
	}
	
	public Floor fromWorld(World world) {
		lazyLoadAll();
		return worldNameToFloor.get(world.getName());
	}
	
	public Floor fromLocation(Location loc) {
		lazyLoadAll();
		return worldNameToFloor.get(loc.getWorld().getName());
	}
	
	public Floor fromFloorName(String floorName) {
		lazyLoadAll();
		return floorNameToFloor.get(floorName);
	}
	
	public void loadAll(boolean force) {
		if(allLoaded && !force) return;
		allLoaded = true;
		masterRegistry.removeFromRegistry(GameObjectType.FLOOR);
		storageManager.getAllStorageAccess(GameObjectType.FLOOR).stream().forEach((storageAccess) -> {
			masterRegistry.getRegisteredObjects().add(loadObject(storageAccess));	
		});
	}
	
	public void lazyLoadAll() {
		loadAll(false);
	}
}
