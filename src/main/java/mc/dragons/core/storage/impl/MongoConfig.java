package mc.dragons.core.storage.impl;

import org.bson.UuidRepresentation;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Configuration values for the MongoDB connection.
 * 
 * @author Rick
 *
 */
public class MongoConfig {
	public static final String USER = "admin";
	public static final String PASSWORD = "RemoteSrv792128342#5qQ!";
	public static final String HOST = "73.65.109.123";
	public static final int PORT = 27017;
	public static final String AUTH_DB = "test";
	public static final String DATABASE = "dragons";
	public static final String GAMEOBJECTS_COLLECTION = "gameobjects";
	public static final String SYSPROFILES_COLLECTION = "sysprofiles";
	public static final String FEEDBACK_COLLECTION = "feedback";
	
	private static MongoDatabase database;
	
	static {
		ConnectionString connectionString = new ConnectionString("mongodb://" + MongoConfig.USER + ":" + MongoConfig.PASSWORD + "@" + MongoConfig.HOST + ":" + MongoConfig.PORT + "/?authSource=" + MongoConfig.AUTH_DB);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.build();
		MongoClient client = MongoClients.create(settings);
		database = client.getDatabase(MongoConfig.DATABASE);
	}
	
	public static MongoDatabase getDatabase() {
		return database;
	}
}
