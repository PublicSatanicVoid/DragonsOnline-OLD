package mc.dragons.core.gameobject.item;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

public class ItemClass extends GameObject {
	
	public ItemClass(StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
	}
	
	public String getClassName() {
		return (String) getData("className");
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public void setName(String name) {
		setData("name", name);
	}

	public ChatColor getNameColor() {
		return ChatColor.valueOf((String) getData("nameColor"));
	}
	
	public void setNameColor(ChatColor nameColor) {
		setData("nameColor", nameColor.name());
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
	
	@SuppressWarnings("unchecked")
	public List<String> getLore() {
		return (List<String>) getData("lore");
	}
	
	public void setLore(List<String> lore) {
		setData("lore", lore);
	}
}
