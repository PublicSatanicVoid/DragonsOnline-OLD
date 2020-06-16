package mc.dragons.dragons.core.storage.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bson.UuidRepresentation;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.storage.Identifier;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Implementation of {@link mc.dragons.dragons.core.storage.StorageManager}
 * for MongoDB.
 * 
 * @author Rick
 *
 */
public class MongoStorageManager implements StorageManager {

	private MongoDatabase database;
	private MongoCollection<Document> gameObjectCollection;
	private Dragons plugin;
	
	public MongoStorageManager(Dragons instance, String host, String port, String username, String password, String authDB) {
		ConnectionString connectionString = new ConnectionString("mongodb://" + username + ":" + password + "@" + host + ":" + port + "/?authSource=" + authDB);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.build();
		MongoClient client = MongoClients.create(settings);
		plugin = instance;
		if(client == null) {
			plugin.getLogger().warning("Could not connect to MongoDB");
		}
		else {
			plugin.getLogger().info("Successfully connected to MongoDB");
		}
		database = client.getDatabase(MongoConfig.DATABASE);
		gameObjectCollection = database.getCollection(MongoConfig.GAMEOBJECTS_COLLECTION);
	}

	public StorageAccess getStorageAccess(GameObjectType objectType, UUID objectUUID) {
		return getStorageAccess(objectType, new Document("_id", objectUUID));
	}
	
	public StorageAccess getStorageAccess(GameObjectType objectType, Document search) {
		FindIterable<Document> results = gameObjectCollection.find(search.append("type", objectType.toString()));
		Document result = results.first();
		if(result == null) {
			return null;
		}
		UUID uuid = (UUID) result.get("_id");
		Identifier identifier = new Identifier(objectType, uuid);
		return new MongoStorageAccess(identifier, result, gameObjectCollection);
	}
	
	
	public Set<StorageAccess> getAllStorageAccess(GameObjectType objectType) {
		if(gameObjectCollection == null) {
			Dragons.getInstance().getLogger().info("gameObjectCollection is NULL");
		}
		if(objectType == null) {
			Dragons.getInstance().getLogger().info("objectType parameter is NULL");
		}
		FindIterable<Document> dbResults = gameObjectCollection.find(new Document("type", objectType.toString()));
		Set<StorageAccess> result = new HashSet<>();
		
		for(Document d : dbResults) {
			Identifier id = new Identifier(GameObjectType.get(d.getString("type")),
					(UUID) d.get("_id"));
			result.add(new MongoStorageAccess(id, d, gameObjectCollection));
		}
		
		return result;
	}
	
	public void storeObject(GameObject gameObject) {
		gameObjectCollection.updateOne(
				new Document("type", gameObject.getType().toString())
					.append("_id", gameObject.getUUID()),
				new Document("$set", gameObject.getData()));
	}

	public StorageAccess getNewStorageAccess(GameObjectType objectType) {
		return getNewStorageAccess(objectType, new Document());
	}

	public StorageAccess getNewStorageAccess(GameObjectType objectType, UUID objectUUID) {
		return getNewStorageAccess(objectType, new Document("_id", objectUUID));
	}

	public StorageAccess getNewStorageAccess(GameObjectType objectType, Document initialData) {
		Identifier identifier = new Identifier(objectType, initialData.containsKey("_id")
				? (UUID) initialData.get("_id")
				: UUID.randomUUID());
		StorageAccess storageAccess = new MongoStorageAccess(identifier, initialData, gameObjectCollection);
		Document insert = new Document(identifier.getDocument());
		insert.putAll(initialData);
		gameObjectCollection.insertOne(insert);
		return storageAccess;
	}

	@Override
	public void removeObject(GameObject gameObject) {
		gameObjectCollection.deleteOne(gameObject.getIdentifier().getDocument());
	}

	@Override
	public void push(GameObjectType objectType, Document selector, Document update) {
		gameObjectCollection.updateMany(new Document(selector).append("type", objectType.toString()), new Document("$set", update));
	}

}
