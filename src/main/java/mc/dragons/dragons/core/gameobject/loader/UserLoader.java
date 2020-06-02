package mc.dragons.dragons.core.gameobject.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.player.PermissionLevel;
import mc.dragons.dragons.core.gameobject.player.Rank;
import mc.dragons.dragons.core.gameobject.player.SkillType;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

public class UserLoader extends GameObjectLoader<User> {

	private static UserLoader INSTANCE;
	
	private Map<Player, User> playerToUser;
	private GameObjectRegistry masterRegistry;
	
	private UserLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		playerToUser = new HashMap<>();
		masterRegistry = instance.getGameObjectRegistry();
	}
	
	public synchronized static UserLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new UserLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public User loadObject(StorageAccess storageAccess) {
		for(User user : playerToUser.values()) {
			if(user.getIdentifier().equals(storageAccess.getIdentifier())) {
				return user;
			}
		}
		Player p = plugin.getServer().getPlayer((UUID)storageAccess.get("_id"));
		User user = new User(p, storageManager, storageAccess);
		playerToUser.put(p, user);
		masterRegistry.getRegisteredObjects().add(user);
		return user;
	}

	public User loadObject(UUID uuid) {
		for(User user : playerToUser.values()) {
			if(user.getUUID().equals(uuid)) {
				return user;
			}
		}
		StorageAccess storageAccess = storageManager.getStorageAccess(GameObjectType.USER, uuid);
		if(storageAccess == null) return null;
		return loadObject(storageAccess);
	}
	
	public User loadObject(String username) {
		for(User user : playerToUser.values()) {
			if(user.p() == null) continue;
			if(user.p().getName().equalsIgnoreCase(username)) {
				return user;
			}
		}
		StorageAccess storageAccess = storageManager.getStorageAccess(GameObjectType.USER, new Document("username", username));
		if(storageAccess == null) return null;
		return loadObject(storageAccess);
	}
	
	public User registerNew(Player player) {
		Document skills = new Document();
		Document skillProgress = new Document();
		for(SkillType skill : SkillType.values()) {
			skills.append(skill.toString(), 0);
			skillProgress.append(skill.toString(), 0.0);
		}
		Document data = new Document("_id", player.getUniqueId())
				.append("username", player.getName())
				.append("maxHealth", player.getMaxHealth())
				.append("xp", 0)
				.append("level", 1)
				.append("permissionLevel", PermissionLevel.USER.toString())
				.append("rank", Rank.DEFAULT.toString())
				.append("firstJoined", System.currentTimeMillis())
				.append("skills", skills)
				.append("skillProgress", skillProgress)
				.append("inventory", new ArrayList<>());
		// TODO: continue init
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.USER, data);
		User user = new User(player, storageManager, storageAccess);
		playerToUser.put(player, user);
		masterRegistry.getRegisteredObjects().add(user);
		return user;
	}
	
	public User fromPlayer(Player player) {
		return playerToUser.get(player);
	}
	
	public void removeStalePlayer(Player player) {
		masterRegistry.getRegisteredObjects().remove(playerToUser.get(player));
		playerToUser.remove(player);
	}
	
	public void unregister(User user) {
		masterRegistry.getRegisteredObjects().remove(user);
		playerToUser.remove(user.p());
	}
	
}
