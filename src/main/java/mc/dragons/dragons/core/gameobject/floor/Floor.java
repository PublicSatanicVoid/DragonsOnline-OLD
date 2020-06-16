package mc.dragons.dragons.core.gameobject.floor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Represents a world with a unique name and level requirement.
 * 
 * There is typically a specific build style, set of regions and
 * NPCs, and dungeon/boss associated with each floor as well.
 * 
 * In production, all player-accessible floors should be in one-to-one
 * relation with a level requirement, so that they can be identified
 * by this number as well as their name. However, this will not be
 * programmatically enforced.
 * 
 * @author Rick
 *
 */
public class Floor extends GameObject {
	
	public Floor(StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		Dragons.getInstance().getLogger().info("Loading floor " + getFloorName() + " [" + getWorldName() + "]");
		World world = Bukkit.createWorld(WorldCreator.name(getWorldName()));
		world.setGameRuleValue("doMobSpawning", "false");
		world.setGameRuleValue("doEntityDrops", "false");
		world.setGameRuleValue("doMobLoot", "false");
		world.setGameRuleValue("doFireTick", "false");
		world.setGameRuleValue("mobGriefing", "false");
		world.setGameRuleValue("showDeathMessages", "false");
		world.setGameRuleValue("keepInventory", "true");
	}
	
	public String getWorldName() {
		return (String) getData("worldName");
	}
	
	public World getWorld() {
		return Bukkit.getWorld(getWorldName());
	}
	
	public String getFloorName() {
		return (String) getData("floorName");
	}
	
	public void setFloorName(String floorName) {
		setData("floorName", floorName);
	}
	
	public String getDisplayName() {
		return (String) getData("displayName");
	}
	
	public void setDisplayName(String displayName) {
		setData("displayName", displayName);
	}
	
	public int getLevelMin() {
		return (int) getData("levelMin");
	}
	
	public void setLevelMin(int levelMin) {
		setData("levelMin", levelMin);
	}
}
