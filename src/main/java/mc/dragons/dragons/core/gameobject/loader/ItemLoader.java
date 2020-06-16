package mc.dragons.dragons.core.gameobject.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.item.ItemClass;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class ItemLoader extends GameObjectLoader<Item> {

	private static ItemLoader INSTANCE;
	private GameObjectRegistry masterRegistry;
	private Map<ItemStack, Item> itemStackToItem;
	
	private ItemLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		masterRegistry = instance.getGameObjectRegistry();
		itemStackToItem = new HashMap<>();
	}
	
	public synchronized static ItemLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new ItemLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public Item loadObject(StorageAccess storageAccess) {
		Material type = Material.valueOf((String) storageAccess.get("materialType"));
		ItemStack itemStack = new ItemStack(type);
		Item item = new Item(itemStack, storageManager, storageAccess);
		masterRegistry.getRegisteredObjects().add(item);
		itemStackToItem.put(itemStack, item);
		return new Item(itemStack, storageManager, storageAccess);
	}
	
	public Item loadObject(UUID uuid) {
		return loadObject(storageManager.getStorageAccess(GameObjectType.ITEM, uuid));
	}
	
	public Item registerNew(ItemClass itemClass) {
		return registerNew(itemClass.getClassName(), itemClass.getName(), itemClass.getNameColor(), itemClass.getMaterial(), itemClass.getLevelMin(), itemClass.getCooldown(), itemClass.isUnbreakable(),
				itemClass.getDamage(), itemClass.getArmor(), itemClass.getLore());
	}
	
	public Item registerNew(String className, String name, ChatColor nameColor, Material material, int levelMin, double cooldown, boolean unbreakable, double damage, double armor, List<String> lore) {
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
				.append("lore", lore);
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.ITEM, data);
		ItemStack itemStack = new ItemStack(material);
		Item item = new Item(itemStack, storageManager, storageAccess);
		itemStackToItem.put(itemStack, item);
		masterRegistry.getRegisteredObjects().add(item);
		return item;
	}
	
	public Item getItemByItemStack(ItemStack itemStack) {
		return itemStackToItem.get(itemStack);
	}

}
