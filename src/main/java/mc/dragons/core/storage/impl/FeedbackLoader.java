package mc.dragons.core.storage.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class FeedbackLoader {
	public static class FeedbackEntry {
		private UUID uuid;
		private String from;
		private String feedback;
		
		public FeedbackEntry(UUID uuid, String from, String feedback) {
			this.uuid = uuid;
			this.from = from;
			this.feedback = feedback;
		}
		
		public UUID getUUID() {
			return uuid;
		}
		
		public String getFrom() {
			return from;
		}
		
		public String getFeedback() {
			return feedback;
		}
	}
	
	private static MongoDatabase database;
	private static MongoCollection<Document> feedbackCollection;
	
	static {
		database = MongoConfig.getDatabase();
		feedbackCollection = database.getCollection(MongoConfig.FEEDBACK_COLLECTION);
	}
	
	public static List<FeedbackEntry> getUnreadFeedback() {
		List<FeedbackEntry> result = new ArrayList<>();
		
		FindIterable<Document> dbResults = feedbackCollection.find(new Document("read", false));
		for(Document d : dbResults) {
			result.add(new FeedbackEntry(d.get("_id", UUID.class), d.getString("from"), d.getString("feedback")));
		}
		
		return result;
	}
	
	public static void deleteFeedback(UUID uuid) {
		feedbackCollection.deleteOne(new Document("_id", uuid));
	}
	
	public static void markRead(UUID uuid, boolean read) {
		feedbackCollection.updateOne(new Document("_id", uuid), new Document("$set", new Document("read", read)));
	}
	
	public static void addFeedback(String from, String feedback) {
		feedbackCollection.insertOne(new Document("_id", UUID.randomUUID()).append("from", from).append("feedback", feedback).append("read", false));
	}
}
