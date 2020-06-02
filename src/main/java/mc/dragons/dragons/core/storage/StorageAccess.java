package mc.dragons.dragons.core.storage;

import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;

/**
 * Stores and manages data for {@link mc.dragons.dragons.core.gameobject.GameObject}s.
 * 
 * <p>All persistent data for a GameObject needs to be stored in its StorageAccess.
 * 
 * <p>The StorageAccess takes care of saving data.
 * 
 * @author Rick
 *
 */
public interface StorageAccess {
	public void set(String key, Object value);
	public void update(Document document);
	public Object get(String key);
	public Set<Entry<String, Object>> getAll();
	public Document getDocument();
	public Identifier getIdentifier();
	
}
