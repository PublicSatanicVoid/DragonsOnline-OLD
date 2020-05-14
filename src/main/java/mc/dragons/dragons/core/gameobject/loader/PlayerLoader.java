package mc.dragons.dragons.core.gameobject.loader;

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

public class PlayerLoader extends GameObjectRegistry {

	private static PlayerLoader INSTANCE;
	
	private Map<Player, User> playerToUser;
	
	private PlayerLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		playerToUser = new HashMap<>();
	}
	
	public synchronized static PlayerLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new PlayerLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	@Override
	public User loadObject(StorageAccess storageAccess) {
		Player p = plugin.getServer().getPlayer((UUID)storageAccess.get("_id"));
		User user = new User(p, storageManager, storageAccess);
		playerToUser.put(p, user);
		return user;
	}

	public User registerNew(Player player) {
		Document skills = new Document();
		for(SkillType skill : SkillType.values()) {
			skills.append(skill.toString(), 0);
		}
		Document data = new Document("_id", player.getUniqueId())
				.append("username", player.getName())
				.append("maxHealth", player.getMaxHealth())
				.append("xp", 0)
				.append("level", 1)
				.append("permissionLevel", PermissionLevel.USER.getLevel())
				.append("rank", Rank.DEFAULT.toString())
				.append("firstJoined", System.currentTimeMillis())
				.append("skills", skills);
		// TODO: continue init
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.PLAYER, data);
		User user = new User(player, storageManager, storageAccess);
		playerToUser.put(player, user);
		registeredObjects.add(user);
		return user;
	}
	
	public User fromPlayer(Player player) {
		return playerToUser.get(player);
	}
	
	public void removeStalePlayer(Player player) {
		playerToUser.remove(player);
	}
	
}
