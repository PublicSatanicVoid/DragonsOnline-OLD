package mc.dragons.dragons.core.gameobject.structure;

import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Represents a structure in the RPG. Structures are pre-made
 * builds, such as houses or towers, which can be generated in
 * certain areas by players as a substitute for unrestricted
 * building.
 * 
 * @author Rick
 *
 */
public class Structure extends GameObject {

	public Structure(StorageManager storageManager) {
		super(GameObjectType.STRUCTURE, storageManager);
		// TODO: Specific instantiation via storageAccess
	}
	
}
