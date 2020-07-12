package mc.dragons.core.gameobject;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.GameObjectLoader;
import mc.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.loader.StructureLoader;
import mc.dragons.core.gameobject.loader.UserLoader;

/**
 * Possible types of a {@link GameObject}.
 * 
 * A registry is associated with each object type, which is responsible for loading
 * and creating instances of that type.
 * 
 * @author Rick
 *
 */
public enum GameObjectType {
	USER(UserLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	ITEM_CLASS(ItemClassLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	ITEM(ItemLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
 	NPC(NPCLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
 	NPC_CLASS(NPCClassLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	QUEST(QuestLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	STRUCTURE(StructureLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	REGION(RegionLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager())),
	FLOOR(FloorLoader.getInstance(Dragons.getInstance(), Dragons.getInstance().getStorageManager()));

	private GameObjectLoader<?> loader;
	
	private <T extends GameObject> GameObjectType(GameObjectLoader<T> loader) {
		this.loader = loader;
	}
	
	public <T extends GameObject> GameObjectLoader<?> getLoader() {
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
