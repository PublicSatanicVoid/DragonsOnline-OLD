package mc.dragons.dragons.core.gameobject.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

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
	
	public Item(ItemStack itemStack, StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + getDecoratedName());
		if(isUnbreakable()) {
			meta.spigot().setUnbreakable(true);
		}
		List<String> lore = new ArrayList<>(Arrays.asList(ChatColor.GRAY + "Lv Min: " + getLevelMin(), ""));
		lore.addAll(getLore().stream().map(line -> ChatColor.LIGHT_PURPLE + " " + ChatColor.ITALIC + line).collect(Collectors.toList()));
		lore.addAll(Arrays.asList("", ChatColor.GRAY + "When equipped:"));
		if(getDamage() > 0.0) {
			lore.add(ChatColor.GREEN + " " + getDamage() + " Damage");
		}
		if(getArmor() > 0.0) {
			lore.add(ChatColor.GREEN + " " + getArmor() + " Armor");
		}
		if(isWeapon()) {
			lore.add(ChatColor.GREEN + " " + getCooldown() + "s Attack Speed");
		}
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		itemStack.setItemMeta(meta);
		this.itemStack = itemStack;
	}
	
	public String getClassName() {
		return (String) getData("className");
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
	
	public List<String> getLore() {
		return (List<String>) getData("lore");
	}
	
	public void setLore(List<String> lore) {
		setData("lore", lore);
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public void registerUse() {
		getLocalData().append("lastUsed", System.currentTimeMillis());
	}
	
	public double getCooldownRemaining() {
		return Math.max(0.0, getCooldown() - (double) (System.currentTimeMillis() -	(long) getLocalData().getOrDefault("lastUsed", 0L)) / 1000.0);
	}
	
	public boolean hasCooldownRemaining() {
		return Math.abs(getCooldownRemaining()) > 0.001;
	}
}
