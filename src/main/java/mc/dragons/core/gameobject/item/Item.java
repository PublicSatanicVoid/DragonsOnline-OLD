package mc.dragons.core.gameobject.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.util.HiddenStringUtil;

/**
 * Represents a general item in the RPG.
 * 
 * <p>In addition to regular properties of Minecraft items,
 * RPG items have additional properties, like XP/skill
 * requirements, use effects, etc.
 * 
 * <p>More than one RPG item may be mapped to the same
 * Minecraft item, and vice versa.
 * 
 * @author Rick
 *
 */
public class Item extends GameObject {
	
	private ItemStack itemStack;
	
	private List<String> getCompleteLore(String[] customLore) {
		String dataTag = HiddenStringUtil.encodeString(getUUID().toString());
		List<String> lore = new ArrayList<>(Arrays.asList(ChatColor.GRAY + "Lv Min: " + getLevelMin() + dataTag));
		//lore.add(HiddenStringUtil.encodeString(getUUID().toString())); // appears as a blank line
		if(customLore.length > 0) {
			lore.add("");
		}
		lore.addAll(Arrays.asList(customLore).stream().map(line -> ChatColor.LIGHT_PURPLE + " " + ChatColor.ITALIC + line).collect(Collectors.toList()));

		List<String> statsMeta = new ArrayList<>();
		if(getDamage() > 0.0) {
			statsMeta.add(ChatColor.GREEN + " " + getDamage() + " Damage");
		}
		if(getArmor() > 0.0) {
			statsMeta.add(ChatColor.GREEN + " " + getArmor() + " Armor");
		}
		if(isWeapon()) {
			statsMeta.add(ChatColor.GREEN + " " + getCooldown() + "s Attack Speed");
		}
		if(getSpeedBoost() != 0.0) {
			statsMeta.add(" " + (getSpeedBoost() < 0.0 ? ChatColor.RED + "" : ChatColor.GREEN + "+") + getSpeedBoost() + " Walk Speed");
		}
		if(isUnbreakable() || isUndroppable()) {
			statsMeta.add("");
		}
		if(isUnbreakable()) {
			statsMeta.add(ChatColor.BLUE + "Unbreakable");
		}
		if(isUndroppable()) {
			statsMeta.add(ChatColor.BLUE + "Undroppable");
		}
		if(isCustom()) {
			statsMeta.addAll(Arrays.asList("", ChatColor.DARK_AQUA + "Custom Item"));
		}
		if(statsMeta.size() > 0) {
			lore.addAll(Arrays.asList("", ChatColor.GRAY + "When equipped:"));
			lore.addAll(statsMeta);
		}
		return lore;
	}
	
	public Item(ItemStack itemStack, StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		LOGGER.fine("Constructing RPG Item (" + itemStack + ", " + storageManager + ", " + storageAccess + ")");
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + getDecoratedName());
		meta.setLore(getCompleteLore(getLore().toArray(new String[getLore().size()])));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		itemStack.setItemMeta(meta);
		itemStack.setAmount(getQuantity());
		if(isUnbreakable()) {
			meta.spigot().setUnbreakable(true);
			//Dragons.getInstance().getBridge().setItemStackUnbreakable(itemStack, true);
		}
		this.itemStack = itemStack;
	}
	
	public void updateItemStackData() {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + getDecoratedName());
		meta.setLore(getCompleteLore(getLore().toArray(new String[getLore().size()])));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		itemStack.setItemMeta(meta);
		itemStack.setAmount(getQuantity());
	}
	
	public boolean isCustom() {
		return (boolean) getData("isCustom");
	}
	
	public void setCustom(boolean custom) {
		setData("isCustom", custom);
	}
	
	public String getClassName() {
		return (String) getData("className");
	}
	
	public int getQuantity() {
		return (int) getData("quantity");
	}
	
	public void setQuantity(int quantity) {
		setData("quantity", quantity);
		itemStack.setAmount(quantity);
	}
	
	public void resyncQuantityFromBukkit() {
		setData("quantity", itemStack.getAmount());
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public ChatColor getNameColor() {
		return ChatColor.valueOf((String) getData("nameColor"));
	}
	
	public void setNameColor(ChatColor nameColor) {
		setData("nameColor", nameColor.name());
	}
	
	public void setName(String name) {
		setData("name", name);
	}
	
	public double getSpeedBoost() {
		return (double) getData("speedBoost");
	}
	
	public void setSpeedBoost(double speedBoost) {
		setData("speedBoost", speedBoost);
	}
	
	public ItemStack rename(String name) {
		setName(name);
		return localRename(name);
	}
	
	public ItemStack localRename(String name) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(name);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	
	public ItemStack relore(String[] lore) {
		setLore(Arrays.asList(lore));
		return localRelore(lore);
	}
	
	public ItemStack localRelore(String[] lore) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setLore(getCompleteLore(lore));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	
//	Should be obsolete now that we embed UUID directly into item lore. (Hopefully)
//	Actually it's not but there's a better way (just return the item stack and handle updates at caller level)
//	/**
//	 * Safely renames the item locally (no persistence),
//	 * without invalidating the ItemLoader's references
//	 * to the item stack.
//	 * 
//	 * @param getStackFunction Must return the current item stack (must be valid and current before and after rename operation)
//	 * @param newLocalName
//	 */
//	public void safeLocalRename(Supplier<ItemStack> getStackFunction, String newLocalName) {
//		ItemMeta meta = getStackFunction.get().getItemMeta();
//		meta.setDisplayName(newLocalName);
//		updateMetaSafely(getStackFunction, meta);
//	}
//	
//	/**
//	 * Safely renames the item permanently,
//	 * without invalidating the ItemLoader's references
//	 * to the item stack.
//	 * 
//	 * @param getStackFunction Must return the current item stack (must be valid and current before and after rename operation)
//	 * @param newLocalName
//	 */
//	public void safePermanentRename(Supplier<ItemStack> getStackFunction, String newName) {
//		safeLocalRename(getStackFunction, newName);
//		setName(newName);
//	}
//	
//	/**
//	 * Safely re-lores the item locally (no persistence),
//	 * without invalidating the ItemLoader's references
//	 * to the item stack.
//	 * 
//	 * @param getStackFunction Must return the current item stack (must be valid and current before and after rename operation)
//	 * @param newLocalName
//	 */
//	public void safeLocalRelore(Supplier<ItemStack> getStackFunction, String[] newLocalLore) {
//		ItemMeta meta = getStackFunction.get().getItemMeta();
//		meta.setLore(getCompleteLore(newLocalLore));
//		updateMetaSafely(getStackFunction, meta);
//	}
//	
//	/**
//	 * Safely re-lores the item permanently,
//	 * without invalidating the ItemLoader's references
//	 * to the item stack.
//	 * 
//	 * @param getStackFunction Must return the current item stack (must be valid and current before and after rename operation)
//	 * @param newLocalName
//	 */
//	public void safePermanentRelore(Supplier<ItemStack> getStackFunction, String[] newLore) {
//		safeLocalRelore(getStackFunction, newLore);
//		setLore(Arrays.asList(newLore));
//	}
//	
//	/**
//	 * Safely updates the item's underlying item meta,
//	 * without invalidating the ItemLoader's references
//	 * to the item stack.
//	 * 
//	 * @param getItemStackFunction
//	 * @param newItemMeta
//	 */
//	public void updateMetaSafely(Supplier<ItemStack> getItemStackFunction, ItemMeta newItemMeta) {
//		ItemStack oldStack = getItemStackFunction.get();
//		oldStack.setItemMeta(newItemMeta);
//		this.itemStack = getItemStackFunction.get();
//		ItemLoader.updateBukkitReference(oldStack, this.itemStack, this);
//	}
//	
//	/**
//	 * Safely updates the item's underlying item stack,
//	 * without invalidating the ItemLoader's references
//	 * to the item stack.
//	 * 
//	 * @param newItemStack
//	 */
//	public void updateItemStackSafely(ItemStack newItemStack) {
//		ItemStack oldStack = this.itemStack;
//		this.itemStack = newItemStack;
//		ItemLoader.updateBukkitReference(oldStack, newItemStack, this);
//	}
	
	public String getDecoratedName() {
		return getNameColor() + getName();
	}
	
	public Material getMaterial() {
		return Material.valueOf((String) getData("materialType"));
	}
	
	public void setMaterial(Material material) {
		setData("materialType", material.toString());
	}
	
	public int getLevelMin() {
		return (int) getData("lvMin");
	}
	
	public void setLevelMin(int lvMin) {
		setData("lvMin", lvMin);
	}
	
	public double getCooldown() {
		return (double) getData("cooldown");
	}
	
	public void setCooldown(double cooldown) {
		setData("cooldown", cooldown);
	}
	
	public boolean isWeapon() {
		Material type = getMaterial();
		return type == Material.BOW || type == Material.DIAMOND_SWORD || type == Material.GOLD_SWORD || type == Material.IRON_SWORD || type == Material.STONE_SWORD || type == Material.WOOD_SWORD
				|| type == Material.STICK;
	}
	
	public boolean isUnbreakable() {
		return (boolean) getData("unbreakable");
	}
	
	public void setUnbreakable(boolean unbreakable) {
		setData("unbreakable", true);
	}
	
	public boolean isUndroppable() {
		return (boolean) getData("undroppable");
	}
	
	public void setUndroppable(boolean undroppable) {
		setData("undroppable", undroppable);
	}
	
	public double getDamage() {
		return (double) getData("damage");
	}
	
	public void setDamage(double damage) {
		setData("damage", damage);
	}
	
	public double getArmor() {
		return (double) getData("armor");
	}
	
	public void setArmor(double armor) {
		setData("armor", armor);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLore() {
		return (List<String>) getData("lore");
	}
	
	public void setLore(List<String> lore) {
		setData("lore", lore);
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		updateItemStackData();
	}
	
//	public void setItemStack(ItemStack itemStack) {
//		ItemLoader.updateBukkitReference(this.itemStack, itemStack, this);
//		this.itemStack = itemStack;
//	}
	
	public void registerUse() {
		getLocalData().append("lastUsed", System.currentTimeMillis());
	}
	
	public double getCooldownRemaining() {
		return Math.max(0.0, getCooldown() - (double) (System.currentTimeMillis() -	(long) getLocalData().getOrDefault("lastUsed", 0L)) / 1000.0);
	}
	
	public boolean hasCooldownRemaining() {
		return Math.abs(getCooldownRemaining()) > 0.001;
	}
	
	public void autoSave() {
		super.autoSave();
		setData("quantity", itemStack.getAmount());
	}
}
