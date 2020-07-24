package mc.dragons.core.storage.impl;

import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.storage.Identifier;
import mc.dragons.core.storage.StorageAccess;

public class LocalStorageAccess implements StorageAccess {

	private Document data;
	private Identifier id;
	
	public LocalStorageAccess(Identifier identifier, Document data) {
		id = identifier;
		this.data = data;
		this.data.putAll(id.getDocument());
	}
	
	public LocalStorageAccess(GameObjectType type, Document data) {
		id = new Identifier(type, UUID.randomUUID());
		this.data = data;
		this.data.putAll(id.getDocument());
	}
	
	@Override
	public void set(String key, Object value) {
		if(key.equals("type") || key.equals("_id")) {
			throw new IllegalArgumentException("Cannot modify type or UUID of storage access once instantiated");
		}
		data.append(key, value);
	}

	@Override
	public void update(Document document) {
		data.putAll(document);
	}

	@Override
	public Object get(String key) {
		return data.get(key);
	}

	@Override
	public Set<Entry<String, Object>> getAll() {
		return data.entrySet();
	}

	@Override
	public Document getDocument() {
		return data;
	}

	@Override
	public Identifier getIdentifier() {
		return id;
	}

}
