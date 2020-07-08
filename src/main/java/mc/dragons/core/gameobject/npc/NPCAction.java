package mc.dragons.core.gameobject.npc;

import java.util.List;

import org.bson.Document;
import org.bukkit.ChatColor;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.user.User;

public class NPCAction {
	public enum NPCActionType {
		BEGIN_QUEST,
		BEGIN_DIALOGUE
	};
	
	private static NPCClassLoader npcClassLoader;
	private static QuestLoader questLoader;
	
	private NPCActionType type;
	private NPCClass npcClass;
	private Quest quest;
	private List<String> dialogue;
	
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
	
	public Document toDocument() {
		Document result = new Document("type", type.toString());
		
		switch(type) {
		case BEGIN_QUEST:
			result.append("quest", quest.getName());
			break;
		case BEGIN_DIALOGUE:
			result.append("dialogue", dialogue);
			break;
		}
		
		return result;
	}
	
	public void execute(User user) {
		switch(type) {
		case BEGIN_QUEST:
			user.updateQuestProgress(quest, quest.getSteps().get(0));
			break;
		case BEGIN_DIALOGUE:
			for(String line : dialogue) {
				user.getPlayer().sendMessage(ChatColor.DARK_GREEN + "[" + npcClass.getName() + "] " + ChatColor.GREEN + line);
			}
			break;
		}
	}
}
