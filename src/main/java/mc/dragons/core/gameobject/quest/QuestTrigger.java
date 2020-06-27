package mc.dragons.core.gameobject.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.User;

public class QuestTrigger {
	public static enum TriggerType {
		ENTER_REGION,
		EXIT_REGION,
		CLICK_NPC,
		KILL_NPC,
		INSTANT,
		BRANCH_CONDITIONAL
	};
	
	private static RegionLoader regionLoader;
	private static NPCClassLoader npcClassLoader;
	
	public static QuestTrigger fromDocument(Document trigger, Quest quest) {
		if(regionLoader == null) {
			regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
			npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
		}
		
		
		QuestTrigger questTrigger = new QuestTrigger();
		
		questTrigger.type = TriggerType.valueOf(trigger.getString("type"));
		if(questTrigger.type == TriggerType.ENTER_REGION || questTrigger.type == TriggerType.EXIT_REGION) {
			questTrigger.region = regionLoader.getRegionByName(trigger.getString("region"));
		}
		else if(questTrigger.type == TriggerType.CLICK_NPC || questTrigger.type == TriggerType.KILL_NPC) {
			questTrigger.npcClass = npcClassLoader.getNPCClassByClassName(trigger.getString("npcClass"));
		}
		else if(questTrigger.type == TriggerType.BRANCH_CONDITIONAL) {
			questTrigger.branchPoints = new HashMap<>();
			for(Document conditional : trigger.getList("branchPoints", Document.class)) {
				questTrigger.branchPoints.put(
						QuestTrigger.fromDocument((Document) conditional.get("trigger"), quest) , 
						QuestAction.fromDocument((Document) conditional.get("action"), quest)); 
			}
		}
		
		return questTrigger;
	}
	
	private TriggerType type;
	private NPCClass npcClass;
	private Region region;
	private Map<QuestTrigger, QuestAction> branchPoints;
	
	private QuestTrigger() { }

	public static QuestTrigger onEnterRegion(Region region) {
		QuestTrigger trigger = new QuestTrigger();
		trigger.type = TriggerType.ENTER_REGION;
		trigger.region = region;
		return trigger;
	}
	
	public static QuestTrigger onExitRegion(Region region) {
		QuestTrigger trigger = new QuestTrigger();
		trigger.type = TriggerType.EXIT_REGION;
		trigger.region = region;
		return trigger;
	}
	
	public static QuestTrigger onClickNPC(NPCClass npcClass) {
		QuestTrigger trigger = new QuestTrigger();
		trigger.type = TriggerType.CLICK_NPC;
		trigger.npcClass = npcClass;
		return trigger;
	}
	
	public static QuestTrigger onKillNPC(NPCClass npcClass) {
		QuestTrigger trigger = new QuestTrigger();
		trigger.type = TriggerType.KILL_NPC;
		trigger.npcClass = npcClass;
		return trigger;
	}
	
	public static QuestTrigger instant() {
		QuestTrigger trigger = new QuestTrigger();
		trigger.type = TriggerType.INSTANT;
		return trigger;
	}
	
	public static QuestTrigger branchConditional(Map<QuestTrigger, QuestAction> branchPoints) {
		QuestTrigger trigger = new QuestTrigger();
		trigger.type = TriggerType.BRANCH_CONDITIONAL;
		trigger.branchPoints = branchPoints;
		return trigger;
	}
	
	public TriggerType getTriggerType() {
		return type;
	}
	
	public NPCClass getNPCClass() {
		return npcClass;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public Map<QuestTrigger, QuestAction> getBranchPoints() {
		return branchPoints;
	}
	
	public Document toDocument() {
		Document document = new Document("type", type.toString());
		switch(type) {
		case ENTER_REGION:
		case EXIT_REGION:
			document.append("region", region.getName());
			break;
		case CLICK_NPC:
		case KILL_NPC:
			document.append("npcClass", npcClass.getName());
			break;
		case BRANCH_CONDITIONAL:
			List<Document> conditions = new ArrayList<>();
			for(Entry<QuestTrigger, QuestAction> entry : branchPoints.entrySet()) {
				conditions.add(new Document("trigger", entry.getKey().toDocument()).append("action", entry.getValue().toDocument()));
			}
			document.append("branchPoints", conditions);
		default:
			break;
		}
		return document;
	}
	
	public boolean test(User user, Event event) {
		//user.p().sendMessage("DEBUG: Testing trigger type " + type.toString());
		if(type == TriggerType.INSTANT) {
			return true;
		}
		if(type == TriggerType.ENTER_REGION) {
			//user.p().sendMessage(" - Testing region " + region.getName());
			user.updateState(false, false); // Force calculation based on EXACT position, but don't re-call triggers
			if(user.getRegions().contains(region)) {
				//user.p().sendMessage(" - Yes!");
				return true;
			}
		}
		if(type == TriggerType.EXIT_REGION) {
			//user.p().sendMessage(" - Testing region " + region.getName());
			user.updateState(false, false);
			if(!user.getRegions().contains(region)) {
				//user.p().sendMessage(" - Yes!");
				return true;
			}
		}
		if(type == TriggerType.CLICK_NPC) {
			//user.p().sendMessage(" - Testing NPC class " + npcClass.getName());
			if(event == null) return false;
			if(event instanceof PlayerInteractEntityEvent) {
				PlayerInteractEntityEvent interactEvent = (PlayerInteractEntityEvent) event;
				NPC npc = NPCLoader.fromBukkit(interactEvent.getRightClicked());
				if(npc == null) return false;
				if(npc.getNPCClass().equals(npcClass)) {
					//user.p().sendMessage(" - Yes!");
					return true;
				}
			}
		}
		if(type == TriggerType.KILL_NPC) {
			//user.p().sendMessage(" - Testing NPC class " + npcClass.getName());
			if(event == null) return false;
			if(event instanceof EntityDeathEvent) {
				EntityDeathEvent deathEvent = (EntityDeathEvent) event;
				NPC npc = NPCLoader.fromBukkit(deathEvent.getEntity());
				if(npc == null) return false;
				if(npc.getNPCClass().equals(npcClass)) {
					//user.p().sendMessage(" - Yes!");
					return true;
				}
			}
		}
		if(type == TriggerType.BRANCH_CONDITIONAL) {
			for(Entry<QuestTrigger, QuestAction> conditional : branchPoints.entrySet()) {
				//user.p().sendMessage(" - Testing trigger " + conditional.getKey().getTriggerType().toString() + " (results below)");
				if(conditional.getKey().test(user, event)) {
					//user.p().sendMessage(" - Yes! (to branch conditional) - executing action.");
					conditional.getValue().execute(user);
					return true;
				}
			}
		}
		return false;
	}
}
