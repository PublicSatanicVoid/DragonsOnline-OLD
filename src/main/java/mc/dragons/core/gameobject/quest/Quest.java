package mc.dragons.core.gameobject.quest;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.quest.QuestTrigger.TriggerType;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

/**
 * Represents a quest in the game. There should only be
 * one instance of a quest per quest.
 * 
 * <p>A quest is defined in terms of a series of steps,
 * each of which is associated with a specific trigger,
 * such as clicking on an NPC or entering a region.
 * 
 * <p>Players can obtain skill points, items, and XP from
 * completing quests.
 * 
 * <p>The last step in a quest should be named "Complete"
 * and should represent that the player has completed the
 * quest.
 * 
 * @author Rick
 *
 */
public class Quest extends GameObject {

	private List<QuestStep> steps;
	
	public Quest(StorageManager storageManager, StorageAccess storageAccess) {
		super(storageManager, storageAccess);
		
		steps = new ArrayList<>();	
		
		@SuppressWarnings("unchecked")
		List<Document> rawSteps = (List<Document>) getData("steps");
		for(Document step : rawSteps) {
			steps.add(QuestStep.fromDocument(step, this));
		}
	}

	public List<Document> getStepsAsDoc() {
		return getStorageAccess().getDocument().getList("steps", Document.class);
	}
	
	public int getStepIndex(QuestStep questStep) {
		return getSteps().indexOf(questStep);
	}
	
	public void addStep(QuestStep step) {
		steps.add(step);
		List<Document> stepsDoc = getStepsAsDoc();
		stepsDoc.add(step.toDocument());
		update(new Document("steps", stepsDoc));
	}
	
	public void delStep(int step) {
		List<Document> stepsDoc = getStepsAsDoc();
		stepsDoc.remove(step);
		steps.remove(step);
		update(new Document("steps", stepsDoc));
	}
	
	public List<QuestStep> getSteps() {
		return steps;
	}
	
	public String getQuestName() {
		return (String) getData("questName");
	}
	
	public void setQuestName(String questName) {
		setData("questName", questName);
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public int getLevelMin() {
		return (int) getData("lvMin");
	}
	
	public void setLevelMin(int lvMin) {
		setData("lvMin", lvMin);
	}

	public boolean isValid() {
		if(steps.size() == 0) return false;
		QuestStep finalStep = steps.get(steps.size() - 1);
		if(!finalStep.getStepName().equalsIgnoreCase("Complete")) return false;
		if(finalStep.getTrigger().getTriggerType() != TriggerType.INSTANT) return false;
		if(finalStep.getActions().size() != 0) return false;
		return true;
	}
	
}
