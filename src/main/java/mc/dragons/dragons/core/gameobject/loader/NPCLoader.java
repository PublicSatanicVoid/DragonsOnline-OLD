package mc.dragons.dragons.core.gameobject.loader;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;
import mc.dragons.dragons.core.storage.StorageUtil;
import net.md_5.bungee.api.ChatColor;

public class NPCLoader extends GameObjectRegistry {
	
	private static NPCLoader INSTANCE;
	
	private NPCLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
	}
	
	public synchronized static NPCLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new NPCLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public NPC loadObject(StorageAccess storageAccess) {
		Location loc = StorageUtil.docToLoc((Document)storageAccess.get("lastLocation"));
		Entity e = loc.getWorld().spawnEntity(loc, EntityType.valueOf((String)storageAccess.get("entityType")));
		return new NPC(e, storageManager, storageAccess);
	}
	
	public NPC registerNew(Entity e, String name, double maxHealth, int level, boolean hostile) {
		Document data = new Document("_id", UUID.randomUUID())
				.append("name", name)
				.append("entityType", e.getType().toString())
				.append("maxHealth", maxHealth)
				.append("lastLocation", StorageUtil.locToDoc(e.getLocation()))
				.append("level", level)
				.append("hostile", Boolean.toString(hostile));
		e.setCustomNameVisible(true);
		e.setCustomName(name + ChatColor.GRAY + " Lv " + level);
		// TODO: continue init
		// TODO: enforce hostile/non-hostile behavior???
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.NPC, data);
		NPC npc = new NPC(e, storageManager, storageAccess);
		npc.setMaxHealth(maxHealth);
		npc.setHealth(maxHealth);
		e.setMetadata("handle", new FixedMetadataValue(plugin, npc));
		return npc;
	}
	
}
