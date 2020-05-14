package mc.dragons.dragons.core.gameobject.npc;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Represents an NPC in the RPG.
 * 
 * <p>In addition to the properties of Minecraft NPCs,
 * RPG NPCs have properties like dialogue and enhanced
 * combat capabilities, as well as levels and more specific
 * interaction options.
 * 
 * <p>There is a many-to-many has-a relationship between
 * RPG NPC types and Minecraft NPC types.
 * 
 * @author Rick
 *
 */
public class NPC extends GameObject {

	public NPC(StorageManager storageManager) {
		super(GameObjectType.NPC, storageManager);
		// TODO: Specific instantiation via storageAccess
	}
	
}
