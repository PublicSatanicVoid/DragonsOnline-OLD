package mc.dragons.core.gameobject.npc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bson.Document;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.user.User;

/**
 * Conditional actions that can be run when a certain trigger is met.
 * <p>
 * The trigger is supplied by an appropriate event handler and passed
 * to the appropriate class, which forwards it here.
 * <p>
 * There may be multiple sets of conditions -> actions pairs.
 * <p>
 * All conditions in a given pair must be met for the actions to be executed.
 * <p>
 * Conditions will be evaluated and actions will be executed in sequential
 * order of insertion in the database.
 * 
 * @author Rick
 *
 */
public class NPCConditionalActions {
	private static Logger LOGGER = Dragons.getInstance().getLogger();
	
	public enum NPCTrigger {
		 /** Attacking an NPC of this class */
		HIT,
		
		/** Right-clicking an NPC of this class */
		CLICK
	};
	
	public NPCConditionalActions(NPCTrigger trigger, NPCClass npcClass) {
		LOGGER.fine("Constructing conditional actions for " + npcClass.getClassName() + " with trigger " + trigger);
		this.trigger = trigger;
		this.npcClass = npcClass;
		this.conditionals = new LinkedHashMap<>();

		for(Document conditional : npcClass.getStorageAccess().getDocument()
				.get("conditionals", Document.class)
				.getList(trigger.toString(), Document.class)) {
			LOGGER.fine("- Found an action set");
			List<Document> conditions = conditional.getList("conditions", Document.class);
			List<Document> actions = conditional.getList("actions", Document.class);
			
			List<NPCCondition> parsedConditions = new ArrayList<>();
			List<NPCAction> parsedActions = new ArrayList<>();
			
			for(Document condition : conditions) {
				LOGGER.fine("  - Found a condition: " + condition.toJson());
				parsedConditions.add(NPCCondition.fromDocument(condition));
			}
			
			for(Document action : actions) {
				LOGGER.fine("  - Found an action: " + action.toJson());
				parsedActions.add(NPCAction.fromDocument(npcClass, action));
			}
			
			conditionals.put(parsedConditions, parsedActions);
		}
	}

	private NPCTrigger trigger;
	private NPCClass npcClass;
	private Map<List<NPCCondition>, List<NPCAction>> conditionals;

	public NPCTrigger getTrigger() {
		return trigger;
	}
	
	public NPCClass getNPCClass() {
		return npcClass;
	}
	
	public Map<List<NPCCondition>, List<NPCAction>> getConditionals() {
		return conditionals;
	}
	
	public void executeConditionals(User user, NPC npc) {
		user.debug("Executing conditional actions");
		for(Entry<List<NPCCondition>, List<NPCAction>> entry : conditionals.entrySet()) {
			boolean meetsConditions = true;
			for(NPCCondition condition : entry.getKey()) {
				if(!condition.test(user)) {
					meetsConditions = false;
					user.debug("- FAILED CONDITION " + condition.getType());
					break;
				}
			}
			if(meetsConditions) {
				user.debug("- MEETS ALL CONDITIONS, executing actions");
				for(NPCAction action : entry.getValue()) {
					action.execute(user, npc);
				}
			}
		}
	}
	
	public Entry<List<NPCCondition>, List<NPCAction>> getConditional(int index) {
		int i = 0;
		for(Entry<List<NPCCondition>, List<NPCAction>> entry : conditionals.entrySet()) {
			if(i == index) return entry;
			i++;
		}
		return null;
	}
	
	public void addLocalEntry() {
		conditionals.put(new ArrayList<>(), new ArrayList<>());
	}
	
	public void removeLocalEntry(int index) {
		List<NPCCondition> key = null;
		int i = 0;
		for(Entry<List<NPCCondition>, List<NPCAction>> entry : conditionals.entrySet()) {
			if(i == index) key = entry.getKey();
			i++;
		}
		if(key != null) {
			conditionals.remove(key);
		}
	}
	
	public List<Document> toDocument() {
		List<Document> result = new ArrayList<>();
		for(Entry<List<NPCCondition>, List<NPCAction>> entry : conditionals.entrySet()) {
			Document pair = new Document();
			List<Document> conditions = new ArrayList<>();
			List<Document> actions = new ArrayList<>();
			for(NPCCondition condition : entry.getKey()) {
				conditions.add(condition.toDocument());
			}
			for(NPCAction action : entry.getValue()) {
				actions.add(action.toDocument());
			}
			pair.append("conditions", conditions).append("actions", actions);
			result.add(pair);
		}
		return result;
	}
}
