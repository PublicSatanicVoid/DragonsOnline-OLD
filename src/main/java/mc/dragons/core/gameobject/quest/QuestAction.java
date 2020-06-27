package mc.dragons.core.gameobject.quest;

import java.util.List;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.StorageUtil;

public class QuestAction {
	public static enum QuestActionType {
		TELEPORT_PLAYER,
		SPAWN_NPC,
		BEGIN_DIALOGUE,
		GIVE_XP,
		GOTO_STAGE
	}

	private static RegionLoader regionLoader;
	private static NPCClassLoader npcClassLoader;
	private static NPCLoader npcLoader;
	
	public static QuestAction fromDocument(Document action, Quest quest) {		
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
			npcLoader = (NPCLoader) GameObjectType.NPC.<NPC>getLoader();
		}
		QuestAction questAction = new QuestAction();
		questAction.action = QuestActionType.valueOf(action.getString("type"));
		if(questAction.action == QuestActionType.TELEPORT_PLAYER) {
			questAction.tpTo = StorageUtil.docToLoc((Document) action.get("tpTo"));
		}
		else if(questAction.action == QuestActionType.SPAWN_NPC) {
			questAction.npcClass = npcClassLoader.getNPCClassByClassName(action.getString("npcClass"));
		}
		else if(questAction.action == QuestActionType.BEGIN_DIALOGUE) {
			questAction.npcClass = npcClassLoader.getNPCClassByClassName(action.getString("npcClass"));
			questAction.dialogue = action.getList("dialogue", String.class);
		}
		else if(questAction.action == QuestActionType.GIVE_XP) {
			questAction.xpAmount = action.getInteger("xp");
		}
		else if(questAction.action == QuestActionType.GOTO_STAGE) {
			questAction.stage = action.getInteger("stage");
		}
		questAction.quest = quest;
		return questAction;
	}
	
	public static QuestAction teleportPlayerAction(Quest quest, Location to) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.TELEPORT_PLAYER;
		action.tpTo = to;
		action.quest = quest;
		return action;
	}
	
	public static QuestAction spawnNPCAction(Quest quest, NPCClass npcClass) {
		QuestAction action = new QuestAction();
		action.action = QuestActionType.SPAWN_NPC;
		action.npcClass = npcClass;
		action.quest = quest;
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
	
	private Quest quest;
	private QuestActionType action;
	private NPCClass npcClass;
	private List<String> dialogue;
	private Location tpTo;
	private int xpAmount;
	private int stage;
	
	public Document toDocument() {
		Document document = new Document("type", action.toString());
		switch(action) {
		case TELEPORT_PLAYER:
			document.append("tpTo", StorageUtil.locToDoc(tpTo));
			break;
		case BEGIN_DIALOGUE:
			document.append("dialogue", dialogue);
		case SPAWN_NPC:
			document.append("npcClass", npcClass.getClassName());
			break;
		case GIVE_XP:
			document.append("xp", xpAmount);
			break;
		case GOTO_STAGE:
			document.append("stage", stage);
		}
		return document;
	}
	
	public QuestActionType getActionType() {
		return action;
	}
	
	public NPCClass getNPCClass() {
		return npcClass;
	}
	
	public List<String> getDialogue() {
		return dialogue;
	}
	
	public Location getLocation() {
		return tpTo;
	}
	
	public int getXPAmount() {
		return xpAmount;
	}
	
	public int getGotoStage() {
		return stage;
	}
	
	/**
	 * 
	 * @param user
	 * @return Whether the quest stage was modified.
	 */
	public boolean execute(User user) {
		//user.p().sendMessage("Executing quest stage. Action type is " + action.toString());
		if(action == QuestActionType.TELEPORT_PLAYER) {
			user.p().teleport(tpTo);
		}
		else if(action == QuestActionType.SPAWN_NPC) {
			World world = user.p().getWorld();
			Location spawnLocation = user.p().getLocation().add(
					user.p().getLocation().getDirection().normalize().multiply(2.0));
			npcLoader.registerNew(world, spawnLocation, npcClass);
		}
		else if(action == QuestActionType.BEGIN_DIALOGUE) {
			// TODO: add delays and stuff
			for(String line : dialogue) {
				user.p().sendMessage(ChatColor.DARK_GREEN + npcClass.getName() + ": " + ChatColor.GREEN + line);
			}
		}
		else if(action == QuestActionType.GIVE_XP) {
			user.addXP(xpAmount);
			user.p().sendMessage(ChatColor.LIGHT_PURPLE + "+ " + xpAmount + " XP" + ChatColor.GRAY + " from quest " + quest.getQuestName());
		}
		else if(action == QuestActionType.GOTO_STAGE) {
			user.updateQuestProgress(quest, quest.getSteps().get(stage), false);
			return true;
		}
		return false;
	}
}
 