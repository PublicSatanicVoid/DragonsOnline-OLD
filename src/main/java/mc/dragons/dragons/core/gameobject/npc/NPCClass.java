package mc.dragons.dragons.core.gameobject.npc;

import org.bson.Document;
import org.bukkit.entity.EntityType;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class NPCClass extends GameObject {
	
	private LootTable lootTable;
	
	public NPCClass(StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		lootTable = new LootTable(this);
	}
	
	public LootTable getLootTable() {
		return lootTable;
	}
	
	public void updateLootTable(String regionName, String itemName, double lootChancePercent) {
		Document lootTableData = (Document) storageAccess.get("lootTable");
		Document regionLoot = (Document) lootTableData.get(regionName);
		if(regionLoot == null) {
			lootTableData.append(regionName, new Document(itemName, lootChancePercent));
			return;
		}
		regionLoot.append(itemName, lootChancePercent);
		update(new Document("lootTable", lootTableData));
	}

	public void deleteFromLootTable(String regionName, String itemName) {
		Document lootTableData = (Document) storageAccess.get("lootTable");
		Document regionLoot = (Document) lootTableData.get(regionName);
		if(regionLoot == null) return;
		regionLoot.remove(itemName);
		update(new Document("lootTable", lootTableData));
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
