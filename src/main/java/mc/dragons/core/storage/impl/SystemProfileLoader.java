package mc.dragons.core.storage.impl;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.impl.SystemProfile.SystemProfileFlags;
import mc.dragons.core.storage.impl.SystemProfile.SystemProfileFlags.SystemProfileFlag;

public class SystemProfileLoader {
	private static MongoDatabase database;
	private static MongoCollection<Document> profileCollection;
	
	private static Set<SystemProfile> profiles;
	
	private static Logger LOGGER;
	
	static {
		database = MongoConfig.getDatabase();
		profileCollection = database.getCollection(MongoConfig.SYSPROFILES_COLLECTION);
		LOGGER = Dragons.getInstance().getLogger();
		profiles = new HashSet<>();
	}
	
	public static SystemProfile authenticateProfile(User user, String profileName, String profilePassword) {
		if(!isAvailable(profileName, user.getName())) return null;
		SystemProfile systemProfile = loadProfile(profileName);
		if(systemProfile == null) return null;
		if(!systemProfile.isActive()) return null;
		if(!systemProfile.getPasswordHash().equals(passwordHash(profilePassword))) return null;
		systemProfile.setLocalCurrentUser(user);
		LOGGER.info(user.getName() + " logged into system profile " + profileName);
		return systemProfile;
	}
	
	public static SystemProfile loadProfile(String profileName) {
		for(SystemProfile profile : profiles) {
			if(profile.getProfileName().equalsIgnoreCase(profileName)) return profile;
		}
		Document profile = profileCollection.find(new Document("profileName", profileName)).first();
		if(profile == null) return null;
		Document flags = profile.get("flags", Document.class);
		SystemProfile systemProfile = new SystemProfile(null, profileName, profile.getString("profilePasswordHash"), PermissionLevel.valueOf(profile.getString("maxPermissionLevel")), 
				new SystemProfileFlags(flags), profile.getBoolean("active"));
		profiles.add(systemProfile);
		return systemProfile;
	}
	
	private static void kickProfileLocally(String profileName) {
		String currentUser = getCurrentUser(profileName);
		if(currentUser.equals("")) return;
		Player player = Bukkit.getPlayerExact(currentUser);
		player.kickPlayer("Your system profile changed, relog for updated permissions.");
		logoutProfile(profileName);
	}
	
	public static String getCurrentUser(String profileName) {
		Document profile = profileCollection.find(new Document("profileName", profileName)).first();
		if(profile == null) return "";
		return profile.getString("currentUser");
	}
	
	public static boolean isAvailable(String profileName, String testUser) {
		String currentUser = getCurrentUser(profileName);
		if(!currentUser.equals(testUser) && !currentUser.equals("")) return false;
		return true;
	}
	
	public static void setActive(String profileName, boolean active) {
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("active", active)));
		loadProfile(profileName).setLocalActive(active);
		if(!active) kickProfileLocally(profileName);
	}
	
	public static void createProfile(String profileName, String profilePassword, PermissionLevel permissionLevel) {
		profileCollection.insertOne(new Document("profileName", profileName)
				.append("profilePasswordHash", passwordHash(profilePassword))
				.append("maxPermissionLevel", permissionLevel.toString())
				.append("flags", SystemProfileFlags.emptyFlagsDocument())
				.append("currentUser", "")
				.append("active", true));
	}
	
	public static void setProfileMaxPermissionLevel(String profileName, PermissionLevel newMaxPermissionLevel) {
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("maxPermissionLevel", newMaxPermissionLevel.toString())));
		loadProfile(profileName).setLocalMaxPermissionLevel(newMaxPermissionLevel);
		kickProfileLocally(profileName);
	}
	
	public static void setProfileFlag(String profileName, String flagName, boolean flagValue) {
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("flags." + flagName, flagValue)));
		SystemProfileFlags flags = loadProfile(profileName).getFlags();
		flags.setLocalFlag(SystemProfileFlag.valueOf(flagName), flagValue);
		kickProfileLocally(profileName);
	}
	
	public static void setProfilePassword(String profileName, String newPassword) {
		String hash = passwordHash(newPassword);
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("profilePasswordHash", hash)));
		loadProfile(profileName).setLocalPasswordHash(hash);
		kickProfileLocally(profileName);
	}
	
	public static void logoutProfile(String profileName) {
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("currentUser", "")));
		loadProfile(profileName).setLocalCurrentUser(null);
	}
	
	public static String passwordHash(String password) {
		try {
			return new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(("DragonsOnline System Logon b091283a#1*&AJK@83" + password).getBytes(StandardCharsets.UTF_8))).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "SHA256HashFailedNoSuchAlgorithmException";
		}
	}
}
