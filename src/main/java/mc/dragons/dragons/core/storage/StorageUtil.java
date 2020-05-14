package mc.dragons.dragons.core.storage;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Provides useful utilities to use when marshaling between
 * game types and storage types.
 * 
 * @author Rick
 *
 */
public class StorageUtil {
	public static Document locToDoc(Location loc) {
		return new Document("world", loc.getWorld().getName())
				.append("x", loc.getX())
				.append("y", loc.getY())
				.append("z", loc.getZ())
				.append("pitch", loc.getPitch())
				.append("yaw", loc.getYaw());
	}
	
	public static Location docToLoc(Document doc) {
		Location loc = Bukkit.getWorld(doc.getString("world")).getSpawnLocation();
		loc.setX(doc.getDouble("x"));
		loc.setY(doc.getDouble("y"));
		loc.setZ(doc.getDouble("z"));
		loc.setPitch((float)(double)doc.getDouble("pitch"));
		loc.setYaw((float)(double)doc.getDouble("yaw"));
		return loc;
	}
}
