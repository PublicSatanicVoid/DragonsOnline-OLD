package mc.dragons.core.storage.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.storage.Identifier;
import mc.dragons.core.storage.StorageAccess;
import mc.dragons.core.storage.StorageManager;

/**
 * Implementation of {@link mc.dragons.core.storage.StorageManager}
 * for MongoDB.
 * 
 * @author Rick
 *
 */
public class MongoStorageManager implements StorageManager {

	private MongoDatabase database;
	private MongoCollection<Document> gameObjectCollection;
	
	private Logger LOGGER;
	
	public MongoStorageManager(Dragons instance, String host, int port, String username, String password, String authDB) {
		LOGGER = Dragons.getInstance().getLogger();
		database = MongoConfig.getDatabase();
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
		UUID uuid = result.get("_id", UUID.class);
		Identifier identifier = new Identifier(objectType, uuid);
		
		LOGGER.finer("Retrieved storage access for type " + objectType.toString());
		
		return new MongoStorageAccess(identifier, result, gameObjectCollection);
	}
	
	
	public Set<StorageAccess> getAllStorageAccess(GameObjectType objectType) {
		return getAllStorageAccess(objectType, new Document());
	}


	@Override
	public Set<StorageAccess> getAllStorageAccess(GameObjectType objectType, Document filter) {
		if(gameObjectCollection == null) {
			LOGGER.severe("Could not load batch storage access: gameObjectCollection is NULL");
		}
		if(objectType == null) {
			LOGGER.warning("objectType parameter is NULL");
		}
		FindIterable<Document> dbResults = gameObjectCollection.find(filter.append("type", objectType.toString()));
		Set<StorageAccess> result = new HashSet<>();
		
		for(Document d : dbResults) {
			Identifier id = new Identifier(GameObjectType.get(d.getString("type")),
					d.get("_id", UUID.class));
			result.add(new MongoStorageAccess(id, d, gameObjectCollection));
		}
		
		LOGGER.finer("Found " + result.size() + " results for filtered storage accesses of type " + objectType.toString());
		
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
				? initialData.get("_id", UUID.class)
				: UUID.randomUUID());
		StorageAccess storageAccess = new MongoStorageAccess(identifier, initialData, gameObjectCollection);
		Document insert = new Document(identifier.getDocument());
		insert.putAll(initialData);
		gameObjectCollection.insertOne(insert);
		LOGGER.finer("Creating new storage access of type " + objectType.toString());
		return storageAccess;
	}

	@Override
	public void removeObject(GameObject gameObject) {
		DeleteResult result = gameObjectCollection.deleteOne(gameObject.getIdentifier().getDocument());
		LOGGER.finer("Results for deleting " + gameObject.getIdentifier() + ": deleted " + result.getDeletedCount() + " objects with identifier " + gameObject.getIdentifier());
	}

	@Override
	public void push(GameObjectType objectType, Document selector, Document update) {
		UpdateResult result = gameObjectCollection.updateMany(new Document(selector).append("type", objectType.toString()), new Document("$set", update));
		LOGGER.finer("Pushed database mass update for type " + objectType.toString() + ". Matched " + result.getMatchedCount() + ", modified " + result.getModifiedCount());
	}

}
