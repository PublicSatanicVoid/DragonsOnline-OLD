package mc.dragons.core.gameobject.npc;

import org.bson.Document;
import org.bukkit.entity.EntityType;

import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.npc.NPC.NPCType;
import mc.dragons.core.gameobject.npc.NPCConditionalActions.NPCTrigger;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

public class NPCClass extends GameObject {
	
	private LootTable lootTable;
	private NPCConditionalActions[] conditionals = new NPCConditionalActions[NPCTrigger.values().length];
	
	public NPCClass(StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		lootTable = new LootTable(this);
		int i = 0;
		for(NPCTrigger trigger : NPCTrigger.values()) {
			conditionals[i] = new NPCConditionalActions(trigger, this);
			i++;
		}
	}
	
	public void executeConditionals(NPCTrigger trigger, User user) {
		user.debug("Executing conditionals");
		for(NPCConditionalActions conditionalAction : conditionals) {
			if(conditionalAction.getTrigger() == trigger) {
				conditionalAction.executeConditionals(user);
			}
		}
	}
	
	public NPCConditionalActions getConditionalActions(NPCTrigger trigger) {
		for(NPCConditionalActions conditionalAction : conditionals) {
			if(conditionalAction.getTrigger() == trigger) {
				return conditionalAction;
			}
		}
		return null;
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
	
	public boolean isImmortal() {
		return (boolean) getData("immortal");
	}
	
	public void setImmortal(boolean immortal) {
		setData("immortal", immortal);
	}
	
	public boolean hasAI() {
		return (boolean) getData("ai");
	}
	
	public void setAI(boolean hasAI) {
		setData("ai", hasAI);
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
	
	public NPCType getNPCType() {
		return NPCType.valueOf((String) getData("npcType"));
	}
	
	public void setNPCType(NPCType npcType) {
		setData("npcType", npcType);
	}
}
