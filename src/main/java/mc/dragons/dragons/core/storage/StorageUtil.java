package mc.dragons.dragons.core.storage;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Provides useful utilities to use when marshaling between
 * game types and storage types.
 * 
 * @author Rick
 *
 */
public class StorageUtil {	
	
	public static Vector docToVec(Document doc) {
		return new Vector(doc.getDouble("x"), doc.getDouble("y"), doc.getDouble("z"));
	}
	
	public static Document vecToDoc(Vector vec) {
		return new Document()
				.append("x", vec.getX())
				.append("y", vec.getY())
				.append("z", vec.getZ());
	}

	public static Document locToDoc(Location loc) {
		Document doc = vecToDoc(loc.toVector());
		return doc.append("world", loc.getWorld().getName())
				.append("pitch", loc.getPitch())
				.append("yaw", loc.getYaw());
	}
	
	public static Location docToLoc(Document doc) {
		Vector vec = docToVec(doc);
		return vec.toLocation(Bukkit.getWorld(doc.getString("world")), (float)(double)doc.getDouble("yaw"), (float)(double)doc.getDouble("pitch"));
	}
	

}
