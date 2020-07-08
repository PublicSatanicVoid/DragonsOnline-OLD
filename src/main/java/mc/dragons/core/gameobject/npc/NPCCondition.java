package mc.dragons.core.gameobject.npc;

import org.bson.Document;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.quest.QuestStep;
import mc.dragons.core.gameobject.user.User;

public class NPCCondition {
	public enum NPCConditionType {
		HAS_COMPLETED_QUEST,
		HAS_QUEST_STAGE,
		HAS_LEVEL,
		HAS_GOLD
	}
	
	private NPCConditionType type;
	private boolean inverse;
	private Quest quest;
	private int stageRequirement;
	private int levelRequirement;
	private double goldRequirement;

	private static QuestLoader questLoader;
	
	private NPCCondition() {}
	
	public static NPCCondition fromDocument(Document document) {
		if(questLoader == null) {
			questLoader = (QuestLoader) GameObjectType.QUEST.<Quest>getLoader();
		}
		NPCConditionType type = NPCConditionType.valueOf(document.getString("type"));
		switch(type) {
		case HAS_COMPLETED_QUEST:
			return hasCompletedQuest(questLoader.getQuestByName(document.getString("quest")), document.getBoolean("inverse"));
		case HAS_QUEST_STAGE:
			return hasQuestStage(questLoader.getQuestByName(document.getString("quest")), document.getInteger("stage"), document.getBoolean("inverse"));
		case HAS_LEVEL:
			return hasLevel(document.getInteger("level"), document.getBoolean("inverse"));
		case HAS_GOLD:
			return hasGold(document.getDouble("gold"), document.getBoolean("inverse"));
		}
		return null;
	}
	
	public static NPCCondition hasCompletedQuest(Quest quest, boolean inverse) {
		NPCCondition cond = new NPCCondition();
		cond.type = NPCConditionType.HAS_COMPLETED_QUEST;
		cond.quest = quest;
		cond.inverse = inverse;
		return cond;
	}

	public static NPCCondition hasQuestStage(Quest quest, int stage, boolean inverse) {
		NPCCondition cond = new NPCCondition();
		cond.type = NPCConditionType.HAS_QUEST_STAGE;
		cond.quest = quest;
		cond.stageRequirement = stage;
		cond.inverse = inverse;
		return cond;
	}
	
	public static NPCCondition hasLevel(int levelReq, boolean inverse) {
		NPCCondition cond = new NPCCondition();
		cond.type = NPCConditionType.HAS_LEVEL;
		cond.levelRequirement = levelReq;
		cond.inverse = inverse;
		return cond;
	}
	
	public static NPCCondition hasGold(double goldReq, boolean inverse) {
		NPCCondition cond = new NPCCondition();
		cond.type = NPCConditionType.HAS_GOLD;
		cond.goldRequirement = goldReq;
		cond.inverse = inverse;
		return cond;
	}
	
	
	public NPCConditionType getType() {
		return type;
	}
	
	public boolean isInverse() {
		return inverse;
	}
	
	public Quest getQuest() {
		return quest;
	}
	
	public int getStageRequirement() {
		return stageRequirement;
	}
	
	public int getLevelRequirement() {
		return levelRequirement;
	}
	
	public double getGoldRequirement() {
		return goldRequirement;
	}
	
	public Document toDocument() {
		Document document = new Document("type", type.toString()).append("inverse", inverse);
		switch(type) {
		case HAS_COMPLETED_QUEST:
			document.append("quest", quest.getName());
			break;
		case HAS_QUEST_STAGE:
			document.append("quest", quest.getName()).append("stage", stageRequirement);
			break;
		case HAS_LEVEL:
			document.append("level", levelRequirement);
			break;
		case HAS_GOLD:
			document.append("gold", goldRequirement);
		}
		return document;
	}
	
	public boolean test(User user) {
		boolean result = false;;
		switch(type) {
		case HAS_COMPLETED_QUEST:
			QuestStep step = user.getQuestProgress().get(quest);
			if(step == null) result = false;
			else result = user.getQuestProgress().get(quest).getStepName().equals("Complete");
			break;
		case HAS_QUEST_STAGE:
			QuestStep step2 = user.getQuestProgress().get(quest);
			if(step2 == null) result = false;
			else result = quest.getStepIndex(step2) >= stageRequirement;
			break;
		case HAS_LEVEL:
			result = user.getLevel() >= levelRequirement;
			break;
		case HAS_GOLD:
			result = user.getGold() >= goldRequirement;
			break;
		}
		return inverse ? !result : result;
	}
}
