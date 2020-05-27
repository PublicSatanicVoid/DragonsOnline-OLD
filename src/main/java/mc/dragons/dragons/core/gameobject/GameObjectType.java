package mc.dragons.dragons.core.gameobject;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.loader.StructureLoader;

/**
 * Possible types of a {@link mc.dragons.dragons.core.gameobject.GameObject}.
 * 
 * A registry is associated with each object type, which is responsible for loading
 * and creating instances of that type.
 * 
 * @author Rick
 *
 */
public enum GameObjectType {
	PLAYER(PlayerLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	ITEM(ItemLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
 	NPC(NPCLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	QUEST(QuestLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	STRUCTURE(StructureLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	REGION(RegionLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager()));

	private GameObjectRegistry loader;
	
	private GameObjectType(GameObjectRegistry loader) {
		this.loader = loader;
	}
	
	public GameObjectRegistry getLoader() {
		return loader;
	}

	public static GameObjectType get(String type) {
		for(GameObjectType objType : values()) {
			if(objType.toString().equalsIgnoreCase(type)) {
				return objType;
			}
		}
		return null;
	}
}
