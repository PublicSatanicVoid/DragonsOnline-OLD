# DragonsRPG
Minecraft MMORPG with dragons!

## Object Structure
All non-local aspects of gameplay are stored in or as game objects. Such game objects include players (`User`), NPCs (`NPC`), quests (`Quest`), and structures (`Structure`).

All game objects extend the `GameObject` class, and have a corresponding loader extending `GameObjectRegistry`.

All game objects are associated with a unique `StorageAccess` which takes care of saving to the backend.
All game object loaders are associated with a (not necessarily unique) `StorageManager` which creates and fetches `StorageAccess`es from the backend.

All game object types are found in the `GameObjectType` enum class. To access the loader for a particular type of game object, use `GameObjectType.(TYPE).getLoader()` and cast it to the appropriate loader type, e.g. `PlayerLoader` or `NPCLoader`.
