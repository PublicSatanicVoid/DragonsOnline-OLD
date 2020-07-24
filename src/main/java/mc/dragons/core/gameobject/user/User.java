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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
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
import mc.dragons.core.storage.impl.SystemProfile.SystemProfileFlags;
import mc.dragons.core.storage.impl.SystemProfile.SystemProfileFlags.SystemProfileFlag;
import mc.dragons.core.util.MathUtil;
import mc.dragons.core.util.PermissionUtil;
import mc.dragons.core.util.StringUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Represents a player in the RPG.
 * 
 * <p>Users are fully backed by the persistent database.
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
	
	public enum ChatChannel {
		ALVADOR("A", "Global chat for all of Alvador"),
		LOCAL("L", "Local chat for your current floor"),
		GUILD("G", "Channel for your guild only"),
		PARTY("P", "Channel for your party only"),
		TRADE("T", "Global chat for trade discussion"),
		HELP("H", "Global chat to ask for help"),
		STAFF("S", "Staff-only channel");
		
		private String abbreviation;
		private String description;
		
		ChatChannel(String abbreviation, String description) {
			this.abbreviation = abbreviation;
			this.description = description;
		}
		
		public String getAbbreviation() {
			return abbreviation;
		}
		
		public String getDescription() {
			return description;
		}
		
		public TextComponent getPrefix() {
			return format(ChatColor.DARK_GREEN + "[" + getAbbreviation() + "]");
		}
		
		public TextComponent format(String str) {
			Long listening = UserLoader.allUsers().stream().filter(u -> u.getActiveChatChannels().contains(this)).collect(Collectors.counting());
			TextComponent component = new TextComponent(str);
			component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + "Channel: " + ChatColor.RESET + this.toString() + "\n")
					.append(ChatColor.YELLOW + "Listening: " + ChatColor.RESET + listening + "\n")
					.append(ChatColor.ITALIC + getDescription() + "\n")
					.append(ChatColor.GRAY + "Do " + ChatColor.RESET + "/channel " + ChatColor.GRAY + "to manage channels").create()));
			return component;
		}
		
		public TextComponent format() {
			return format(this.toString());
		}
		
		public boolean canHear(User to, User from) {
			switch(this) {
			case ALVADOR:
				return to.getActiveChatChannels().contains(ChatChannel.ALVADOR);
			case LOCAL:
				return to.getPlayer().getWorld().equals(from.getPlayer().getWorld());
			case GUILD:
				return false; // TODO: implement guild stuff
			case PARTY:
				return false; // TODO: implement party stuff
			case TRADE:
				return to.getActiveChatChannels().contains(ChatChannel.TRADE);
			case STAFF:
				return to.getActiveChatChannels().contains(ChatChannel.STAFF) && PermissionUtil.verifyActivePermissionLevel(to, PermissionLevel.BUILDER, false);
			case HELP:
				return to.getActiveChatChannels().contains(ChatChannel.HELP);
			default:
				return false;
			}
		}
		
		public static ChatChannel parse(String str) {
			for(ChatChannel ch : ChatChannel.values()) {
				if(str.equalsIgnoreCase(ch.toString()) || str.equalsIgnoreCase(ch.getAbbreviation())) return ch;
			}
			return null;
		}
	}

	private static RegionLoader regionLoader;
	private static FloorLoader floorLoader;
	private static QuestLoader questLoader;
	private static ItemLoader itemLoader;
	private static UserLoader userLoader;
	
	public static final double MIN_DISTANCE_TO_UPDATE_STATE = 2.0;
	
	private Player player;
	private Set<Region> cachedRegions;
	private Location cachedLocation;
	private PermissionLevel activePermissionLevel;
	private SystemProfile profile;
	private Map<Quest, QuestStep> questProgress;
	private Map<Quest, Integer> questActionIndices;
	private Map<Quest, Boolean> questPauseStates;
	private List<CommandSender> currentlyDebugging;
	private List<String> currentDialogueBatch;
	private String currentDialogueSpeaker;
	private int currentDialogueIndex;
	private long whenBeganDialogue;
	private List<Consumer<User>> currentDialogueCompletionHandlers;
	private boolean isOverridingWalkSpeed;
	private CommandSender lastReceivedMessageFrom;
	private boolean chatSpy;
	
	
	
	//
	// Static Utilities
	//
	
	public static int calculateLevel(int xp) {
		return (int) Math.floor(xp / 1_000_000 + Math.sqrt(xp / 100)) + 1;
	}
	
	public static int calculateMaxXP(int level) { // floored inverse function of non-floored calculateLevel
		return (int) Math.floor(1_000_000 * Math.pow(Math.sqrt(level + 2499) - 50, 2));
	}
	
	public static int calculateSkillLevel(double progress) {
		return (int) Math.floor(Math.sqrt(progress / 15));
	}
	
	public static int calculateMaxHealth(int level) {
		return Math.min(28, 20 + (int) Math.floor(level / 3));
	}
	
	
	
	//
	// Construction
	//
	
	
	public User(Player player, StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		LOGGER.fine("Constructing user (" + player + ", " + storageManager + ", " + storageAccess + ")");
		
		currentlyDebugging = new ArrayList<>();
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		}
		if(floorLoader == null) {
			floorLoader = (FloorLoader) GameObjectType.FLOOR.<Floor>getLoader();
		}
		if(questLoader == null) {
			questLoader = (QuestLoader) GameObjectType.QUEST.<Quest>getLoader();
		}
		if(itemLoader == null) {
			itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
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
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(calculateMaxHealth(getLevel()));
			if(getData("health") != null) {
				player.setHealth((double) getData("health"));
			}		
			Document inventory = (Document) getData("inventory");
			List<String> brokenItems = new ArrayList<>();
			for(Entry<String, Object> entry : inventory.entrySet()) {
				String[] labels = entry.getKey().split(Pattern.quote("-"));
				String part = labels[0];
				int slot = Integer.valueOf(labels[1]);
				Item item = itemLoader.loadObject((UUID) entry.getValue());
				if(item == null) {
					brokenItems.add(entry.getValue().toString());
					continue;
				}
				ItemStack itemStack = item.getItemStack();
				if(part.equals("I")) {
					player.getInventory().setItem(slot, itemStack);
				}
				else if(part.equals("Helmet")) {
					player.getInventory().setHelmet(itemStack);
				}
				else if(part.equals("Chestplate")) {
					player.getInventory().setChestplate(itemStack);
				}
				else if(part.equals("Leggings")) {
					player.getInventory().setLeggings(itemStack);
				}
				else if(part.equals("Boots")) {
					player.getInventory().setBoots(itemStack);
				}
			}
			if(brokenItems.size() > 0) {
				player.sendMessage(ChatColor.RED + "" + brokenItems.size() + " items in your saved inventory could not be loaded:");
				brokenItems.forEach(uuid -> player.sendMessage(ChatColor.RED + " - " + uuid));
			}
		}
		
		questProgress = new HashMap<>();
		questActionIndices = new HashMap<>();
		questPauseStates = new HashMap<>();
		Document questProgressDoc = (Document) getData("quests");
		for(Entry<String, Object> entry : questProgressDoc.entrySet()) {
			Quest quest = questLoader.getQuestByName(entry.getKey());
			if(quest == null) continue; // Quest was deleted?
			questProgress.put(quest, quest.getSteps().get((Integer) entry.getValue()));
			questActionIndices.put(quest, 0);
			questPauseStates.put(quest, false);
		}
		
		cachedRegions = new HashSet<>();
		activePermissionLevel = PermissionLevel.USER;
		
		LOGGER.fine("Finished initializing user " + this);
		
		return this;
	}
	
	
	
	
	//
	// Debugging Utilities
	//
	
	
	public void addDebugTarget(CommandSender debugger) {
		currentlyDebugging.add(debugger);
	}
	
	public void removeDebugTarget(CommandSender debugger) {
		currentlyDebugging.remove(currentlyDebugging.indexOf(debugger));
	}
	
	public void debug(String message) {
		for(CommandSender debugger : currentlyDebugging) {
			debugger.sendMessage("[DEBUG:" + getName() + "] " + message);
		}
	}
	
	
	
	
	//
	// User State
	//

	
	public void updateState() {
		updateState(true, true);
	}
	
	public void updateState(boolean applyQuestTriggers, boolean notify) {
		LOGGER.finest("Update user state: " + getName() + " (applyQuestTriggers=" + applyQuestTriggers + ", notify=" + notify + ")");
		Set<Region> regions = regionLoader.getRegionsByLocation(player.getLocation());
		
		if(cachedLocation != null) {
			if(cachedLocation.getWorld() != player.getLocation().getWorld()) {
				Floor floor = FloorLoader.fromWorldName(player.getLocation().getWorld().getName());
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
				if(Boolean.valueOf(region.getFlags().getString("hidden"))) continue;
				//sendActionBar(ChatColor.LIGHT_PURPLE + "Leaving " + region.getFlags().getString("fullname"));
				if(notify) {
					player.sendMessage(ChatColor.GRAY + "Leaving " + region.getFlags().getString("fullname"));
				}
			}
		}
		// Find newly entered regions
		for(Region region : regions) {
			if(!cachedRegions.contains(region)) {	
				int lvMin = Integer.parseInt(region.getFlags().getString("lvmin"));			
				if(getLevel() < lvMin) {
					player.setVelocity(cachedLocation.toVector().subtract(player.getLocation().toVector()).multiply(2.0));
					if(notify) {
						player.sendMessage(ChatColor.RED + "This region requires level " + lvMin + " to enter");
					}
				}
				
				if(Boolean.valueOf(region.getFlags().getString("hidden"))) continue;
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
				
				int lvRec = Integer.parseInt(region.getFlags().getString("lvrec"));
				if(getLevel() < lvRec && notify) {
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
	
	
	
	
	//
	// Quest & Dialogue Management
	//
	
	
	public void setDialogueBatch(Quest quest, String speaker, List<String> dialogue) {
		currentDialogueSpeaker = speaker;
		currentDialogueBatch = dialogue;
		currentDialogueIndex = 0;
		whenBeganDialogue = System.currentTimeMillis();
		currentDialogueCompletionHandlers = new CopyOnWriteArrayList<>();
	}
	
	public boolean hasActiveDialogue() {
		return currentDialogueBatch != null;
	}
	
	public long getWhenBeganDialogue() {
		return whenBeganDialogue;
	}
	
	public void onDialogueComplete(Consumer<User> handler) {
		currentDialogueCompletionHandlers.add(handler);
	}
	
	public void resetDialogueAndHandleCompletion() {
		if(currentDialogueBatch == null) return;
		if(currentDialogueIndex >= currentDialogueBatch.size()) {
			debug("Handling dialogue completion...");
			currentDialogueSpeaker = null;
			currentDialogueBatch = null;
			currentDialogueIndex = 0;
			for(Consumer<User> handler : currentDialogueCompletionHandlers) {
				handler.accept(this);
			}
		}
	}
	
	public void fastForwardDialogue() {
		while(hasActiveDialogue()) {
			nextDialogue();
		}
	}
	
	/**
	 * 
	 * @return Whether there is more dialogue
	 */
	public boolean nextDialogue() {
		if(!hasActiveDialogue()) {
			return false;
		}
		debug("nextDialogue");
		debug(" - idx=" + currentDialogueIndex);
		TextComponent message = new TextComponent(TextComponent.fromLegacyText(
				ChatColor.GRAY + "[" + (currentDialogueIndex + 1) + "/" + currentDialogueBatch.size() + "] " 
						+ ChatColor.DARK_GREEN + currentDialogueSpeaker + ": "
						+ ChatColor.GREEN + currentDialogueBatch.get(currentDialogueIndex++).replaceAll(Pattern.quote("%PLAYER%"), getName())));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fastforwarddialogue"));
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + "Click to fast-forward through the dialogue").create()));
		player.spigot().sendMessage(message);
		if(currentDialogueIndex >= currentDialogueBatch.size()) {
			resetDialogueAndHandleCompletion();
			return false;
		}
		return true;
	}
	
	public void setQuestPaused(Quest quest, boolean paused) {
		questPauseStates.put(quest, paused);
	}
	
	public boolean isQuestPaused(Quest quest) {
		return questPauseStates.getOrDefault(quest, false);
	}
	
	public void updateQuests(Event event) {
		debug("Updating quests...");
		if(currentDialogueBatch != null) {
			if(currentDialogueIndex < currentDialogueBatch.size()) {
				debug("- Cancelled quest update because of active dialogue");
				return;
			}
		}
		for(Entry<Quest, QuestStep> questStep : questProgress.entrySet()) {
			debug("- Step " + questStep.getValue().getStepName() + " of " + questStep.getKey().getName());
			if(questStep.getValue().getStepName().equalsIgnoreCase("Complete")) continue; // Nothing to check if they're already done
			if(isQuestPaused(questStep.getKey())) continue; // Quest paused for now
			debug("  - Trigger: " + questStep.getValue().getTrigger().getTriggerType());

			if(questStep.getValue().getTrigger().test(this, event)) {
				Quest quest = questStep.getKey();
				debug("   - Triggered (starting @ action #" + getQuestActionIndex(quest) + ")");
				if(questStep.getValue().executeActions(this, getQuestActionIndex(quest))) {
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
		questActionIndices.put(quest, 0);
		updatedQuestProgress.append(quest.getName(), quest.getSteps().indexOf(questStep));
		storageAccess.update(new Document("quests", updatedQuestProgress));
		if(notify) {
			if(questStep.getStepName().equals("Complete")) {
				player.sendMessage(ChatColor.GRAY + "Completed quest " + quest.getQuestName());
			}
			else {
				player.sendMessage(ChatColor.GRAY + "New Objective: " + questStep.getStepName());
			}
		}
	}
	
	public void updateQuestAction(Quest quest, int actionIndex) {
		questActionIndices.put(quest, actionIndex);
	}
	
	public int getQuestActionIndex(Quest quest) {
		return questActionIndices.getOrDefault(quest, 0);
	}
	
	public void updateQuestProgress(Quest quest, QuestStep questStep) {
		updateQuestProgress(quest, questStep, true);
	}
	
	
	//
	// Chat Utilities
	//
	
	
	@SuppressWarnings("unchecked")
	public List<ChatChannel> getActiveChatChannels() {
		return ((List<String>) getData("chatChannels")).stream().map(ch -> ChatChannel.valueOf(ch)).collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	public void addActiveChatChannel(ChatChannel channel) {
		List<String> channels = (List<String>) getData("chatChannels");
		channels.add(channel.toString());
		setData("chatChannels", channels);
	}	
	
	@SuppressWarnings("unchecked")
	public void removeActiveChatChannel(ChatChannel channel) {
		List<String> channels = (List<String>) getData("chatChannels");
		channels.remove(channel.toString());
		setData("chatChannels", channels);
	}
	
	public ChatChannel getSpeakingChannel() {
		return ChatChannel.valueOf((String) getData("speakingChannel"));
	}
	
	public void setSpeakingChannel(ChatChannel channel) {
		setData("speakingChannel", channel.toString());
	}
	
	public void sendMessage(ChatChannel channel, String message) {
		sendMessage(channel, TextComponent.fromLegacyText(message));
	}
	
	public void sendMessage(ChatChannel channel, Location source, String message) {
		sendMessage(channel, source, TextComponent.fromLegacyText(message));
	}
	
	public void sendMessage(ChatChannel channel, Location source, BaseComponent... message) {
		if(channel == ChatChannel.LOCAL && !FloorLoader.fromWorld(source.getWorld()).equals(FloorLoader.fromWorld(player.getWorld()))) return; // For now, LOCAL = Floor
		sendMessage(channel, message);
	}
	
	public void sendMessage(ChatChannel channel, BaseComponent... message) {
		if(getActiveChatChannels().contains(channel)) {
			player.spigot().sendMessage(new ComponentBuilder(channel.getPrefix()).append(" ").append(message).create());
		}
	}

	public void chat(String message) {
		PunishmentData muteData = getActivePunishmentData(PunishmentType.MUTE);
		if(muteData != null) {
			getPlayer().sendMessage(ChatColor.RED + "You are muted!" + (muteData.getReason().equals("") ? "" : " (" + muteData.getReason() + ")"));
			getPlayer().sendMessage(ChatColor.RED + "Expires " + muteData.getExpiry().toString());
			return;
		}
		
		String messageSenderInfo = /*getLevelColor() + */""/* + getLevel() + " "*/;
		
		if(getRank().hasChatPrefix()) {
			messageSenderInfo += getRank().getChatPrefix() + " ";
		}
		
		messageSenderInfo += getRank().getNameColor() + getName();
		
		TextComponent messageComponent = new TextComponent(messageSenderInfo);
		messageComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + "" + ChatColor.BOLD + getName() + "\n")
				.append(ChatColor.GRAY + "Rank: " + ChatColor.RESET + getRank().getNameColor() + getRank().getRankName() + "\n")
				.append(ChatColor.GRAY + "Level: " + getLevelColor() + getLevel() + "\n")
				.append(ChatColor.GRAY + "XP: " + ChatColor.RESET + getXP() + "\n")
				.append(ChatColor.GRAY + "Gold: " + ChatColor.RESET + getGold() + "\n")
				.append(ChatColor.GRAY + "Location: " + ChatColor.RESET + StringUtil.locToString(getPlayer().getLocation())
					+ ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (when message sent)\n") 
				.append(ChatColor.GRAY + "Floor: " + ChatColor.RESET + FloorLoader.fromWorld(getPlayer().getWorld()).getDisplayName()
					+ ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (when message sent)\n")
				.append(ChatColor.GRAY + "First Joined: " + ChatColor.RESET + getFirstJoined().toString())
				.create()));
		
		messageComponent.addExtra(ChatColor.GRAY + " » " + getRank().getChatColor() + message);
		
		ChatChannel channel = getSpeakingChannel();
		if(!channel.canHear(this, this)) {
			player.sendMessage(ChatColor.RED + "It looks like you can't hear yourself! Make sure you can listen to the channel you're speaking on.");
		}
		Location location = player.getLocation();
		for(User user : UserLoader.allUsers()) {
			if(!channel.canHear(user, this)) continue;
			user.sendMessage(channel, location, messageComponent);
		}
		LOGGER.info("[" + channel.getAbbreviation() + "] [" + getName() + "] " + message);
	}
	
	public CommandSender getLastReceivedMessageFrom() {
		return lastReceivedMessageFrom;
	}

	public void setLastReceivedMessageFrom(CommandSender lastReceivedMessageFrom) {
		this.lastReceivedMessageFrom = lastReceivedMessageFrom;
	}

	public void setChatSpy(boolean enabled) {
		chatSpy = enabled;
	}
	
	public boolean hasChatSpy() {
		return chatSpy;
	}
	
	
	
	
	//
	// Inventory & Item Management
	//
	
	
	public void giveItem(Item item, boolean updateDB, boolean dbOnly, boolean silent) {
		int giveQuantity = item.getQuantity();
		int maxStackSize = item.getMaxStackSize();
		if(!dbOnly) {
			int remaining = giveQuantity;
			for(int i = 0; i < player.getInventory().getContents().length; i++) {
				ItemStack itemStack = player.getInventory().getContents()[i];
				if(itemStack == null) continue;
				Item testItem = ItemLoader.fromBukkit(itemStack);
				if(testItem == null) continue;
				if(item.getClassName().equals(testItem.getClassName()) && !item.isCustom() && !testItem.isCustom()) {
					int quantity = Math.min(maxStackSize, testItem.getQuantity() + remaining);
					int added = quantity - testItem.getQuantity();
					debug("Adding to existing stack: " + testItem.getUUID().toString() + " (curr=" + testItem.getQuantity() + ", add=" + added + ", tot=" + quantity + ")");
					remaining -= added;
					testItem.setQuantity(quantity);
					player.getInventory().setItem(i, testItem.getItemStack());
					item.setQuantity(item.getQuantity() - added);
					if(remaining == 0) break;
					debug(" - " + remaining + " remaining to dispense");
				}
			}
			if(remaining > 0) {
				debug("Adding remaining items as new item stack");
				player.getInventory().addItem(item.getItemStack());
			}
		}
		if(updateDB) {
			storageAccess.update(new Document("inventory", getInventoryAsDocument()));
		}
		if(!silent) {
			player.sendMessage(ChatColor.GRAY + "+ " + item.getDecoratedName() + (item.getQuantity() > 1 ? ChatColor.GRAY + " (x" + giveQuantity + ")" : ""));
		}

	}
	
	public void giveItem(Item item) {
		giveItem(item, true, false, false);
	}
	
	public void takeItem(Item item, int amount, boolean updateDB, boolean updateInventory, boolean notify) {
		debug("Removing " + amount + " of " + item.getName() + " (has " + item.getQuantity() + ")");
		if(amount < item.getQuantity()) {
			debug("-New quantity: " + item.getQuantity());
			item.setQuantity(item.getQuantity() - amount);
		}
		if(updateInventory) {
			ItemStack removal = item.getItemStack().clone();
			removal.setAmount(amount);
			player.getInventory().removeItem(removal);
		}
		if(updateDB) {
			storageAccess.update(new Document("inventory", getInventoryAsDocument()));
		}
		if(notify) {
			player.sendMessage(ChatColor.RED + "- " + item.getDecoratedName() + (amount > 1 ? ChatColor.GRAY + " (x" + amount + ")" : ""));
		}
	}
	
	public void takeItem(Item item) {
		takeItem(item, 1, true, true, true);
	}
	
	public Document getInventoryAsDocument() {
		Document inventory = new Document();
		for(int i = 0; i < player.getInventory().getContents().length; i++) {
			ItemStack is = player.getInventory().getContents()[i];
			if(is == null) continue;
			Item item = ItemLoader.fromBukkit(is);
			if(item == null) continue;
			inventory.append("I-" + i, item.getUUID());
		}
		ItemStack helmetStack = player.getInventory().getHelmet();
		Item helmet = ItemLoader.fromBukkit(helmetStack);
		if(helmet != null) {
			inventory.append("Helmet-0", helmet.getUUID());
		}
		ItemStack chestplateStack = player.getInventory().getChestplate();
		Item chestplate = ItemLoader.fromBukkit(chestplateStack);
		if(chestplate != null) {
			inventory.append("Chestplate-0", chestplate.getUUID());
		}

		ItemStack leggingsStack = player.getInventory().getLeggings();
		Item leggings = ItemLoader.fromBukkit(leggingsStack);
		if(leggings != null) {
			inventory.append("Leggings-0", leggings.getUUID());
		}

		ItemStack bootsStack = player.getInventory().getBoots();
		Item boots = ItemLoader.fromBukkit(bootsStack);
		if(boots != null) {
			inventory.append("Boots-0", boots.getUUID());
		}
		
		return inventory;
	}
	
	
	//
	//  Event Handlers
	//
	
	
	public void handleJoin(boolean firstJoin) {
		setData("lastJoined", System.currentTimeMillis());		
		if(isVanished()) {
			player.sendMessage(ChatColor.DARK_GREEN + "You are currently vanished.");
		}
		else {
			if(getRank().ordinal() >= Rank.PATRON.ordinal()) {
				Bukkit.broadcastMessage(getRank().getNameColor() + "" + ChatColor.BOLD + getRank().getRankName() + " " + player.getName() + " joined!");
			}
			else {
				Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " joined!");
			}
		}
		player.sendMessage(ChatColor.GOLD + "Hello " + getName() + " and welcome to DragonsOnline.");
		String spacer = ChatColor.GRAY + "    -    ";
		player.sendMessage(ChatColor.LIGHT_PURPLE + "Level: " + getLevel() + spacer + ChatColor.GREEN + "XP: " + getXP() + " (" + MathUtil.round(getLevelProgress() * 100) + "%)"
				+ spacer + ChatColor.YELLOW + "Gold: " + getGold());
		TextComponent component = new TextComponent(ChatColor.AQUA + "Speaking in ");
		TextComponent speaking = getSpeakingChannel().format();
		speaking.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		component.addExtra(speaking);
		component.addExtra(ChatColor.AQUA + " and listening to ");
		List<ChatChannel> channels = getActiveChatChannels();
		for(int i = 0; i < channels.size(); i++) {
			TextComponent listening = channels.get(i).format();
			listening.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
			component.addExtra(listening);
			if(i < channels.size() - 1) component.addExtra(", ");
		}
		player.spigot().sendMessage(component);
		if(firstJoin) {
			player.sendMessage(ChatColor.AQUA + "Use " + ChatColor.DARK_AQUA + "/channel" + ChatColor.AQUA + " to change channels.");
		}
		player.sendMessage("");
		
		updateState();
		updateVanishState();
		updateVanishStatesOnSelf();
		updateVanillaLeveling();
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
		player.getInventory().setArmorContents(new ItemStack[4]);
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
	
	
	//
	// Getters and Setters
	//
	
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
		player.sendMessage(ChatColor.GRAY + "+ " + ChatColor.GOLD + gold + " Gold");
	}
	
	public void takeGold(double gold) {
		setData("gold", getGold() - gold);
		player.sendMessage(ChatColor.RED + "- " + ChatColor.GOLD + gold + " Gold");
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
	
	public void overrideWalkSpeed(float speed) {
		player.setWalkSpeed(speed);
		isOverridingWalkSpeed = true;
	}
	
	public void removeWalkSpeedOverride() {
		isOverridingWalkSpeed = false;
		player.setWalkSpeed((float) getEffectiveWalkSpeed());
	}
	
	public double getEffectiveWalkSpeed() {
		if(isOverridingWalkSpeed) return player.getWalkSpeed();
		
		double speed = Dragons.getInstance().getServerOptions().getDefaultWalkSpeed();

		for(ItemStack itemStack : player.getInventory().getArmorContents()) {
			if(itemStack == null) continue;
			Item item = ItemLoader.fromBukkit(itemStack);
			if(item == null) continue;
			speed += item.getSpeedBoost();
		}
		
		ItemStack held = player.getInventory().getItemInMainHand();
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
		Floor floor = FloorLoader.fromFloorName(floorName);
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
			sendTitle(ChatColor.DARK_AQUA, "Level Up!", ChatColor.AQUA, getLevel() + "  >>>  " + level);
			Bukkit.broadcastMessage(ChatColor.AQUA + getName() + " is now level " + level + "!");
		}
		update(new Document("xp", xp).append("level", level));
		updateVanillaLeveling();
	}
	
	public void updateVanillaLeveling() {
		int level = getLevel();
		player.setLevel(level);
		player.setExp(getLevelProgress());
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(calculateMaxHealth(level));
	}
	
	public int getXP() {
		return (int) getData("xp");
	}
	
	public int getLevel() {
		return (int) getData("level");
	}
	
	public float getLevelProgress() {
		int prevMax = calculateMaxXP(getLevel());
		return (float) (getXP() - prevMax) / (calculateMaxXP(getLevel() + 1) - prevMax);
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
		if(userOf == null || userFor == null) return;
		if(userOf.player == null || userFor.player == null) return;
		if(userOf.isVanished() && userFor.getActivePermissionLevel().ordinal() < PermissionLevel.MOD.ordinal()) {
			userFor.player.hidePlayer(Dragons.getInstance(), userOf.player);
		}
		else if(!userFor.player.canSee(userOf.player)) {
			userFor.player.showPlayer(Dragons.getInstance(), userOf.player);
		}
	}
	
	public void updateVanishStatesOnSelf() {
		for(Player test : Bukkit.getOnlinePlayers()) {
			User user = UserLoader.fromPlayer(test);
			updateVanishStateBetween(user, this);
		}
	}
	
	public void updateVanishState() {
		player.setCollidable(!isVanished());
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
		LOGGER.fine("User " + getName() + " system profile set to " + (profile == null ? "null" : profile.getProfileName()));
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
		LOGGER.fine("User " + getName() + " active permission level set to " + permissionLevel);
		activePermissionLevel = permissionLevel;
		SystemProfileFlags flags = getSystemProfile().getFlags();
		player.addAttachment(Dragons.getInstance(), "worldedit.*", flags.hasFlag(SystemProfileFlag.WORLDEDIT));
		player.addAttachment(Dragons.getInstance(), "minecraft.command.give", permissionLevel.ordinal() >= PermissionLevel.GM.ordinal() || flags.hasFlag(SystemProfileFlag.CMD));
		player.addAttachment(Dragons.getInstance(), "minecraft.command.summon", permissionLevel.ordinal() >= PermissionLevel.GM.ordinal() || flags.hasFlag(SystemProfileFlag.CMD));
		player.addAttachment(Dragons.getInstance(), "minecraft.command.teleport", permissionLevel.ordinal() >= PermissionLevel.MOD.ordinal() || flags.hasFlag(SystemProfileFlag.CMD));
		player.addAttachment(Dragons.getInstance(), "minecraft.command.setworldspawn", permissionLevel.ordinal() >= PermissionLevel.GM.ordinal());
		player.setOp(flags.hasFlag(SystemProfileFlag.CMD)); // Until we patch the server jar, this will have to do
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
	
	
	
	//
	// Punishment Management
	//
	

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
	
	
	//
	// Auto-Saving
	//
	
	@Override
	public void autoSave() {
		super.autoSave();
		sendActionBar(ChatColor.GREEN + "Autosaving...");
		Document autoSaveData = new Document("lastLocation", StorageUtil.locToDoc(player.getLocation()))
				.append("lastSeen", System.currentTimeMillis())
				.append("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
				.append("health", player.getHealth())
				.append("inventory", getInventoryAsDocument());
		for(ItemStack itemStack : player.getInventory().getContents()) {
			if(itemStack == null) continue;
			Item item = ItemLoader.fromBukkit(itemStack);
			if(item == null) continue;
			item.autoSave();
		}
		update(autoSaveData);
	}

}
