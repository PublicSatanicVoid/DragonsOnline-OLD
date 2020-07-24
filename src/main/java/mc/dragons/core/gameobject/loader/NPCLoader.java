package mc.dragons.core.gameobject.loader;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPC.NPCType;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.storage.StorageUtil;
import mc.dragons.core.storage.impl.LocalStorageAccess;
import mc.dragons.core.storage.impl.LocalStorageManager;
import mc.dragons.core.util.StringUtil;

public class NPCLoader extends GameObjectLoader<NPC> {
	
	private static NPCLoader INSTANCE;
	private Logger LOGGER = Dragons.getInstance().getLogger();
	private GameObjectRegistry masterRegistry;
	private boolean allPermanentLoaded = false;
	private LocalStorageManager localStorageManager;
	
	private NPCLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		masterRegistry = instance.getGameObjectRegistry();
		localStorageManager = instance.getLocalStorageManager();
	}
	
	public synchronized static NPCLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new NPCLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public NPC loadObject(StorageAccess storageAccess) {
		lazyLoadAllPermanent();
		LOGGER.fine("Loading NPC " + storageAccess.getIdentifier());
		NPCType npcType = NPCType.valueOf((String) storageAccess.get("npcType"));
		Location loc = StorageUtil.docToLoc((Document) storageAccess.get("lastLocation"));
		Entity e = loc.getWorld().spawnEntity(loc, EntityType.valueOf((String)storageAccess.get("entityType")));
		NPC npc = new NPC(e, npcType.isPersistent() ? storageManager : localStorageManager, npcType.isPersistent() ? storageAccess : localStorageManager.downgrade(storageAccess));
		e.setMetadata("handle", new FixedMetadataValue(plugin, npc));
		return npc;
	}
	
	public NPC registerNew(Entity entity, String npcClassName) {
		return registerNew(entity, ((NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader()).getNPCClassByClassName(npcClassName));
	}
	
	public NPC registerNew(World world, Location spawnLocation, String npcClassName) {
		return registerNew(world, spawnLocation, ((NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader()).getNPCClassByClassName(npcClassName));
	}
	
	public NPC registerNew(Entity entity, NPCClass npcClass) {
		return registerNew(entity, npcClass.getClassName(), npcClass.getName(), npcClass.getMaxHealth(), npcClass.getLevel(), npcClass.getNPCType(), npcClass.hasAI(), npcClass.isImmortal());
	}
	
	public NPC registerNew(World world, Location spawnLocation, NPCClass npcClass) {
		return registerNew(world, spawnLocation, npcClass.getEntityType(), npcClass.getClassName(), npcClass.getName(), 
				npcClass.getMaxHealth(), npcClass.getLevel(), npcClass.getNPCType(), npcClass.hasAI(), npcClass.isImmortal());
	}
	
	public NPC registerNew(World world, Location spawnLocation, EntityType entityType, String className, String name, double maxHealth, int level, NPCType npcType, boolean ai, boolean immortal) {
		Entity e = world.spawnEntity(spawnLocation, entityType);
		return registerNew(e, className, name, maxHealth, level, npcType, ai, immortal);
	}
	
	public static NPC fromBukkit(Entity entity) {
		if(entity == null) return null;
		if(!entity.hasMetadata("handle")) return null;
		if(entity.getMetadata("handle").size() == 0) return null;
		Object value = entity.getMetadata("handle").get(0).value();
		if(value instanceof NPC) return (NPC) value;
		return null;
	}
	
	public NPC registerNew(Entity entity, String className, String name, double maxHealth, int level, NPCType npcType, boolean ai, boolean immortal) {
		LOGGER.fine("Registering new NPC of class " + className + " using Bukkit entity " + StringUtil.entityToString(entity));
		lazyLoadAllPermanent();
		Document data = new Document("_id", UUID.randomUUID())
				.append("className", className)
				.append("name", name)
				.append("entityType", entity.getType().toString())
				.append("maxHealth", maxHealth)
				.append("lastLocation", StorageUtil.locToDoc(entity.getLocation()))
				.append("level", level)
				.append("npcType", npcType.toString())
				.append("ai", ai)
				.append("immortal", immortal)
				.append("lootTable", new Document());
		// TODO: enforce hostile/non-hostile behavior???
		StorageAccess storageAccess = npcType.isPersistent() ? storageManager.getNewStorageAccess(GameObjectType.NPC, data) : localStorageManager.getNewStorageAccess(GameObjectType.NPC, data);
		NPC npc = new NPC(entity, npcType.isPersistent() ? storageManager : localStorageManager, storageAccess);
		if(storageAccess instanceof LocalStorageAccess) {
			LOGGER.fine("- Using local storage access for NPC of type " + npcType + " (" + storageAccess + ")");
		}
		if(storageAccess == null) {
			LOGGER.warning("- Whoops! The storage access was null!");
		}
		npc.setMaxHealth(maxHealth);
		npc.setHealth(maxHealth);
		entity.setMetadata("handle", new FixedMetadataValue(plugin, npc));
		masterRegistry.getRegisteredObjects().add(npc);
		return npc;
	}

	public void loadAllPermanent(boolean force) {
		if(allPermanentLoaded && !force) return;
		LOGGER.fine("Loading all permanent NPCs...");
		allPermanentLoaded = true; // must be here to prevent infinite recursion -> stack overflow -> death
		masterRegistry.removeFromRegistry(GameObjectType.NPC);
		storageManager.getAllStorageAccess(GameObjectType.NPC, new Document("$or", 
				Arrays.asList(NPCType.values())
					.stream()
					.filter(type -> type.isPersistent())
					.map(type -> new Document("npcType", type.toString()))
					.collect(Collectors.toList())))
			.stream()
			.forEach((storageAccess) -> {
				NPC npc = loadObject(storageAccess);
				Dragons.getInstance().getLogger().fine("Loaded permanent NPC: " + npc.getIdentifier() + " of class " + npc.getNPCClass().getClassName());
				masterRegistry.getRegisteredObjects().add(npc);
		});
	}
	
	public void lazyLoadAllPermanent() {
		loadAllPermanent(false);
	}
}
