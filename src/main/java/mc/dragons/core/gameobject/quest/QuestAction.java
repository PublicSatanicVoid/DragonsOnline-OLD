package mc.dragons.core.gameobject.quest;

import java.util.List;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.item.ItemClass;
import mc.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.StorageUtil;
import mc.dragons.core.util.PathfindingUtil;

public class QuestAction {
	public static class QuestActionResult {
		private boolean stageModified;
		private boolean shouldPause;
		
		public QuestActionResult(boolean stageModified, boolean shouldPause) {
			this.stageModified = stageModified;
			this.shouldPause = shouldPause;
		}
		
		public boolean wasStageModified() {
			return stageModified;
		}
		
		public boolean shouldPause() {
			return shouldPause;
		}
	}
	
	
	public static enum QuestActionType {
		TELEPORT_PLAYER,
		SPAWN_NPC,
		TELEPORT_NPC,
		PATHFIND_NPC,
		BEGIN_DIALOGUE,
		GIVE_XP,
		GOTO_STAGE,
		TAKE_ITEM,
		GIVE_ITEM,
		ADD_POTION_EFFECT,
		REMOVE_POTION_EFFECT,
		COMPLETION_HEADER,
		WAIT
	}

	private static RegionLoader regionLoader;
	private static NPCClassLoader npcClassLoader;
	private static ItemClassLoader itemClassLoader;
	private static NPCLoader npcLoader;
	private static ItemLoader itemLoader;
	
	public static QuestAction fromDocument(Document action, Quest quest) {		
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
			itemClassLoader = (ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader();
			npcLoader = (NPCLoader) GameObjectType.NPC.<NPC>getLoader();
			itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
		}
		QuestAction questAction = new QuestAction();
		questAction.action = QuestActionType.valueOf(action.getString("type"));
		if(questAction.action == QuestActionType.TELEPORT_PLAYER) {
			questAction.to = StorageUtil.docToLoc(action.get("tpTo", Document.class));
		}
		else if(questAction.action == QuestActionType.SPAWN_NPC) {
			//questAction.npcClass = npcClassLoader.getNPCClassByClassName(action.getString("npcClass"));
			questAction.npcClass = null;
			questAction.npcClassShortName = action.getString("npcClass");
			questAction.npcReferenceName = action.getString("npcReferenceName");
		}
		else if(questAction.action == QuestActionType.BEGIN_DIALOGUE) {
			//questAction.npcClass = npcClassLoader.getNPCClassByClassName(action.getString("npcClass"));
			questAction.npcClass = null;
			questAction.npcClassShortName = action.getString("npcClass");
			questAction.dialogue = action.getList("dialogue", String.class);
		}
		else if(questAction.action == QuestActionType.GIVE_XP) {
			questAction.xpAmount = action.getInteger("xp");
		}
		else if(questAction.action == QuestActionType.GOTO_STAGE) {
			questAction.stage = action.getInteger("stage");
			questAction.notify = action.getBoolean("notify");
		}
		else if(questAction.action == QuestActionType.TELEPORT_NPC) {
			questAction.npcReferenceName = action.getString("npcReferenceName");
			questAction.to = StorageUtil.docToLoc(action.get("tpTo", Document.class));
		}
		else if(questAction.action == QuestActionType.PATHFIND_NPC) {
			questAction.npcReferenceName = action.getString("npcReferenceName");
			questAction.to = StorageUtil.docToLoc(action.get("tpTo", Document.class));
			questAction.stage = action.getInteger("stage");
		}
		else if(questAction.action == QuestActionType.TAKE_ITEM) {
			questAction.itemClass = itemClassLoader.getItemClassByClassName(action.getString("itemClass"));
			questAction.quantity = action.getInteger("quantity");
		}
		else if(questAction.action == QuestActionType.GIVE_ITEM) {
			questAction.itemClass = itemClassLoader.getItemClassByClassName(action.getString("itemClass"));
			questAction.quantity = action.getInteger("quantity");
		}
		else if(questAction.action == QuestActionType.ADD_POTION_EFFECT) {
			questAction.effectType = PotionEffectType.getByName(action.getString("effectType"));
			questAction.duration = action.getInteger("duration");
			questAction.amplifier = action.getInteger("amplifier");
		}
		else if(questAction.action == QuestActionType.REMOVE_POTION_EFFECT) {
			questAction.effectType = PotionEffectType.getByName(action.getString("effectType"));
		}
		else if(questAction.action == QuestActionType.WAIT) {
			questAction.waitTime = action.getInteger("waitTime");
		}
		
		questAction.quest = quest;
		return questAction;
	}
	
	public static QuestAction teleportPlayerAction(Quest quest, Location to) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.TELEPORT_PLAYER;
		action.to = to;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction spawnNPCAction(Quest quest, NPCClass npcClass, String referenceName) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.SPAWN_NPC;
		action.npcClass = npcClass;
		action.quest = quest;
		action.npcReferenceName = referenceName;
		return action;
	}
	
	public static QuestAction teleportNPCAction(Quest quest, String referenceName, Location to) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.TELEPORT_NPC;
		action.npcReferenceName = referenceName;
		action.to = to;
		action.quest = quest;
		return action;
	}
	
	/** Use -1 as gotoStage to not change stages on completion */
	public static QuestAction pathfindNPCAction(Quest quest, String referenceName, Location to, int gotoStage) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.PATHFIND_NPC;
		action.npcReferenceName = referenceName;
		action.to = to;
		action.quest = quest;
		action.stage = gotoStage;
		return action;
	}
	
	public static QuestAction beginDialogueAction(Quest quest, NPCClass npcClass, List<String> dialogue) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.BEGIN_DIALOGUE;
		action.npcClass = npcClass;
		action.dialogue = dialogue;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction giveXPAction(Quest quest, int xpAmount) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.GIVE_XP;
		action.xpAmount = xpAmount;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction goToStageAction(Quest quest, int stage, boolean notify) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.GOTO_STAGE;
		action.stage = stage;
		action.quest = quest;
		action.notify = notify;
		return action;
	}
	
	public static QuestAction takeItemAction(Quest quest, ItemClass itemClass, int quantity) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.TAKE_ITEM;
		action.itemClass = itemClass;
		action.quantity = quantity;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction giveItemAction(Quest quest, ItemClass itemClass, int quantity) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.GIVE_ITEM;
		action.itemClass = itemClass;
		action.quantity = quantity;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction addPotionEffectAction(Quest quest, PotionEffectType effectType, int duration, int amplifier) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.ADD_POTION_EFFECT;
		action.effectType = effectType;
		action.duration = duration;
		action.amplifier = amplifier;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction removePotionEffectAction(Quest quest, PotionEffectType effectType) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.REMOVE_POTION_EFFECT;
		action.effectType = effectType;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction completionHeaderAction(Quest quest) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.COMPLETION_HEADER;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction waitAction(Quest quest, int seconds) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.WAIT;
		action.waitTime = seconds;
		action.quest = quest;
		return action;
	}
	
	private Quest quest;
	private QuestActionType action;
	private String npcClassShortName;
	private NPCClass npcClass;
	private String npcReferenceName;
	private List<String> dialogue;
	private Location to;
	private int xpAmount;
	private int stage;
	private ItemClass itemClass;
	private int quantity;
	private boolean notify; // For GOTO_STAGE
	private PotionEffectType effectType;
	private int duration;
	private int amplifier;
	private int waitTime;
	
	public Document toDocument() {
		Document document = new Document("type", action.toString());
		switch(action) {
		case TELEPORT_PLAYER:
			document.append("tpTo", StorageUtil.locToDoc(to));
			break;
		case BEGIN_DIALOGUE:
			npcClassDeferredLoad();
			document.append("dialogue", dialogue).append("npcClass", npcClass.getClassName());
			break;
		case SPAWN_NPC:
			npcClassDeferredLoad();
			document.append("npcClass", npcClass.getClassName())
				.append("npcReferenceName", npcReferenceName);
			break;
		case GIVE_XP:
			document.append("xp", xpAmount);
			break;
		case GOTO_STAGE:
			document.append("stage", stage).append("notify", notify);
			break;
		case TELEPORT_NPC:
		case PATHFIND_NPC:
			document.append("npcReferenceName", npcReferenceName)
				.append("tpTo", StorageUtil.locToDoc(to))
				.append("stage", stage);
			break;
		case GIVE_ITEM:
		case TAKE_ITEM:
			document.append("itemClass", itemClass.getClassName())
				.append("quantity", quantity);
			break;
		case ADD_POTION_EFFECT:
			document.append("duration", duration).append("amplifier", amplifier);
		case REMOVE_POTION_EFFECT:
			document.append("effectType", effectType.getName());
			break;
		case COMPLETION_HEADER:
			break;
		case WAIT:
			document.append("waitTime", waitTime);
		}
		return document;
	}
	
	private void npcClassDeferredLoad() {
		if(npcClass == null) {
			npcClass = npcClassLoader.getNPCClassByClassName(npcClassShortName);
		}
	}
	
	public QuestActionType getActionType() {
		return action;
	}
	
	public NPCClass getNPCClass() {
		npcClassDeferredLoad();
		return npcClass;
	}
	
	public String getNPCReferenceName() {
		return npcReferenceName;
	}
	
	public List<String> getDialogue() {
		return dialogue;
	}
	
	public Location getLocation() {
		return to;
	}
	
	public int getXPAmount() {
		return xpAmount;
	}
	
	public int getGotoStage() {
		return stage;
	}
	
	public ItemClass getItemClass() {
		return itemClass;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public PotionEffectType getEffectType() {
		return effectType;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getAmplifier() {
		return amplifier;
	}
	
	public int getWaitTime() {
		return waitTime;
	}
	
	/**
	 * 
	 * @param user
	 * @return Whether the quest stage was modified.
	 */
	public QuestActionResult execute(User user) {
		//user.p().sendMessage("Executing quest stage. Action type is " + action.toString());
		if(action == QuestActionType.TELEPORT_PLAYER) {
			user.getPlayer().teleport(to);
		}
		else if(action == QuestActionType.SPAWN_NPC) {
			npcClassDeferredLoad();
			World world = user.getPlayer().getWorld();
			Location spawnLocation = user.getPlayer().getLocation().add(
					user.getPlayer().getLocation().getDirection().normalize().setY(0).multiply(2.0));
			NPC npc = npcLoader.registerNew(world, spawnLocation, npcClass);
			quest.registerNPCReference(user, npc, npcReferenceName);
		}
		else if(action == QuestActionType.BEGIN_DIALOGUE) {
			npcClassDeferredLoad();
			user.setDialogueBatch(quest, npcClass.getName(), dialogue);
			new BukkitRunnable() {
				@Override public void run() {
					if(!user.nextDialogue()) {
						user.updateQuests(null);
						this.cancel();
					}
				}
			}.runTaskTimer(Dragons.getInstance(), 0L, 20L * 2);
//			for(String line : dialogue) {
//				user.getPlayer().sendMessage(ChatColor.DARK_GREEN + "[" + npcClass.getName() + "] " + ChatColor.GREEN + line);
//			}
			return new QuestActionResult(false, true);
		}
		else if(action == QuestActionType.GIVE_XP) {
			user.getPlayer().sendMessage(ChatColor.GRAY + "+ " + ChatColor.LIGHT_PURPLE + xpAmount + " XP" + ChatColor.GRAY + " from quest " + quest.getQuestName());
			user.addXP(xpAmount);
		}
		else if(action == QuestActionType.GOTO_STAGE) {
			user.updateQuestProgress(quest, quest.getSteps().get(stage), notify);
			user.debug("    - going to stage " + stage + " (" + quest.getSteps().get(stage).getStepName() + ")");
			new BukkitRunnable() {
				@Override public void run() {
					user.updateQuests(null);
				}
			}.runTaskLater(Dragons.getInstance(), 1L);
			return new QuestActionResult(true, false);
		}
		else if(action == QuestActionType.TELEPORT_NPC) {
			npcClassDeferredLoad();
			NPC npc = quest.getNPCByReference(user, npcReferenceName);
			npc.getEntity().teleport(to);
		}
		else if(action == QuestActionType.PATHFIND_NPC) {
			npcClassDeferredLoad();
			NPC npc = quest.getNPCByReference(user, npcReferenceName);
			PathfindingUtil.walkToLocation(npc.getEntity(), to, 0.15, e -> {
				if(stage != -1) {
					user.updateQuestProgress(quest, quest.getSteps().get(stage), false);
				}
			});
			return new QuestActionResult(stage != -1, false);
		}
		else if(action == QuestActionType.TAKE_ITEM) {
			int remaining = quantity;
			for(ItemStack itemStack : user.getPlayer().getInventory().getContents()) {
				Item item = ItemLoader.fromBukkit(itemStack);
				if(item != null) {
					if(item.getClassName().equals(itemClass.getClassName())) {
						int removeAmount = Math.min(remaining, item.getQuantity());
						user.takeItem(item, removeAmount, true, true, false);
						remaining -= item.getQuantity();
						if(remaining <= 0) return new QuestActionResult(false, false);
					}
				}
			}
		}
		else if(action == QuestActionType.GIVE_ITEM) {
			Item item = itemLoader.registerNew(itemClass);
			item.setQuantity(quantity);
			user.giveItem(item);
		}
		else if(action == QuestActionType.ADD_POTION_EFFECT) {
			user.getPlayer().addPotionEffect(new PotionEffect(effectType, duration, amplifier), true);
		}
		else if(action == QuestActionType.REMOVE_POTION_EFFECT) {
			user.getPlayer().removePotionEffect(effectType);
		}
		else if(action == QuestActionType.COMPLETION_HEADER) {
			user.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Quest Complete: " + quest.getQuestName());
			user.getPlayer().sendMessage(ChatColor.GRAY + "Rewards:");
		}
		else if(action == QuestActionType.WAIT) {
			user.debug("Waiting " + waitTime + "s");
			user.setQuestPaused(quest, true);
			new BukkitRunnable() {
				@Override public void run() {
					user.debug("Resuming quest actions");
					user.setQuestPaused(quest, false);
					user.updateQuests(null);
				}
			}.runTaskLater(Dragons.getInstance(), 20L * waitTime);
			return new QuestActionResult(false, true);
		}
		return new QuestActionResult(false, false);
	}
}
 