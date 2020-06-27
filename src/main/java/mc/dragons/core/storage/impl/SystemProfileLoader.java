package mc.dragons.core.storage.impl;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bson.Document;
import org.bson.UuidRepresentation;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;

public class SystemProfileLoader {
	private static MongoDatabase database;
	private static MongoCollection<Document> profileCollection;
	
	static {
		ConnectionString connectionString = new ConnectionString("mongodb://" + MongoConfig.USER + ":" + MongoConfig.PASSWORD + "@" + MongoConfig.HOST + ":" + MongoConfig.PORT + "/?authSource=" + MongoConfig.AUTH_DB);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.build();
		MongoClient client = MongoClients.create(settings);
		database = client.getDatabase(MongoConfig.DATABASE);
		profileCollection = database.getCollection(MongoConfig.SYSPROFILES_COLLECTION);
	}
	
	public static SystemProfile loadProfile(User user, String profileName, String profilePassword) {
		if(!isAvailable(profileName, user.getName())) return null;
		Document profile = profileCollection.find(new Document("profileName", profileName).append("profilePasswordHash", passwordHash(profilePassword))).first();
		if(profile == null) return null;
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("currentUser", user.getName())));
		SystemProfile systemProfile = new SystemProfile(user, profileName, PermissionLevel.valueOf(profile.getString("maxPermissionLevel")));
		return systemProfile;
	}
	
	public static boolean isAvailable(String profileName, String testUser) {
		Document profile = profileCollection.find(new Document("profileName", profileName)).first();
		if(profile == null) return true;
		if(!profile.getString("currentUser").equals(testUser) && !profile.getString("currentUser").equals("")) return false;
		return true;
	}
	
	public static void createProfile(String profileName, String profilePassword, PermissionLevel permissionLevel) {
		profileCollection.insertOne(new Document("profileName", profileName).append("profilePasswordHash", passwordHash(profilePassword)).append("maxPermissionLevel", permissionLevel.toString()).append("currentUser", ""));
	}
	
	public static void logoutProfile(String profileName) {
		profileCollection.updateOne(new Document("profileName", profileName), new Document("$set", new Document("currentUser", "")));
	}
	
	private static String passwordHash(String password) {
		try {
			return new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(("DragonsOnline System Logon b091283a#1*&AJK@83" + password).getBytes(StandardCharsets.UTF_8))).toString(16);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "SHA256HashFailedNoSuchAlgorithmException";
		}
	}
}
