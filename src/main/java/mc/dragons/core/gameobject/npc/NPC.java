package mc.dragons.core.gameobject.npc;

import java.util.UUID;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.util.ProgressBarUtil;
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
	protected GameObjectRegistry registry;

	protected static NPCClassLoader npcClassLoader;
	
	public NPC(Entity entity, StorageManager storageManager, StorageAccess storageAccess) {
		super(GameObjectType.NPC, (UUID) storageAccess.get("_id"), storageManager);
		this.entity = entity;
		this.registry = Dragons.getInstance().getGameObjectRegistry();
		entity.setCustomNameVisible(true);
		entity.setCustomName(getDecoratedName());
		
		if(npcClassLoader == null) {
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
		}
	}
	
	public NPCClass getNPCClass() {
		String className = (String) storageAccess.get("className");
		if(className == null) return null;
		return npcClassLoader.getNPCClassByClassName(className);
	}

	public void setMaxHealth(double maxHealth) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable) entity;
			damageable.setMaxHealth(maxHealth);
			setData("maxHealth", maxHealth);
		}
	}
	
	public double getMaxHealth() {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable) entity;
			return damageable.getMaxHealth();
		}
		return 0.0;
	}
	
	public void setHealth(double health) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable) entity;
			damageable.setHealth(health);
		}
	}
	
	public void damage(double damage, Entity source) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable) entity;
			damageable.damage(damage, source);
		}
	}

	public void damage(double damage) {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable) entity;
			damageable.damage(damage);
		}
	}
	
	public double getHealth() {
		if(entity instanceof Damageable) {
			Damageable damageable = (Damageable) entity;
			return damageable.getHealth();
		}
		return 0.0;
	}
	
	public void updateHealthBar() {
		updateHealthBar(0.0);
	}
	
	public void updateHealthBar(double additionalDamage) {
		entity.setCustomName(getDecoratedName()
			 + ChatColor.DARK_GRAY + " ["
			 + ProgressBarUtil.getHealthBar(getHealth() - additionalDamage, getMaxHealth())
			 + ChatColor.DARK_GRAY + "]" );
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public String getDecoratedName() {
		return (isHostile() ? ChatColor.RED : ChatColor.YELLOW) + getName() + ChatColor.GRAY + " Lv. " + getLevel();
	}
	
	public boolean isHostile() {
		return (boolean) getData("hostile");
	}
	
	public int getLevel() {
		return (int) getData("level");
	}
	
	public void remove() {
		entity.remove();
		registry.removeFromDatabase(this);
	}
	
	public Entity e() {
		return entity;
	}
	
}
