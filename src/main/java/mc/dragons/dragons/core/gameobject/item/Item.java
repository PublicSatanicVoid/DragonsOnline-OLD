package mc.dragons.dragons.core.gameobject.item;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Represents a general item in the RPG.
 * 
 * <p>In addition to regular properties of Minecraft items,
 * RPG items have additional properties, like XP/skill
 * requirements, use effects, etc.
 * 
 * <p>More than one RPG item may be mapped to the same
 * Minecraft item, and vice versa.
 * 
 * @author Rick
 *
 */
public class Item extends GameObject {
	
	public Item(StorageManager storageManager) {
		super(GameObjectType.ITEM, storageManager);
		// TODO: Specific instantiation via storageAccess
	}
	
}
