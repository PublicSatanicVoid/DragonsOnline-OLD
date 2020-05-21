package mc.dragons.dragons.core.gameobject.npc;

import java.util.UUID;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.HealthBarUtil;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;
import net.md_5.bungee.api.ChatColor;

/**
 * Represents an NPC in the RPG.
 * 
 * <p>In addition to the properties of Minecraft NPCs,
 * RPG NPCs have properties like dialogue and enhanced
 * combat capabilities, as well as levels and more specific
 * interaction options.
 * 
 * <p>There is a many-to-many has-a relationship between
 * RPG NPC types and Minecraft NPC types.
 * 
 * @author Rick
 *
 */
public class NPC extends GameObject {

	protected Entity entity;
	
	public NPC(Entity entity, StorageManager storageManager, StorageAccess storageAccess) {
		super(GameObjectType.NPC, (UUID)storageAccess.get("_id"), storageManager);
		this.entity = entity;
	}

	public void setMaxHealth(double maxHealth) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable)entity;
			damageable.setMaxHealth(maxHealth);
			setData("maxHealth", maxHealth);
		}
	}
	
	public double getMaxHealth() {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable)entity;
			return damageable.getMaxHealth();
		}
		return 0.0;
	}
	
	public void setHealth(double health) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable)entity;
			damageable.setHealth(health);
		}
	}
	
	public void damage(double damage, Entity source) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable)entity;
			damageable.damage(damage, source);
		}
	}

	public void damage(double damage) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable)entity;
			damageable.damage(damage);
		}
	}
	
	public double getHealth() {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable)entity;
			return damageable.getHealth();
		}
		return 0.0;
	}
	
	public void updateHealthBar() {
		entity.setCustomName(getName() + ChatColor.GRAY + " Lv. " + getLevel() + ChatColor.GRAY + " [" + HealthBarUtil.getHealthBar(getHealth(), getMaxHealth()) + ChatColor.DARK_GRAY + "]");
	}
	
	public String getName() {
		return (String)getData("name");
	}
	
	public int getLevel() {
		return (int)getData("level");
	}
	
	public Entity e() {
		return entity;
	}
	
}
