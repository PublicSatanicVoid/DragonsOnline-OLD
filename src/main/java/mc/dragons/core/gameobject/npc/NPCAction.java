package mc.dragons.core.gameobject.npc;

import java.util.List;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.StorageUtil;
import mc.dragons.core.util.PathfindingUtil;

public class NPCAction {
	public enum NPCActionType {
		BEGIN_QUEST,
		BEGIN_DIALOGUE,
		TELEPORT_NPC,
		PATHFIND_NPC
	};
	
	private static NPCClassLoader npcClassLoader;
	private static QuestLoader questLoader;
	
	private NPCActionType type;
	private NPCClass npcClass;
	private Quest quest;
	private List<String> dialogue;
	private Location to;
	
	private NPCAction() {}
	
	public static NPCAction fromDocument(NPCClass npcClass, Document document) {
		if(npcClassLoader == null) {
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
			questLoader = (QuestLoader) GameObjectType.QUEST.<Quest>getLoader();
		}
		
		NPCActionType type = NPCActionType.valueOf(document.getString("type"));
		switch(type) {
		case BEGIN_QUEST:
			return beginQuest(npcClass, questLoader.getQuestByName(document.getString("quest")));
		case BEGIN_DIALOGUE:
			return beginDialogue(npcClass, document.getList("dialogue", String.class));
		case TELEPORT_NPC:
			return teleportNPC(npcClass, StorageUtil.docToLoc(document.get("to", Document.class)));
		case PATHFIND_NPC:
			return pathfindNPC(npcClass, StorageUtil.docToLoc(document.get("to", Document.class)));
		}
		
		return null;
	}
	
	public static NPCAction beginQuest(NPCClass npcClass, Quest quest) {
		NPCAction action = new NPCAction();
		action.type = NPCActionType.BEGIN_QUEST;
		action.npcClass = npcClass;
		action.quest = quest;
		return action;
	}
	
	public static NPCAction beginDialogue(NPCClass npcClass, List<String> dialogue) {
		NPCAction action = new NPCAction();
		action.type = NPCActionType.BEGIN_DIALOGUE;
		action.npcClass = npcClass;
		action.dialogue = dialogue;
		return action;
	}
	
	public static NPCAction teleportNPC(NPCClass npcClass, Location to) {
		NPCAction action = new NPCAction();
		action.type = NPCActionType.TELEPORT_NPC;
		action.npcClass = npcClass;
		action.to = to;
		return action;
	}
	
	public static NPCAction pathfindNPC(NPCClass npcClass, Location to) {
		NPCAction action = new NPCAction();
		action.type = NPCActionType.PATHFIND_NPC;
		action.npcClass = npcClass;
		action.to = to;
		return action;
	}
	
	public NPCActionType getType() {
		return type;
	}
	
	public NPCClass getNPCClass() {
		return npcClass;
	}
	
	public Quest getQuest() {
		return quest;
	}
	
	public List<String> getDialogue() {
		return dialogue;
	}
	
	public Location getTo() {
		return to;
	}
	
	public Document toDocument() {
		Document result = new Document("type", type.toString());
		
		switch(type) {
		case BEGIN_QUEST:
			result.append("quest", quest.getName());
			break;
		case BEGIN_DIALOGUE:
			result.append("dialogue", dialogue);
			break;
		case TELEPORT_NPC:
		case PATHFIND_NPC:
			result.append("to", StorageUtil.locToDoc(to));
			break;
		}
		
		return result;
	}
	
	public void execute(User user, NPC npc) {
		switch(type) {
		case BEGIN_QUEST:
			user.updateQuestProgress(quest, quest.getSteps().get(0));
			break;
		case BEGIN_DIALOGUE:
			user.setDialogueBatch(null, npcClass.getName(), dialogue);
			new BukkitRunnable() {
				@Override public void run() {
					if(!user.nextDialogue()) {
						this.cancel();
					}
				}
			}.runTaskTimer(Dragons.getInstance(), 0L, 20L * 2);
//			for(String line : dialogue) {
//				user.getPlayer().sendMessage(ChatColor.DARK_GREEN + "[" + npcClass.getName() + "] " + ChatColor.GREEN + line);
//			}
			break;
		case TELEPORT_NPC:
			npc.getEntity().teleport(to);
			break;
		case PATHFIND_NPC:
			PathfindingUtil.walkToLocation(npc.getEntity(), to, 0.15, null);
		}
	}
}
