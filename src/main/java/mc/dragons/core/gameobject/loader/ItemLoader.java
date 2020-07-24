package mc.dragons.core.gameobject.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.item.ItemClass;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.util.HiddenStringUtil;

public class ItemLoader extends GameObjectLoader<Item> {

	private static ItemLoader INSTANCE;
	private Logger LOGGER = Dragons.getInstance().getLogger();
	private GameObjectRegistry masterRegistry;
	//private static Map<ItemStack, Item> itemStackToItem;
	private static Map<String, Item> uuidToItem;
	
	private ItemLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		masterRegistry = instance.getGameObjectRegistry();
		//itemStackToItem = new HashMap<>();
		uuidToItem = new HashMap<>();
	}
	
	public synchronized static ItemLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new ItemLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public Item loadObject(StorageAccess storageAccess) {
		if(storageAccess == null) return null;
		LOGGER.fine("Loading item by storage access " + storageAccess.getIdentifier());
		Material type = Material.valueOf((String) storageAccess.get("materialType"));
		ItemStack itemStack = new ItemStack(type);
		Item item = new Item(itemStack, storageManager, storageAccess);
		masterRegistry.getRegisteredObjects().add(item);
		//itemStackToItem.put(itemStack, item);
		uuidToItem.put(item.getUUID().toString(), item);
		return new Item(itemStack, storageManager, storageAccess);
	}
	
	public Item loadObject(UUID uuid) {
		LOGGER.fine("Loading item by UUID " + uuid);
		return loadObject(storageManager.getStorageAccess(GameObjectType.ITEM, uuid));
	}
	
	public Item registerNew(ItemClass itemClass) {
		return registerNew(itemClass.getClassName(), itemClass.getName(), false, itemClass.getNameColor(), itemClass.getMaterial(), itemClass.getLevelMin(), itemClass.getCooldown(), itemClass.getSpeedBoost(),
				itemClass.isUnbreakable(), itemClass.isUndroppable(), itemClass.getDamage(), itemClass.getArmor(), itemClass.getLore(), itemClass.getMaxStackSize());
	}
	
	public Item registerNew(Item item) {
		return registerNew(item.getClassName(), item.getName(), item.isCustom(), item.getNameColor(), item.getMaterial(), item.getLevelMin(), item.getCooldown(), 
				item.getSpeedBoost(), item.isUnbreakable(), item.isUndroppable(), item.getDamage(), item.getArmor(), item.getLore(), item.getMaxStackSize());
	}
	
	public Item registerNew(String className, String name, boolean custom, ChatColor nameColor, Material material, int levelMin, double cooldown, double speedBoost, boolean unbreakable, 
			boolean undroppable, double damage, double armor, List<String> lore, int maxStackSize) {
		LOGGER.fine("Registering new item of class " + className);
		Document data = new Document("_id", UUID.randomUUID())
				.append("className", className)
				.append("name", name)
				.append("isCustom", custom)
				.append("nameColor", nameColor.name())
				.append("materialType", material.toString())
				.append("lvMin", levelMin)
				.append("cooldown", cooldown)
				.append("speedBoost", speedBoost)
				.append("unbreakable", unbreakable)
				.append("undroppable", undroppable)
				.append("damage", damage)
				.append("armor", armor)
				.append("lore", lore)
				.append("quantity", 1)
				.append("maxStackSize", maxStackSize);
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.ITEM, data);
		ItemStack itemStack = new ItemStack(material);
		Item item = new Item(itemStack, storageManager, storageAccess);
		//itemStackToItem.put(itemStack, item);
		uuidToItem.put(item.getUUID().toString(), item);
		masterRegistry.getRegisteredObjects().add(item);
		return item;
	}
	
	public static Item fromBukkit(ItemStack itemStack) {
		if(itemStack == null) return null;
		if(itemStack.getItemMeta() == null) return null;
		if(itemStack.getItemMeta().getLore() == null) return null;
		if(itemStack.getItemMeta().getLore().size() < 1) return null;
		//return itemStackToItem.get(itemStack);
		return uuidToItem.get(HiddenStringUtil.extractHiddenString(itemStack.getItemMeta().getLore().get(0)));
	}
}
