package mc.dragons.core.gameobject.quest;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import mc.dragons.core.gameobject.user.User;

public class QuestStep {
	private QuestTrigger trigger;
	private List<QuestAction> actions;
	private String stepName;
	private Quest quest;
	
	
	public QuestStep(String stepName, QuestTrigger trigger, List<QuestAction> actions, Quest quest) {
		this.stepName = stepName;
		this.trigger = trigger;
		this.actions = actions;
		this.quest = quest;
	}
	
	public static QuestStep fromDocument(Document step, Quest quest) {
		return new QuestStep(step.getString("stepName"), 
				QuestTrigger.fromDocument((Document) step.get("trigger"), quest), 
				step.getList("actions", Document.class)
					.stream()
					.map(d -> QuestAction.fromDocument(d, quest))
					.collect(Collectors.toList()),
				quest);
	}

	public Document toDocument() {
		return new Document("stepName", stepName)
				.append("trigger", trigger.toDocument())
				.append("actions", actions.stream().map(a -> a.toDocument()).collect(Collectors.toList()));
	}
	
	public QuestTrigger getTrigger() {
		return trigger;
	}
	
	public void setTrigger(QuestTrigger trigger) {
		this.trigger = trigger;
		int stepIndex = quest.getStepIndex(this);
		List<Document> steps = quest.getStepsAsDoc();
		steps.get(stepIndex).append("trigger", trigger.toDocument());
		quest.getStorageAccess().update(new Document("steps", steps));
	}
	
	public void addAction(QuestAction action) {
		actions.add(action);
		int stepIndex = quest.getStepIndex(this);
		List<Document> steps = quest.getStepsAsDoc();
		List<Document> actions = steps.get(stepIndex).getList("actions", Document.class);
		actions.add(action.toDocument());
		quest.getStorageAccess().update(new Document("steps", steps));
	}
	
	public void addDialogue(int actionIndex, String dialogue) {
		actions.get(actionIndex).getDialogue().add(dialogue);
		List<Document> steps = quest.getStepsAsDoc();
		quest.getStorageAccess().update(new Document("steps", steps));
	}
	
	public void addBranchPoint(QuestTrigger trigger, QuestAction action) {
		this.trigger.getBranchPoints().put(trigger, action);
		int stepIndex = quest.getStepIndex(this);
		List<Document> steps = quest.getStepsAsDoc();
		steps.get(stepIndex).get("trigger", Document.class)
			.getList("branchPoints", Document.class)
			.add(new Document("trigger", trigger.toDocument())
				.append("action", action.toDocument()));
		quest.getStorageAccess().update(new Document("steps", steps));
	}
	
	public void deleteAction(int actionIndex) {
		actions.remove(actionIndex);
		int stepIndex = quest.getStepIndex(this);
		List<Document> steps = quest.getStepsAsDoc();
		steps.get(stepIndex).getList("actions", Document.class).remove(actionIndex);
		quest.getStorageAccess().update(new Document("steps", steps));
	}
	
	public List<QuestAction> getActions() {
		return actions;
	}
	
	public String getStepName() {
		return stepName;
	}
	
	public void setStepName(String stepName) {
		this.stepName = stepName;
		int stepIndex = quest.getStepIndex(this);
		List<Document> steps = quest.getStepsAsDoc();
		steps.get(stepIndex).append("stepName", stepName);
		quest.getStorageAccess().update(new Document("steps", steps));
	}
	
	/**
	 * 
	 * @param user
	 * @return Whether the caller needs to update the quest stage.
	 */
	public boolean executeActions(User user) {
		user.debug(" - Executing actions");
		boolean shouldUpdateStage = true;
		for(QuestAction action : actions) {
			user.debug("   - Action type " + action.getActionType());
			if(action.execute(user)) { // quest stage was modified
				shouldUpdateStage = false;
			}
		}
		return shouldUpdateStage;
	}
}
