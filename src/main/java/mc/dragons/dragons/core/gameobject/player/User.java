package mc.dragons.dragons.core.gameobject.player;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;
import mc.dragons.dragons.core.storage.StorageUtil;

/**
 * Represents a player in the RPG.
 * 
 * <p>Like all {@link mc.dragons.dragons.core.gameobject.GameObject}s,
 * users are backed by the database.
 * 
 * <p>In addition to the standard properties of players, users
 * have specific skills which can be leveled up, as well as friends,
 * guilds, parties, quest logs, and expanded inventories.
 * 
 * @author Rick
 *
 */
public class User extends GameObject {

	private Player player;
	
	public User(Player player, StorageManager storageManager, StorageAccess storageAccess) {
		super(GameObjectType.PLAYER, 
				(UUID)storageAccess.get("_id"), 
				storageManager);
		this.player = player;
		// TODO: implement skills!
	}
	
	public void handleJoin() {
		setData("lastJoined", System.currentTimeMillis());
		player.sendMessage(ChatColor.GOLD + "Hello " + player.getName() + " and welcome to Dragons.");
		player.sendMessage(ChatColor.YELLOW + "Your level is " + getData("level") + " [" + getData("xp") + " XP]");
	}
	
	public Player p() {
		return player;
	}
	
	public void sendActionBar(String message) {
		Dragons.getInstance().getBridge().sendActionBar(player, message);
	}
	
	public void sendTitle(ChatColor titleColor, String title, ChatColor subtitleColor, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
		Dragons.getInstance().getBridge().sendTitle(player, titleColor, title, subtitleColor, subtitle, fadeInTime, showTime, fadeOutTime);
	}
	
	public void respawn() {
		Dragons.getInstance().getBridge().respawnPlayer(player);
	}
	
	@Override
	public void autoSave() {
		sendActionBar(ChatColor.GREEN + "Autosaving...");
		Document autoSaveData = new Document("lastLocation", StorageUtil.locToDoc(player.getLocation()))
				.append("lastSeen", System.currentTimeMillis());
		update(autoSaveData);
	}

}
