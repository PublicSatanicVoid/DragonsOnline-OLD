package mc.dragons.dragons.core.gameobject.player;

import java.util.Date;
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
	
	public static int calculateLevel(int xp) {
		return (int) Math.floor(xp / 50000 + Math.sqrt(xp / 50));
	}
	
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
	
	public void addXP(int xp) {
		int totalXP = (int)getData("xp") + xp;
		int level = calculateLevel(totalXP);
		if(level > getLevel()) {
			sendTitle(ChatColor.DARK_AQUA, "Level Up!", ChatColor.AQUA, getLevel() + "  >>>  " + level, 10, 10, 10);
		}
		update(new Document("xp", totalXP).append("level", level));
	}
	
	public int getXP() {
		return (int)getData("xp");
	}
	
	public int getLevel() {
		return (int)getData("level");
	}
	
	public ChatColor getLevelColor() {
		int level = getLevel();
		if(level < 10) {
			return ChatColor.GRAY;
		}
		else if(level < 20) {
			return ChatColor.AQUA;
		}
		else if(level < 30) {
			return ChatColor.GREEN;
		}
		else if(level < 40) {
			return ChatColor.YELLOW;
		}
		else if(level < 50) {
			return ChatColor.DARK_AQUA;
		}
		else if(level < 60) {
			return ChatColor.GOLD;
		}
		else if(level < 70) {
			return ChatColor.DARK_GREEN;
		}
		else if(level < 80) {
			return ChatColor.LIGHT_PURPLE;
		}
		else if(level < 90) {
			return ChatColor.DARK_PURPLE;
		}
		else if(level < 100) {
			return ChatColor.RED;
		}
		else {
			return ChatColor.WHITE;
		}
	}
	
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.fromInt((int)getData("permissionLevel"));
	}
	
	public Rank getRank() {
		return Rank.valueOf((String)getData("rank"));
	}
	
	public Date getFirstJoined() {
		return new Date((long)getData("firstJoined"));
	}
	
	public Date getLastJoined() {
		return new Date((long)getData("lastJoined"));
	}
	
	public Date getLastSeen() {
		return new Date((long)getData("lastSeen"));
	}
	
	public int getSkillLevel(SkillType type) {
		return (int)((Document) getData("skills")).getInteger(type.toString());
	}
	
	@Override
	public void autoSave() {
		sendActionBar(ChatColor.GREEN + "Autosaving...");
		Document autoSaveData = new Document("lastLocation", StorageUtil.locToDoc(player.getLocation()))
				.append("lastSeen", System.currentTimeMillis());
		update(autoSaveData);
	}

}
