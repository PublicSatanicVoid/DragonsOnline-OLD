package mc.dragons.dragons.core.gameobject.quest;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.storage.StorageManager;

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
 * @author Rick
 *
 */
public class Quest extends GameObject {

	public Quest(StorageManager storageManager) {
		super(GameObjectType.QUEST, storageManager);
		// TODO: Specific instantiation via storageAccess
	}

}
