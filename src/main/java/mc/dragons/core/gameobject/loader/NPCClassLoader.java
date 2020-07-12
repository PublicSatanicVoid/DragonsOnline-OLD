package mc.dragons.core.gameobject.loader;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.entity.EntityType;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.npc.NPC.NPCType;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.npc.NPCConditionalActions.NPCTrigger;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

public class NPCClassLoader extends GameObjectLoader<NPCClass> {
	
	private static NPCClassLoader INSTANCE;
	private Logger LOGGER = Dragons.getInstance().getLogger();
	private GameObjectRegistry masterRegistry;
	private boolean allLoaded = false;
	
	private NPCClassLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		masterRegistry = instance.getGameObjectRegistry();
	}
	
	public synchronized static NPCClassLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new NPCClassLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public NPCClass loadObject(StorageAccess storageAccess) {
		lazyLoadAll();
		LOGGER.fine("Loading NPC class " + storageAccess.getIdentifier());
		NPCClass npcClass = new NPCClass(storageManager, storageAccess);
		masterRegistry.getRegisteredObjects().add(npcClass);
		return npcClass;
	}
	
	public NPCClass getNPCClassByClassName(String npcClassName) {
		lazyLoadAll();
		for(GameObject gameObject : masterRegistry.getRegisteredObjects(GameObjectType.NPC_CLASS)) {
			NPCClass npcClass = (NPCClass) gameObject;
			if(npcClass.getClassName().equalsIgnoreCase(npcClassName)) {
				return npcClass;
			}
		}
		return null;
	}
	
	public NPCClass registerNew(String className, String name, EntityType entityType, double maxHealth, int level, NPCType npcType) {
		lazyLoadAll();
		LOGGER.fine("Registering new NPC class (" + className + ")");
		Document emptyConditionals = new Document();
		for(NPCTrigger trigger : NPCTrigger.values()) {
			emptyConditionals.append(trigger.toString(), new ArrayList<Document>());
		}
		Document data = new Document("_id", UUID.randomUUID())
				.append("className", className)
				.append("name", name)
				.append("entityType", entityType.toString())
				.append("maxHealth", maxHealth)
				.append("level", level)
				.append("ai", npcType.hasAIByDefault())
				.append("immortal", npcType.isImmortalByDefault())
				.append("npcType", npcType.toString())
				.append("lootTable", new Document())
				.append("conditionals", emptyConditionals);
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.NPC_CLASS, data);
		NPCClass npcClass = new NPCClass(storageManager, storageAccess);
		masterRegistry.getRegisteredObjects().add(npcClass);
		return npcClass;
	}	
	
	public void loadAll(boolean force) {
		if(allLoaded && !force) return;
		LOGGER.fine("Loading all NPC classes...");
		allLoaded = true; // must be here to prevent infinite recursion -> stack overflow -> death
		masterRegistry.removeFromRegistry(GameObjectType.NPC_CLASS);
		storageManager.getAllStorageAccess(GameObjectType.NPC_CLASS).stream().forEach((storageAccess) -> {
			masterRegistry.getRegisteredObjects().add(loadObject(storageAccess));	
		});
	}
	
	public void lazyLoadAll() {
		loadAll(false);
	}
	
}