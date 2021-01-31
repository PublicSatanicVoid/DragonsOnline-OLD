__Note:__ References to `@author Rick` are because that is the online alias I used to use. However, I (Adam Priebe) am the author of all this code, unless a file notes otherwise.

# DragonsRPG
Minecraft MMORPG with dragons!

## Object Structure
All non-local aspects of gameplay are stored in or as game objects. Such game objects include players (`User`), NPCs (`NPC`), quests (`Quest`), and structures (`Structure`).

All game objects extend the `GameObject` class, and have a corresponding loader extending `GameObjectLoader`.

All game objects are associated with a unique `StorageAccess` which takes care of saving to the backend.
All game object loaders are associated with a (not necessarily unique) `StorageManager` which creates and fetches `StorageAccess`es from the backend.

All game object types are found in the `GameObjectType` enum class. To access the loader for a particular type of game object, use `GameObjectType.(TYPE).<TypeClass>getLoader()` and cast it to the appropriate loader type, e.g. `PlayerLoader` or `NPCLoader`.

All game objects should be stored in one or more `GameObjectRegistry`s, which allow for centralized loading and processing.

A minimal game object class would look like:

	public class MyObject extends GameObject {
	
		public MyObject(StorageManager storageManager, StorageAccess storageAccess) {
			super(storageManager, storageAccess);
		}
		
	}

and a minimal game object loader class would look like:

	public class MyObjectLoader extends GameObjectLoader<MyObject> {
		
		private static MyObjectLoader INSTANCE;
		private GameObjectRegistry registry;
		
		private MyObjectLoader(Dragons instance, StorageManager storageManager) {
			super(instance, storageManager);
			registry = instance.getGameObjectRegistry();
		}
		
		public synchronized static MyObjectLoader getInstance(Dragons instance, StorageManager storageManager) {
			if(INSTANCE == null) {
				INSTANCE = new MyObjectLoader(instance, storageManager);
			}
			return INSTANCE;
		}
		
		@Override
		public MyObject loadObject(StorageAccess storageAccess) {
			MyObject myObject = new MyObject(storageManager, storageAccess);
			registry.getRegisteredObjects().add(myObject);
			return myObject;
		}
		
		public MyObject registerNew() {
			StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.MY_OBJECT);
			return loadObject(storageAccess);
		}
		
	}
	
In practice, game object loaders are rarely this tidy. Most objects require additional initialization and may require backing from a Bukkit object, such as an `Entity` or `ItemStack`. It is common to overload the `registerNew()` method with various initialization strategies, and for configurable meta-objects to have their own meta-class, e.g. `NPCClass` or `ItemClass` with an associated loader.

Note that these "class" designations are not related to the Java notion of a class. They are used like a class only to the extent that they may be passed in to certain loaders to assist in the initialization of a game object 
