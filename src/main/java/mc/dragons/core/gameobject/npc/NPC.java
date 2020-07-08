package mc.dragons.core.gameobject.npc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.util.EntityHider;
import mc.dragons.core.util.EntityHider.Policy;
import mc.dragons.core.util.ProgressBarUtil;

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
	
	public enum NPCType {
		HOSTILE(ChatColor.RED, ""),
		NEUTRAL(ChatColor.YELLOW, ""),
		QUEST(ChatColor.DARK_GREEN, ChatColor.DARK_GREEN + "[NPC] "),
		SHOP(ChatColor.DARK_AQUA, ChatColor.DARK_AQUA + "[NPC] ");
		
		private ChatColor nameColor;
		private String prefix;
		
		NPCType(ChatColor nameColor, String prefix) {
			this.nameColor = nameColor;
			this.prefix = prefix;
		}
		
		public ChatColor getNameColor() {
			return nameColor;
		}
		
		public String getPrefix() {
			return prefix;
		}
	};

	protected Entity entity;
	protected GameObjectRegistry registry;

	protected static NPCClassLoader npcClassLoader;
	protected static EntityHider entityHider;
	
	public NPC(Entity entity, StorageManager storageManager, StorageAccess storageAccess) {
		super(GameObjectType.NPC, (UUID) storageAccess.get("_id"), storageManager);
		
		if(npcClassLoader == null) {
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
		}
		if(entityHider == null) {
			entityHider = new EntityHider(Dragons.getInstance(), Policy.BLACKLIST);
		}
		
		this.entity = entity;
		this.registry = Dragons.getInstance().getGameObjectRegistry();
	
		initializeEntity();
		
		Dragons.getInstance().getBridge().setEntityAI(entity, getNPCClass().hasAI());
		//Dragons.getInstance().getBridge().setEntityInvulnerable(entity, isImmortal());
	}
	
	public void initializeEntity() {
		entity.setCustomNameVisible(true);
		entity.setCustomName(getDecoratedName());
		entity.setMetadata("handle", new FixedMetadataValue(Dragons.getInstance(), this));
	}
	
	public NPCClass getNPCClass() {
		String className = (String) getData("className");
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
	
	public boolean isImmortal() {
		return (boolean) getData("immortal");
	}
	
	public void updateHealthBar() {
		updateHealthBar(0.0);
	}
	
	public void updateHealthBar(double additionalDamage) {
		entity.setCustomName(getDecoratedName()
			 + (isImmortal()
					 ? ChatColor.LIGHT_PURPLE + " [Immortal]"
					 : ChatColor.DARK_GRAY    + " [" + ProgressBarUtil.getHealthBar(getHealth() - additionalDamage, getMaxHealth()) + ChatColor.DARK_GRAY + "]"));
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public String getDecoratedName() {
		return getNPCType().getPrefix() + getNPCType().getNameColor() + getName() + ChatColor.GRAY + " Lv " + getLevel();
	}
	
	public NPCType getNPCType() {
		return NPCType.valueOf((String) getData("npcType"));
	}
	
	public void setNPCType(NPCType npcType) {
		setData("npcType", npcType.toString());
	}
	
	public int getLevel() {
		return (int) getData("level");
	}
	
	public void remove() {
		entity.remove();
		registry.removeFromDatabase(this);
	}
	
	public void phase(Player playerFor) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!p.equals(playerFor)) {
				entityHider.hideEntity(p, entity);
			}
		}
		entityHider.showEntity(playerFor, entity);
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
}
