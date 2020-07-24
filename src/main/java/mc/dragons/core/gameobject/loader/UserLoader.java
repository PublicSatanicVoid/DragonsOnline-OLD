package mc.dragons.core.gameobject.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.user.Rank;
import mc.dragons.core.gameobject.user.SkillType;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.gameobject.user.User.ChatChannel;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

public class UserLoader extends GameObjectLoader<User> {

	private static UserLoader INSTANCE;
	private static Logger LOGGER = Dragons.getInstance().getLogger();
	
	private static Set<User> users;
	private GameObjectRegistry masterRegistry;
	
	private UserLoader(Dragons instance, StorageManager storageManager) {
		super(instance, storageManager);
		users = new HashSet<>();
		masterRegistry = instance.getGameObjectRegistry();
	}
	
	public synchronized static UserLoader getInstance(Dragons instance, StorageManager storageManager) {
		if(INSTANCE == null) {
			INSTANCE = new UserLoader(instance, storageManager);
		}
		return INSTANCE;
	}
	
	public static User fixUser(User user) {
		Player oldPlayer = user.getPlayer();
		Player newPlayer = Bukkit.getPlayerExact(user.getName());
		if(oldPlayer != newPlayer) {
			user.initialize(newPlayer);
			users.add(user);
			assign(newPlayer, user);
		}
		return user;
	}
	
	@Override
	public User loadObject(StorageAccess storageAccess) {
		LOGGER.fine("Loading user by storage access " + storageAccess.getIdentifier());
		for(User user : users) {
			if(user.getIdentifier().equals(storageAccess.getIdentifier())) {
				LOGGER.finer(" - Found user in cache, fixing and returning");
				return fixUser(user);
			}
		}
		Player p = plugin.getServer().getPlayer((UUID) storageAccess.get("_id"));
		if(p == null) {
			LOGGER.warning("Attempting to load user with an offline or nonexistent player (" + storageAccess.getIdentifier() + ")");
		}
		User user = new User(p, storageManager, storageAccess);
		assign(p, user);
		users.add(user);
		masterRegistry.getRegisteredObjects().add(user);
		return user;
	}

	public User loadObject(UUID uuid) {
		LOGGER.fine("Loading user by UUID " + uuid);
		for(User user : users) {
			if(user.getUUID().equals(uuid)) {
				LOGGER.finer(" - Found user in cache, fixing and returning");
				return fixUser(user);
			}
		}
		StorageAccess storageAccess = storageManager.getStorageAccess(GameObjectType.USER, uuid);
		if(storageAccess == null) return null;
		return loadObject(storageAccess);
	}
	
	public User loadObject(String username) {
		LOGGER.fine("Loading user by username " + username);
		for(User user : users) {
			if(user.getName().equalsIgnoreCase(username)) {
				LOGGER.finer(" - Found user in cache, fixing and returning");
				return fixUser(user);
			}
		}
		StorageAccess storageAccess = storageManager.getStorageAccess(GameObjectType.USER, new Document("username", username));
		if(storageAccess == null) return null;
		return loadObject(storageAccess);
	}
	
	public User registerNew(Player player) {
		LOGGER.fine("Registering new user " + player.getName());
		Document skills = new Document();
		Document skillProgress = new Document();
		for(SkillType skill : SkillType.values()) {
			skills.append(skill.toString(), 0);
			skillProgress.append(skill.toString(), 0.0);
		}
		Document data = new Document("_id", player.getUniqueId())
				.append("username", player.getName())
				.append("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
				.append("xp", 0)
				.append("level", 1)
				.append("rank", Rank.DEFAULT.toString())
				.append("gold", 0.0)
				.append("godMode", false)
				.append("firstJoined", System.currentTimeMillis())
				.append("lastJoined", System.currentTimeMillis())
				.append("lastSeen", System.currentTimeMillis())
				.append("skills", skills)
				.append("skillProgress", skillProgress)
				.append("inventory", new Document())
				.append("quests", new Document())
				.append("vanished", false)
				.append("punishmentHistory", new ArrayList<>())
				.append("chatChannels", Arrays.asList(ChatChannel.LOCAL.toString()))
				.append("speakingChannel", ChatChannel.LOCAL.toString());
		StorageAccess storageAccess = storageManager.getNewStorageAccess(GameObjectType.USER, data);
		User user = new User(player, storageManager, storageAccess);
		assign(player, user);
		masterRegistry.getRegisteredObjects().add(user);
		return user;
	}
	
	public static void assign(Player player, User user) {
		LOGGER.fine("Assigning player " + player + " to user " + user);
		if(player != null) {
			player.removeMetadata("handle", Dragons.getInstance());
			player.setMetadata("handle", new FixedMetadataValue(Dragons.getInstance(), user));
		}
		user.setPlayer(player);
	}
	
	public static User fromPlayer(Player player) {
		if(player == null) return null;
		if(!player.hasMetadata("handle")) return null;
		if(player.getMetadata("handle").size() == 0) return null;
		Object value = player.getMetadata("handle").get(0).value();
		if(value instanceof User) return (User) value;
		return null;
	}
	
	public void removeStalePlayer(Player player) {
		LOGGER.fine("Removing stale player " + player.getName());
		User user = fromPlayer(player);
		masterRegistry.getRegisteredObjects().remove(user);
		users.remove(user);
	}
	
	public void unregister(User user) {
		LOGGER.fine("Locally unregistering player " + user.getName());
		masterRegistry.getRegisteredObjects().remove(user);
		users.remove(user);
	}

	public static Collection<User> allUsers() {
		return users;
	}
	
}
