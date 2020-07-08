package mc.dragons.core.gameobject.quest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
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
	public static enum QuestActionType {
		TELEPORT_PLAYER,
		SPAWN_NPC,
		TELEPORT_NPC,
		PATHFIND_NPC,
		BEGIN_DIALOGUE,
		GIVE_XP,
		GOTO_STAGE,
		TAKE_ITEM,
		GIVE_ITEM
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
	
	public static QuestAction goToStageAction(Quest quest, int stage) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.GOTO_STAGE;
		action.stage = stage;
		action.quest = quest;
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
	
	public Document toDocument() {
		Document document = new Document("type", action.toString());
		switch(action) {
		case TELEPORT_PLAYER:
			document.append("tpTo", StorageUtil.locToDoc(to));
			break;
		case BEGIN_DIALOGUE:
			document.append("dialogue", dialogue).append("npcClass", npcClassShortName);
			break;
		case SPAWN_NPC:
			document.append("npcClass", npcClassShortName)
				.append("npcReferenceName", npcReferenceName);
			break;
		case GIVE_XP:
			document.append("xp", xpAmount);
			break;
		case GOTO_STAGE:
			document.append("stage", stage);
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
	
	/**
	 * 
	 * @param user
	 * @return Whether the quest stage was modified.
	 */
	public boolean execute(User user) {
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
			// TODO: add delays and stuff
			for(String line : dialogue) {
				user.getPlayer().sendMessage(ChatColor.DARK_GREEN + "[" + npcClass.getName() + "] " + ChatColor.GREEN + line);
			}
		}
		else if(action == QuestActionType.GIVE_XP) {
			user.addXP(xpAmount);
			user.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "+ " + xpAmount + " XP" + ChatColor.GRAY + " from quest " + quest.getQuestName());
		}
		else if(action == QuestActionType.GOTO_STAGE) {
			user.updateQuestProgress(quest, quest.getSteps().get(stage), false);
			user.debug("    - going to stage " + stage + " (" + quest.getSteps().get(stage).getStepName() + ")");
			new BukkitRunnable() {
				@Override public void run() {
					user.updateQuests(null);
				}
			}.runTaskLater(Dragons.getInstance(), 1L);
			return true;
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
			return stage != -1;
		}
		else if(action == QuestActionType.TAKE_ITEM) { // TODO clean this up maybeee
			Set<Item> take = new HashSet<>();
			int taken = 0;
			for(ItemStack itemStack : user.getPlayer().getInventory().getContents()) {
				Item item = ItemLoader.fromBukkit(itemStack);
				if(item != null) {
					if(item.getClassName().equals(itemClass.getClassName())) {
						take.add(item);
						taken += itemStack.getAmount();
					}
				}
				if(taken >= quantity) break;
			}
			for(Item item : take) {
				//user.getPlayer().getInventory().remove(itemStack);
				user.takeItem(item, true, false);
			}
			if(taken > quantity) {
				Item compensate = itemLoader.registerNew(itemClass);
				compensate.getItemStack().setAmount(taken - quantity);
				user.giveItem(compensate, true, false, true);
			}
		}
		else if(action == QuestActionType.GIVE_ITEM) {
			Item item = itemLoader.registerNew(itemClass);
			item.getItemStack().setAmount(quantity);
			user.giveItem(item);
		}
		return false;
	}
}
 