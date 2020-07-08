package mc.dragons.core.gameobject.loader;

import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.ItemClass;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

public class ItemClassLoader extends GameObjectLoader<ItemClass> {

	private static ItemClassLoader INSTANCE;
	private GameObjectRegistry masterRegistry;
	private boolean allLoaded = false;
	
	private ItemClassLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		masterRegistry = instance.getGameObjectRegistry();
	}
	
	public synchronized static ItemClassLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new ItemClassLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public ItemClass loadObject(StorageAccess storageAccess) {
		ItemClass itemClass = new ItemClass(storageManager, storageAccess);
		masterRegistry.getRegisteredObjects().add(itemClass);
		return itemClass;
	}
	
	public ItemClass getItemClassByClassName(String itemClassName) {
		lazyLoadAll();
		for(GameObject gameObject : masterRegistry.getRegisteredObjects(GameObjectType.ITEM_CLASS)) {
			ItemClass itemClass = (ItemClass) gameObject;
			if(itemClass.getClassName().equalsIgnoreCase(itemClassName)) {
				return itemClass;
			}
		}
		return null;
	}
	
	public ItemClass registerNew(String className, String name, ChatColor nameColor, Material material, int levelMin, double cooldown, double speedBoost, boolean unbreakable, double damage, double armor, List<String> lore) {
		lazyLoadAll();
		Document data = new Document("_id", UUID.randomUUID())
				.append("className", className)
				.append("name", name)
				.append("nameColor", nameColor.name())
				.append("materialType", material.toString())
				.append("lvMin", levelMin)
				.append("cooldown", cooldown)
				.append("unbreakable", unbreakable)
				.append("damage", damage)
				.append("armor", armor)
				.append("speedBoost", speedBoost)
				.append("lore", lore);
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.ITEM_CLASS, data);
		ItemClass itemClass = new ItemClass(storageManager, storageAccess);
		masterRegistry.getRegisteredObjects().add(itemClass);
		return itemClass;
	}
	
	public void loadAll(boolean force) {
		if(allLoaded && !force) return;
		allLoaded = true;
		masterRegistry.removeFromRegistry(GameObjectType.ITEM_CLASS);
		storageManager.getAllStorageAccess(GameObjectType.ITEM_CLASS).stream().forEach((storageAccess) -> {
			masterRegistry.getRegisteredObjects().add(loadObject(storageAccess));	
		});
	}
	
	public void lazyLoadAll() {
		loadAll(false);
	}

	
	
}
