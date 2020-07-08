package mc.dragons.core.gameobject.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.floor.Floor;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.quest.QuestStep;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.storage.StorageUtil;
import mc.dragons.core.storage.impl.SystemProfile;
import mc.dragons.core.storage.impl.SystemProfileLoader;

/**
 * Represents a player in the RPG.
 * 
 * <p>Like all {@link mc.dragons.core.gameobject.GameObject}s,
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
	
	public enum PunishmentType {
		BAN("ban"),
		MUTE("mute");
		
		private String dataHeader;
		
		PunishmentType(String dataHeader) {
			this.dataHeader = dataHeader;
		}
		
		public String getDataHeader() {
			return dataHeader;
		}
		
	}
	
	public class PunishmentData {
		private PunishmentType type;
		private String reason;
		private Date expiry;
		public boolean permanent;
		
		public PunishmentData(PunishmentType type, String reason, Date expiry, boolean permanent) {
			this.type = type;
			this.reason = reason;
			this.expiry = expiry;
			this.permanent = permanent;
		}
		
		public PunishmentType getType() { return type; }
		public String getReason() { return reason; }
		public Date getExpiry() { return expiry; }
		public boolean isPermanent() { return permanent; }
	}

	private static RegionLoader regionLoader;
	private static FloorLoader floorLoader;
	private static QuestLoader questLoader;
	private static UserLoader userLoader;
	
	public static final double MIN_DISTANCE_TO_UPDATE_STATE = 2.0;
	
	private Player player;
	private Set<Region> cachedRegions;
	private Location cachedLocation;
	private PermissionLevel activePermissionLevel;
	private SystemProfile profile;
	private Map<Quest, QuestStep> questProgress;
	private boolean isDebugging;
	
	public static int calculateLevel(int xp) {
		return (int) Math.floor(xp / 1_000_000 + Math.sqrt(xp / 100)) + 1;
	}
	
	public static int calculateSkillLevel(double progress) {
		return (int) Math.floor(Math.sqrt(progress / 15));
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
		if(questLoader == null) {
			questLoader = (QuestLoader) GameObjectType.QUEST.<Quest>getLoader();
		}
		if(userLoader == null) {
			userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		}
		
		initialize(player);
	}	
	
	public User initialize(Player player) {
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
		
		questProgress = new HashMap<>();
		Document questProgressDoc = (Document) getData("quests");
		for(Entry<String, Object> entry : questProgressDoc.entrySet()) {
			Quest quest = questLoader.getQuestByName(entry.getKey());
			if(quest == null) continue; // Quest was deleted?
			questProgress.put(quest, quest.getSteps().get((Integer) entry.getValue()));
		}
		
		cachedRegions = new HashSet<>();
		activePermissionLevel = PermissionLevel.USER;
		return this;
	}
	
	public void setDebugging(boolean debugging) {
		isDebugging = debugging;
	}
	
	public boolean isDebugging() {
		return isDebugging;
	}
	
	public void debug(String message) {
		if(isDebugging()) {
			player.sendMessage("[DEBUG] " + message);
		}
	}
	
	public void updateQuests(Event event) {
		debug("Updating quests...");
		for(Entry<Quest, QuestStep> questStep : questProgress.entrySet()) {
			debug("- Step " + questStep.getValue().getStepName() + " of " + questStep.getKey().getName());
			if(questStep.getValue().getStepName().equalsIgnoreCase("Complete")) continue; // Nothing to check if they're already done
			debug("  - Trigger: " + questStep.getValue().getTrigger().getTriggerType());
			if(questStep.getValue().getTrigger().test(this, event)) {
				Quest quest = questStep.getKey();
				debug("   - Triggered");
				if(questStep.getValue().executeActions(this)) {
					debug("      - Normal progression to next step");
					int nextIndex = quest.getSteps().indexOf(questStep.getValue()) + 1;
					if(nextIndex != quest.getSteps().size()) {
						QuestStep nextStep = quest.getSteps().get(nextIndex);
						updateQuestProgress(quest, nextStep, true);
					}
				}
			}
		}
	}
	
	public void updateState() {
		updateState(true, true);
	}
	
	public void updateState(boolean applyQuestTriggers, boolean notify) {
		Set<Region> regions = regionLoader.getRegionsByLocationXZ(player.getLocation());
		
		if(cachedLocation != null) {
			if(cachedLocation.getWorld() != player.getLocation().getWorld()) {
				Floor floor = floorLoader.fromWorldName(player.getLocation().getWorld().getName());
				cachedLocation = player.getLocation();
				cachedRegions = regions;
				if(notify) {
					if(floor == null) {
						sendActionBar(ChatColor.DARK_RED + "- Unofficial World -");
						player.sendMessage(ChatColor.RED + "WARNING: This is an unofficial world and is not associated with a floor.");
					}
					else {
						player.sendMessage(ChatColor.GRAY + "Floor " + floor.getLevelMin() + ": " + floor.getDisplayName());
						sendTitle(ChatColor.DARK_GRAY, "Floor " + floor.getLevelMin(), ChatColor.GRAY, floor.getDisplayName());
					}
				}
				return;
			}
		}
		
		// Find newly left regions
		for(Region region : cachedRegions) {
			if(!regions.contains(region)) {
				//sendActionBar(ChatColor.LIGHT_PURPLE + "Leaving " + region.getFlags().getString("fullname"));
				if(notify) {
					player.sendMessage(ChatColor.GRAY + "Leaving " + region.getFlags().getString("fullname"));
				}
			}
		}
		
		// Find newly entered regions
		for(Region region : regions) {
			if(!cachedRegions.contains(region)) {
				if(notify) {
					if(Boolean.parseBoolean(region.getFlags().getString("showtitle"))) {
						sendTitle(ChatColor.DARK_PURPLE, "Welcome!", ChatColor.LIGHT_PURPLE, "To " + region.getFlags().getString("fullname"));
					}
					//sendActionBar(ChatColor.LIGHT_PURPLE + "Entering " + region.getFlags().getString("fullname"));
					player.sendMessage(ChatColor.GRAY + "Entering " + region.getFlags().getString("fullname"));
					if(!region.getFlags().getString("desc").equals("")) {
						player.sendMessage(ChatColor.DARK_GRAY + "   " + ChatColor.ITALIC + region.getFlags().getString("desc"));
					}
				}
				int lvMin = Integer.parseInt(region.getFlags().getString("lvmin"));
				int lvRec = Integer.parseInt(region.getFlags().getString("lvrec"));
				if(getLevel() < lvMin) {
					player.setVelocity(cachedLocation.toVector().subtract(player.getLocation().toVector()).multiply(2.0));
					if(notify) {
						player.sendMessage(ChatColor.RED + "This region requires level " + lvMin + " to enter");
					}
				}
				else if(getLevel() < lvRec && notify) {
					player.sendMessage(ChatColor.YELLOW + "Caution: The recommended level for this region is " + lvRec);
				}
			}
		}

		if(applyQuestTriggers) {
			updateQuests(null);
		}
		
		cachedLocation = player.getLocation();
		cachedRegions = regions;
		
		updateEffectiveWalkSpeed();
	}
	
	public Map<Quest, QuestStep> getQuestProgress() {
		return questProgress;
	}
	
	public void updateQuestProgress(Quest quest, QuestStep questStep, boolean notify) {	
		Document updatedQuestProgress = (Document) getData("quests");
		if(questStep == null) {
			questProgress.remove(quest);
			updatedQuestProgress.remove(quest.getName());
			storageAccess.update(new Document("quests", updatedQuestProgress));
			return;
		}
		debug("==UPDATING QUEST PROGRESS: " + quest.getName() + " step " + questStep.getStepName());
		questProgress.put(quest, questStep);
		updatedQuestProgress.append(quest.getName(), quest.getSteps().indexOf(questStep));
		storageAccess.update(new Document("quests", updatedQuestProgress));
		if(notify) {
			if(questStep.getStepName().equalsIgnoreCase("Complete")) {
				player.sendMessage(ChatColor.GRAY + "Completed quest " + quest.getQuestName());
			}
			else {
				player.sendMessage(ChatColor.GRAY + "New Objective: " + questStep.getStepName());
			}
		}
	}
	
	public void updateQuestProgress(Quest quest, QuestStep questStep) {
		updateQuestProgress(quest, questStep, true);
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
	
	public void takeItem(Item item, boolean updateDB, boolean notify) {
		player.getInventory().remove(item.getItemStack());
		if(updateDB) {
			@SuppressWarnings("unchecked")
			ArrayList<UUID> inventory = (ArrayList<UUID>) getData("inventory");
			inventory.remove(item.getUUID());
			storageAccess.update(new Document("inventory", inventory));
		}
		if(notify) {
			player.sendMessage(ChatColor.GRAY + "Lost " + item.getDecoratedName());
		}
	}
	
	public void takeItem(Item item) {
		takeItem(item, true, true);
	}
	
	public void handleJoin() {
		setData("lastJoined", System.currentTimeMillis());
		player.sendMessage(ChatColor.GOLD + "Hello " + player.getName() + " and welcome to Dragons.");
		player.sendMessage(ChatColor.YELLOW + "Your level is " + getLevel() + " [" + getXP() + " XP]");
		player.sendMessage(ChatColor.YELLOW + "You have " + getGold() + " gold.");
		if(isVanished()) {
			player.sendMessage(ChatColor.DARK_GREEN + "You are currently vanished.");
		}
		else {
			if(getRank().ordinal() >= Rank.PATRON.ordinal()) {
				Bukkit.broadcastMessage(getRank().getNameColor() + getRank().getRankName() + " " + player.getName() + " joined!");
			}
			else {
				Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " joined!");
			}
		}
		
		updateState();
		updateVanishState();
		updateVanishStatesOnSelf();
	}
	
	public void handleQuit() {
		autoSave();
		if(!isVanished()) {
			Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " left!");
		}
		if(profile != null) {
			SystemProfileLoader.logoutProfile(profile.getProfileName());
			setActivePermissionLevel(PermissionLevel.USER);
			setSystemProfile(null);
		}
		player.getInventory().clear();
		// TODO remove armor as well
		userLoader.removeStalePlayer(player);
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
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
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
	
	public double getGold() {
		return (double) getData("gold");
	}
	
	public void setGold(double gold) {
		setData("gold", gold);
		player.sendMessage(ChatColor.GRAY + "Your gold balance is now " + ChatColor.GOLD + gold);
	}
	
	public void giveGold(double gold) {
		setData("gold", getGold() + gold);
		player.sendMessage(ChatColor.GRAY + "Received " + ChatColor.GOLD + gold + " Gold");
	}
	
	public void takeGold(double gold) {
		setData("gold", getGold() - gold);
		player.sendMessage(ChatColor.GRAY + "Lost " + ChatColor.GOLD + gold + " Gold");
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
	
	public double getEffectiveWalkSpeed() {
		double speed = Dragons.getInstance().getServerOptions().getDefaultWalkSpeed();

		for(ItemStack itemStack : player.getInventory().getArmorContents()) {
			if(itemStack == null) continue;
			Item item = ItemLoader.fromBukkit(itemStack);
			if(item == null) continue;
			speed += item.getSpeedBoost();
		}
		
		ItemStack held = player.getItemInHand();
		Item item = ItemLoader.fromBukkit(held);
		if(item != null) {
			speed += item.getSpeedBoost();
		}
		
		return Math.min(1.0, Math.max(0.05, speed));
	}
	
	public void updateEffectiveWalkSpeed() {
		player.setWalkSpeed((float) getEffectiveWalkSpeed()); 
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
		new BukkitRunnable() {
			int counter = seconds;
			@Override
			public void run() {
				if(hasDeathCountdown()) {
					sendActionBar(ChatColor.DARK_RED + "Respawning in " + counter + "s");
					counter--;
				}
				else {
					sendActionBar(ChatColor.YELLOW + "Respawning...");
					this.cancel();
				}
			}
		}.runTaskTimer(Dragons.getInstance(), 0L, 20L);
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
	
	public void sendToFloor(String floorName, boolean overrideLevelRequirement) {
		Floor floor = floorLoader.fromFloorName(floorName);
		if(!overrideLevelRequirement && getLevel() < floor.getLevelMin()) return;
		player.teleport(floor.getWorld().getSpawnLocation());
	}
	
	public void sendToFloor(String floorName) {
		sendToFloor(floorName, false);
	}
	
	public void addXP(int xp) {
		setXP(getXP() + xp);
	}
	
	public void setXP(int xp) {
		int level = calculateLevel(xp);
		if(level > getLevel()) {
			sendTitle(ChatColor.DARK_AQUA, "Level Up!", ChatColor.AQUA, getLevel() + "  >>>  " + level, 10, 10, 10);
			player.setMaxHealth(calculateMaxHealth(level));
		}
		update(new Document("xp", xp).append("level", level));
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
	
	public static void updateVanishStateBetween(User userOf, User userFor) {
		if(userOf.isVanished() && userFor.getActivePermissionLevel().ordinal() < PermissionLevel.MOD.ordinal()) {
			userFor.player.hidePlayer(userOf.player);
		}
		else if(!userFor.player.canSee(userOf.player)) {
			userFor.player.showPlayer(userOf.player);
		}
	}
	
	public void updateVanishStatesOnSelf() {
		for(Player test : Bukkit.getOnlinePlayers()) {
			User user = UserLoader.fromPlayer(test);
			updateVanishStateBetween(user, this);
		}
	}
	
	public void updateVanishState() {
		player.spigot().setCollidesWithEntities(!isVanished());
		player.setAllowFlight(isVanished());
		for(Player test : Bukkit.getOnlinePlayers()) {
			updateVanishStateBetween(this, UserLoader.fromPlayer(test));
		}
	}
	
	public void setVanished(boolean vanished) {
		setData("vanished", vanished);
		updateVanishState();
	}
	
	public boolean isVanished() {
		return (boolean) getData("vanished");
	}
	
	public void setGodMode(boolean enabled) {
		setData("godMode", enabled);
	}
	
	public boolean isGodMode() {
		return (boolean) getData("godMode");
	}
	
	public void setSystemProfile(SystemProfile profile) {
		this.profile = profile;
	}
	
	public SystemProfile getSystemProfile() {
		return profile;
	}
	
	public PermissionLevel getActivePermissionLevel() {
		return activePermissionLevel;
	}
	
	public boolean setActivePermissionLevel(PermissionLevel permissionLevel) {
		if(permissionLevel.ordinal() > getSystemProfile().getMaxPermissionLevel().ordinal()) {
			return false;
		}
		activePermissionLevel = permissionLevel;
		player.addAttachment(Dragons.getInstance(), "worldedit.*", permissionLevel.ordinal() >= PermissionLevel.BUILDER.ordinal());
		player.addAttachment(Dragons.getInstance(), "minecraft.command.teleport", permissionLevel.ordinal() >= PermissionLevel.MOD.ordinal());
		sendActionBar(ChatColor.GRAY + "Active permission level changed to " + permissionLevel.toString());
		updateVanishStatesOnSelf();
		return true;
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
	
	public void incrementSkillProgress(SkillType type, double increment) {
		setSkillProgress(type, getSkillProgress(type) + increment);
	}
	
	public void setSkillProgress(SkillType type, double progress) {
		Document skillProgress = (Document) getData("skillProgress");
		skillProgress.append(type.toString(), progress);
		int currentLevel = getSkillLevel(type);
		int level = calculateSkillLevel(progress);
		if(level != currentLevel) {
			setSkillLevel(type, level);
			sendTitle(ChatColor.DARK_GREEN, type.getFriendlyName() + (level > currentLevel ? " Increased!" : " Changed"), ChatColor.GREEN, currentLevel + " >>> " + level);
		}
		update(new Document("skillProgress", skillProgress));
	}
	
	public double getSkillProgress(SkillType type) {
		return (double)((Document) getData("skillProgress")).getDouble(type.toString());
	}
	

	public List<PunishmentData> getPunishmentHistory() {
		List<PunishmentData> history = new ArrayList<>();
	
		@SuppressWarnings("unchecked")
		List<Document> results = (List<Document>) getData("punishmentHistory");
		for(Document entry : results) {
			Date expiry = new Date(1000 * (entry.getLong("banDate") + entry.getLong("duration")));
			history.add(new PunishmentData(PunishmentType.valueOf(entry.getString("type")), entry.getString("reason"), expiry, entry.getLong("duration") == -1));
		}
		
		return history;
	}
	
	public void punish(PunishmentType punishmentType, String reason) {
		punish(punishmentType, reason, -1L);
	}
	
	public void punish(PunishmentType punishmentType, String reason, long durationSeconds) {
		long now = Instant.now().getEpochSecond();
		Document punishment = new Document("type", punishmentType.toString())
				.append("reason", reason)
				.append("duration", durationSeconds)
				.append("banDate", now);
		setData(punishmentType.getDataHeader(), punishment);
	
		@SuppressWarnings("unchecked")
		List<Document> punishmentHistory = (List<Document>) getData("punishmentHistory");
		punishmentHistory.add(punishment);
		setData("punishmentHistory", punishmentHistory);
		
		String expiry = durationSeconds == -1 ? "Never" : new Date(1000 * (now + durationSeconds)).toString();
		if(player != null) {
			if(punishmentType == PunishmentType.BAN) {
				player.kickPlayer(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You have been banned.\n\n"
						+ (reason.equals("") ? "" : ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + ChatColor.WHITE + "\n")
						+ ChatColor.GRAY + "Expires: " + ChatColor.WHITE + expiry);
			}
			else {
				player.sendMessage("");
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You have been muted.");
				if(!reason.equals("")) {
					player.sendMessage(ChatColor.RED + "Reason: " + reason);
				}
				player.sendMessage(ChatColor.RED + "Expires: " + expiry);
				player.sendMessage("");
			}
		}
	}
	
	
	public void unpunish(PunishmentType punishmentType) {
		setData(punishmentType.getDataHeader(), null);
		if(player != null) {
			if(punishmentType == PunishmentType.MUTE) {
				player.sendMessage("");
				player.sendMessage(ChatColor.DARK_GREEN + "Your mute has been revoked.");
				player.sendMessage("");
			}
		}
	}
	
	
	
	public PunishmentData getActivePunishmentData(PunishmentType punishmentType) {
		Document banData = (Document) getData(punishmentType.getDataHeader());
		if(banData == null) {
			return null;
		}
		PunishmentType type = PunishmentType.valueOf(banData.getString("type"));
		String reason = banData.getString("reason");
		long duration = banData.getLong("duration");
		long banDate = banData.getLong("banDate");
		long now = Instant.now().getEpochSecond();
		Date expiry = new Date(1000 * (banDate + duration));
		if(duration == -1) {
			return new PunishmentData(type, reason, expiry, true);
		}
		if(now > banDate + duration) {
			return null;
		}
		return new PunishmentData(type, reason, expiry, false);
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
