package mc.dragons.dragons.core.gameobject.npc;

import org.bukkit.entity.EntityType;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class NPCClass extends GameObject {
	
	public NPCClass(StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
	}

	public String getClassName() {
		return (String) getData("className");
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public void setName(String displayName) {
		setData("name", displayName);
	}
	
	public EntityType getEntityType() {
		return EntityType.valueOf((String) getData("entityType"));
	}
	
	public void setEntityType(EntityType type) {
		setData("entityType", type.toString());
	}
	
	public double getMaxHealth() {
		return (double) getData("maxHealth");
	}
	
	public void setMaxHealth(double maxHealth) {
		setData("maxHealth", maxHealth);
	}
	
	public int getLevel() {
		return (int) getData("level");
	}
	
	public void setLevel(int level) {
		setData("level", level);
	}
	
	public boolean isHostile() {
		return (boolean) getData("hostile");
	}
	
	public void setHostile(boolean hostile) {
		setData("hostile", hostile);
	}
}
