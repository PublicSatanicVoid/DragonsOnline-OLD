package mc.dragons.dragons.core.gameobject.player;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.floor.Floor;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
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
	private static FloorLoader floorLoader;
	
	public static final double MIN_DISTANCE_TO_UPDATE_STATE = 2.0;
	
	private Player player;
	private Set<Region> cachedRegions;
	private Location cachedLocation;
	
	public static int calculateLevel(int xp) {
		return (int) Math.floor(xp / 1_000_000 + Math.sqrt(xp / 100)) + 1;
	}
	
	public static int calculateMaxHealth(int level) {
		return Math.min(28, 20 + (int) Math.floor(level / 3));
	}
	
	public User(Player player, StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		}
		if(floorLoader == null) {
			floorLoader = (FloorLoader) GameObjectType.FLOOR.<Floor>getLoader();
		}
		this.player = player;
		if(player != null) {
			player.getInventory().clear();
			player.setMaxHealth(calculateMaxHealth(getLevel()));
			if(getData("health") != null) {
				player.setHealth((double) getData("health"));
			}
		}
		@SuppressWarnings("unchecked")
		List<UUID> inventory = (List<UUID>) getData("inventory");
		for(UUID uuid : inventory) {
			Item item = ((ItemLoader) GameObjectType.ITEM.<Item>getLoader()).loadObject(uuid);
			giveItem(item, false, player == null, true);
		}
		
		cachedRegions = new HashSet<>();
	}	
	
	private void updateState() {
		Set<Region> regions = regionLoader.getRegionsByLocationXZ(player.getLocation());
		
		if(cachedLocation != null) {
			if(cachedLocation.getWorld() != player.getLocation().getWorld()) {
				Floor floor = floorLoader.fromWorldName(player.getLocation().getWorld().getName());
				cachedLocation = player.getLocation();
				cachedRegions = regions;
				if(floor == null) {
					sendActionBar(ChatColor.DARK_RED + "- Unofficial World -");
					player.sendMessage(ChatColor.RED + "WARNING: This is an unofficial world and is not associated with a floor.");
				}
				else {
					player.sendMessage(ChatColor.GRAY + "Floor " + floor.getLevelMin() + ": " + floor.getDisplayName());
					sendTitle(ChatColor.DARK_GRAY, "Floor " + floor.getLevelMin(), ChatColor.GRAY, floor.getDisplayName());
				}
				return;
			}
		}
		
		// Find newly left regions
		for(Region region : cachedRegions) {
			if(!regions.contains(region)) {
				//sendActionBar(ChatColor.LIGHT_PURPLE + "Leaving " + region.getFlags().getString("fullname"));
				player.sendMessage(ChatColor.GRAY + "Leaving " + region.getFlags().getString("fullname"));
			}
		}
		
		// Find newly entered regions
		for(Region region : regions) {
			if(!cachedRegions.contains(region)) {
				if(Boolean.parseBoolean(region.getFlags().getString("showtitle"))) {
					sendTitle(ChatColor.DARK_PURPLE, "Welcome!", ChatColor.LIGHT_PURPLE, "To " + region.getFlags().getString("fullname"));
				}
				//sendActionBar(ChatColor.LIGHT_PURPLE + "Entering " + region.getFlags().getString("fullname"));
				player.sendMessage(ChatColor.GRAY + "Entering " + region.getFlags().getString("fullname"));
				if(!region.getFlags().getString("desc").equals("")) {
					player.sendMessage(ChatColor.DARK_GRAY + "   " + ChatColor.ITALIC + region.getFlags().getString("desc"));
				}
				int lvMin = Integer.parseInt(region.getFlags().getString("lvmin"));
				int lvRec = Integer.parseInt(region.getFlags().getString("lvrec"));
				if(getLevel() < lvMin) {
					player.setVelocity(cachedLocation.toVector().subtract(player.getLocation().toVector()).multiply(2.0));
					player.sendMessage(ChatColor.RED + "This region requires level " + lvMin + " to enter");
				}
				else if(getLevel() < lvRec) {
					player.sendMessage(ChatColor.YELLOW + "Caution: The recommended level for this region is " + lvRec);
				}
			}
		}

		cachedLocation = player.getLocation();
		cachedRegions = regions;
	}
	
	public void giveItem(Item item, boolean updateDB, boolean dbOnly, boolean silent) {
		if(!dbOnly) {
			player.getInventory().addItem(item.getItemStack());
		}
		if(updateDB) {
			@SuppressWarnings("unchecked")
			ArrayList<UUID> inventory = (ArrayList<UUID>) getData("inventory");
			inventory.add(item.getUUID());
			storageAccess.update(new Document("inventory", inventory));
		}
		if(!silent) {
			player.sendMessage(ChatColor.GRAY + "Received " + item.getDecoratedName());
		}
	}
	
	public void giveItem(Item item) {
		giveItem(item, true, false, false);
	}
	
	public void takeItem(Item item, boolean updateDB) {
		player.getInventory().remove(item.getItemStack());
		if(updateDB) {
			@SuppressWarnings("unchecked")
			ArrayList<UUID> inventory = (ArrayList<UUID>) getData("inventory");
			inventory.remove(item.getUUID());
			storageAccess.update(new Document("inventory", inventory));
		}
		player.sendMessage(ChatColor.GRAY + "Lost " + item.getDecoratedName());
	}
	
	public void takeItem(Item item) {
		takeItem(item, true);
	}
	
	public void handleJoin() {
		setData("lastJoined", System.currentTimeMillis());
		player.sendMessage(ChatColor.GOLD + "Hello " + player.getName() + " and welcome to Dragons.");
		player.sendMessage(ChatColor.YELLOW + "Your level is " + getData("level") + " [" + getData("xp") + " XP]");
		if(getRank().ordinal() >= Rank.PATRON.ordinal()) {
			Bukkit.broadcastMessage(getRank().getNameColor() + getRank().getRankName() + " " + player.getName() + " joined!");
		}
		updateState();
	}
	
	public void handleMove() {
		boolean update = false;
		if(cachedLocation == null) {
			cachedLocation = player.getLocation();
		}
		else { // avoid NPEs when cachedLocation is null - do not roll up to else if!
			if(player.getLocation().getWorld() != cachedLocation.getWorld()) {
				update = true;
			}
			else { // avoid IAEs when the worlds don't match - do not roll up to else if!
				if(player.getLocation().distanceSquared(cachedLocation) >= MIN_DISTANCE_TO_UPDATE_STATE * MIN_DISTANCE_TO_UPDATE_STATE) {
					update = true;
				}
			}
		}
		if(update) {
			updateState();
		}
	}
	
	public Player p() {
		return player;
	}
	
	public String getName() {
		return (String) getData("username");
	}
	
	public Location getSavedLocation() {
		return StorageUtil.docToLoc((Document) getData("lastLocation"));
	}
	
	public double getSavedHealth() {
		return (double) getData("health"); 
	}
	
	public double getSavedMaxHealth() {
		return (double) getData("maxHealth");
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
	
	public void clearInventory() {
		player.getInventory().clear();
		setData("inventory", new ArrayList<>());
		sendActionBar(ChatColor.DARK_RED + "- All items have been lost! -");
	}
	
	public void setDeathCountdown(int seconds) {
		setData("deathCountdown", seconds);
		setData("deathTime", System.currentTimeMillis());
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * seconds, 10, false, false), true);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * seconds, 10, false, false), true);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * seconds, 10, false, false), true);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * seconds, 0, false, false), true);
	}
  
	public boolean hasDeathCountdown() {
		Long deathTime = (Long) getData("deathTime");
		if(deathTime == null) return false;
		int deathCountdown = (int) getData("deathCountdown");
		long now = System.currentTimeMillis();
		return deathTime + 1000 * deathCountdown > now;
	}
	
	public void respawn() {
		Dragons.getInstance().getBridge().respawnPlayer(player);
	}
	
	public void sendToFloor(String floorName) {
		Floor floor = floorLoader.fromFloorName(floorName);
		player.teleport(floor.getWorld().getSpawnLocation());
	}
	
	public void addXP(int xp) {
		int totalXP = (int) getData("xp") + xp;
		int level = calculateLevel(totalXP);
		if(level > getLevel()) {
			sendTitle(ChatColor.DARK_AQUA, "Level Up!", ChatColor.AQUA, getLevel() + "  >>>  " + level, 10, 10, 10);
			player.setMaxHealth(calculateMaxHealth(level));
		}
		update(new Document("xp", totalXP).append("level", level));
	}
	
	public int getXP() {
		return (int) getData("xp");
	}
	
	public int getLevel() {
		return (int) getData("level");
	}
	
	public ChatColor getLevelColor() {
		int level = getLevel();
		if(level < 10) {
			return ChatColor.GRAY;
		}
		else if(level < 20) {
			return ChatColor.YELLOW;
		}
		else if(level < 30) {
			return ChatColor.GREEN;
		}
		else if(level < 40) {
			return ChatColor.AQUA;
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
		return PermissionLevel.valueOf((String) getData("permissionLevel"));
	}
	
	public void setPermissionLevel(PermissionLevel permissionLevel) {
		setData("permissionLevel", permissionLevel.toString());
	}
	
	public Rank getRank() {
		return Rank.valueOf((String) getData("rank"));
	}
	
	public void setRank(Rank rank) {
		setData("rank", rank.toString());
	}
	
	public Set<Region> getRegions() {
		return cachedRegions;
	}
	
	public Date getFirstJoined() {
		return new Date((long) getData("firstJoined"));
	}
	
	public Date getLastJoined() {
		return new Date((long) getData("lastJoined"));
	}
	
	public Date getLastSeen() {
		return new Date((long) getData("lastSeen"));
	}
	
	public int getSkillLevel(SkillType type) {
		return (int)((Document) getData("skills")).getInteger(type.toString());
	}
	
	public void setSkillLevel(SkillType type, int level) {
		Document skillLevels = (Document) getData("skills");
		skillLevels.append(type.toString(), level);
		update(new Document("skills", skillLevels));
	}
	
	public void updateSkillProgress(SkillType type, double increment) {
		Document skillProgress = (Document) getData("skillProgress");
		double progress = skillProgress.getDouble(type.toString()) + increment;
		int level = getSkillLevel(type);
		skillProgress.append(type.toString(), progress);
		if(progress >= 30 * (level + 1)) {
			setSkillLevel(type, level + 1);
			sendTitle(ChatColor.DARK_GREEN, type.getFriendlyName() + " Increased!", ChatColor.GREEN, level + " >>> " + (level + 1));
		}
		update(new Document("skillProgress", skillProgress));
	}
	
	public double getSkillProgress(SkillType type) {
		return (double)((Document) getData("skillProgress")).getDouble(type.toString());
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
