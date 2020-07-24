package mc.dragons.core.gameobject.npc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
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
import mc.dragons.core.util.StringUtil;

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
		HOSTILE(ChatColor.RED, "", false, false, true),
		NEUTRAL(ChatColor.YELLOW, "", false, false, true),
		QUEST(ChatColor.DARK_GREEN, ChatColor.DARK_GREEN + "[NPC] ", true, true, false),
		SHOP(ChatColor.DARK_AQUA, ChatColor.DARK_AQUA + "[NPC] ", true, true, false),
		PERSISTENT(ChatColor.YELLOW, "", true, true, true);
		
		private ChatColor nameColor;
		private String prefix;
		private boolean persistent;
		private boolean immortalByDefault;
		private boolean aiByDefault;
		
		NPCType(ChatColor nameColor, String prefix, boolean persistent, boolean immortalByDefault, boolean aiByDefault) {
			this.nameColor = nameColor;
			this.prefix = prefix;
			this.persistent = persistent;
			this.immortalByDefault = immortalByDefault;
			this.aiByDefault = aiByDefault;
		}
		
		public ChatColor getNameColor() {
			return nameColor;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public boolean isPersistent() {
			return persistent;
		}
		
		public boolean isImmortalByDefault() {
			return immortalByDefault;
		}
		
		public boolean hasAIByDefault() {
			return aiByDefault;
		}
	};

	protected Entity entity;
	protected GameObjectRegistry registry;

	protected static NPCClassLoader npcClassLoader;
	protected static EntityHider entityHider;
	
	public NPC(Entity entity, StorageManager storageManager, StorageAccess storageAccess) {
		//super(GameObjectType.NPC, storageAccess.getIdentifier().getUUID(), storageManager);
		super(storageManager, storageAccess);
		LOGGER.fine("Constructing NPC (" + StringUtil.entityToString(entity) + ", " + storageManager + ", " + storageAccess + ")");
		if(npcClassLoader == null) {
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
		}
		if(entityHider == null) {
			entityHider = new EntityHider(Dragons.getInstance(), Policy.BLACKLIST);
		}

		if(entity instanceof Zombie) {
			((Zombie) entity).setBaby(false); // TODO: make configurable in NPC Class
		}

		if(entity.isInsideVehicle()) {
			entity.getVehicle().eject();
		}
		
		this.entity = entity;
		this.registry = Dragons.getInstance().getGameObjectRegistry();
	
		initializeEntity();
		
		Dragons.getInstance().getBridge().setEntityAI(entity, getNPCClass().hasAI());
		Dragons.getInstance().getBridge().setEntityInvulnerable(entity, isImmortal());

		getNPCClass().getAddons().forEach(addon -> addon.initialize(this));
	}
	
	public void initializeEntity() {
		entity.setCustomName(getDecoratedName());
		entity.setCustomNameVisible(true);
		entity.setMetadata("handle", new FixedMetadataValue(Dragons.getInstance(), this));
	}
	
	public NPCClass getNPCClass() {
		String className = (String) getData("className");
		if(className == null) return null;
		return npcClassLoader.getNPCClassByClassName(className);
	}

	public void setMaxHealth(double maxHealth) {
		if(entity instanceof Attributable) {
			Attributable attributable = (Attributable) entity;
			attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
			setData("maxHealth", maxHealth);
		}
	}
	
	public double getMaxHealth() {
		if(entity instanceof Attributable) {
			Attributable attributable = (Attributable) entity;
			return attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
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
		LOGGER.finer("Phasing NPC " + getIdentifier() + " for " + playerFor.getName());
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!p.equals(playerFor)) {
				entityHider.hideEntity(p, entity);
			}
		}
		entityHider.showEntity(playerFor, entity);
	}
	
	public void setEntity(Entity entity) {
		this.entity.removeMetadata("handle", Dragons.getInstance());
		LOGGER.finer("Replacing entity backing NPC " + getIdentifier() + ": " + StringUtil.entityToString(this.entity) + " -> " + StringUtil.entityToString(entity));
		this.entity = entity;
		this.entity.setMetadata("handle", new FixedMetadataValue(Dragons.getInstance(), this));
	}
	
	public Entity getEntity() {
		return entity;
	}
	
}
