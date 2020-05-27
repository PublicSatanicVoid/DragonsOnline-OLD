package mc.dragons.dragons.core.gameobject.player;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.region.Region;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;
import mc.dragons.dragons.core.storage.StorageUtil;

/**
 * Represents a player in the RPG.
 * 
 * <p>Like all {@link mc.dragons.dragons.core.gameobject.GameObject}s,
 * users are backed by the database.
 * 
 * <p>In addition to the standard properties of players, users
 * have specific skills which can be leveled up, as well as friends,
 * guilds, parties, quest logs, and expanded inventories.
 * 
 * @author Rick
 *
 */
public class User extends GameObject {

	private static RegionLoader regionLoader;
	
	public static final double MIN_DISTANCE_TO_UPDATE_STATE = 2.0;
	
	private Player player;
	private Set<Region> cachedRegions;
	private Location cachedLocation;
	
	public static int calculateLevel(int xp) {
		return (int) Math.floor(xp / 50000 + Math.sqrt(xp / 50));
	}
	
	public static int calculateMaxHealth(int level) {
		return 20 + (int) Math.floor(level / 3);
	}
	
	public User(Player player, StorageManager storageManager, StorageAccess storageAccess) {
		super(GameObjectType.PLAYER, 
				(UUID)storageAccess.get("_id"), 
				storageManager);
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.getLoader();
		}
		this.player = player;
		player.setMaxHealth(calculateMaxHealth(getLevel()));
		if(getData("health") != null) {
			player.setHealth((double)getData("health"));
		}
		cachedRegions = new HashSet<>();
	}	
	
	private void updateState() {
		
		Set<Region> regions = regionLoader.getRegionsByLocationXZ(player.getLocation());
		
		// Find newly entered regions
		for(Region region : regions) {
			if(!cachedRegions.contains(region)) {
				if(Boolean.parseBoolean(region.getFlags().getString("showtitle"))) {
					sendTitle(ChatColor.DARK_PURPLE, "Welcome!", ChatColor.LIGHT_PURPLE, "To " + region.getFlags().getString("fullname"));
				}
				//sendActionBar(ChatColor.LIGHT_PURPLE + "Entering " + region.getFlags().getString("fullname"));
				player.sendMessage(ChatColor.GRAY + "Entering " + region.getFlags().getString("fullname"));
				int lvMin = Integer.parseInt(region.getFlags().getString("lvmin"));
				int lvRec = Integer.parseInt(region.getFlags().getString("lvrec"));
				if(getLevel() < lvMin) {
					player.setVelocity(player.getLocation().toVector().subtract(cachedLocation.toVector()));
					player.sendMessage(ChatColor.RED + "This region requires level " + lvMin + " to enter");
				}
				else if(getLevel() < lvRec) {
					player.sendMessage(ChatColor.YELLOW + "Warning: The recommended level for this region is " + lvRec);
				}
			}
		}
		
		// Find newly left regions
		for(Region region : cachedRegions) {
			if(!regions.contains(region)) {
				//sendActionBar(ChatColor.LIGHT_PURPLE + "Leaving " + region.getFlags().getString("fullname"));
				player.sendMessage(ChatColor.GRAY + "Leaving " + region.getFlags().getString("fullname"));
			}
		}

		cachedLocation = player.getLocation();
		cachedRegions = regions;
	}
	
	public void handleJoin() {
		setData("lastJoined", System.currentTimeMillis());
		player.sendMessage(ChatColor.GOLD + "Hello " + player.getName() + " and welcome to Dragons.");
		player.sendMessage(ChatColor.YELLOW + "Your level is " + getData("level") + " [" + getData("xp") + " XP]");
	}
	
	public void handleMove() {
		boolean update = false;
		if(cachedLocation == null) {
			cachedLocation = player.getLocation();
		}
		else { // avoid NPEs when cachedLocation is null
			if(player.getLocation().distanceSquared(cachedLocation) >= MIN_DISTANCE_TO_UPDATE_STATE * MIN_DISTANCE_TO_UPDATE_STATE) {
				update = true;
			}
		}
		if(update) {
			updateState();
		}
	}
	
	public Player p() {
		return player;
	}
	
	public void sendActionBar(String message) {
		Dragons.getInstance().getBridge().sendActionBar(player, message);
	}
	
	public void sendTitle(ChatColor titleColor, String title, ChatColor subtitleColor, String subtitle) {
		sendTitle(titleColor, title, subtitleColor, subtitle, 1, 5, 1);
	}
	
	public void sendTitle(ChatColor titleColor, String title, ChatColor subtitleColor, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
		Dragons.getInstance().getBridge().sendTitle(player, titleColor, title, subtitleColor, subtitle, fadeInTime, showTime, fadeOutTime);
	}
	
	public void addXP(int xp) {
		int totalXP = (int)getData("xp") + xp;
		int level = calculateLevel(totalXP);
		if(level > getLevel()) {
			sendTitle(ChatColor.DARK_AQUA, "Level Up!", ChatColor.AQUA, getLevel() + "  >>>  " + level, 10, 10, 10);
			player.setMaxHealth(calculateMaxHealth(level));
		}
		update(new Document("xp", totalXP).append("level", level));
	}
	
	public int getXP() {
		return (int)getData("xp");
	}
	
	public int getLevel() {
		return (int)getData("level");
	}
	
	public ChatColor getLevelColor() {
		int level = getLevel();
		if(level < 10) {
			return ChatColor.GRAY;
		}
		else if(level < 20) {
			return ChatColor.AQUA;
		}
		else if(level < 30) {
			return ChatColor.GREEN;
		}
		else if(level < 40) {
			return ChatColor.YELLOW;
		}
		else if(level < 50) {
			return ChatColor.DARK_AQUA;
		}
		else if(level < 60) {
			return ChatColor.GOLD;
		}
		else if(level < 70) {
			return ChatColor.DARK_GREEN;
		}
		else if(level < 80) {
			return ChatColor.LIGHT_PURPLE;
		}
		else if(level < 90) {
			return ChatColor.DARK_PURPLE;
		}
		else if(level < 100) {
			return ChatColor.RED;
		}
		else {
			return ChatColor.WHITE;
		}
	}
	
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.valueOf((String)getData("permissionLevel"));
	}
	
	public Rank getRank() {
		return Rank.valueOf((String)getData("rank"));
	}
	
	public Set<Region> getRegions() {
		return cachedRegions;
	}
	
	public Date getFirstJoined() {
		return new Date((long)getData("firstJoined"));
	}
	
	public Date getLastJoined() {
		return new Date((long)getData("lastJoined"));
	}
	
	public Date getLastSeen() {
		return new Date((long)getData("lastSeen"));
	}
	
	public int getSkillLevel(SkillType type) {
		return (int)((Document) getData("skills")).getInteger(type.toString());
	}
	
	@Override
	public void autoSave() {
		sendActionBar(ChatColor.GREEN + "Autosaving...");
		Document autoSaveData = new Document("lastLocation", StorageUtil.locToDoc(player.getLocation()))
				.append("lastSeen", System.currentTimeMillis())
				.append("maxHealth", player.getMaxHealth())
				.append("health", player.getHealth());
		update(autoSaveData);
	}

}
