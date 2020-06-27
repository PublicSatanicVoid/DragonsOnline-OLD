package mc.dragons.core.util;

import java.util.Map.Entry;

import static mc.dragons.core.util.MathUtil.round;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class StringUtil {
	
	private StringUtil() {}
	
	
	/**
	 * String representation of xyz-coordinates of a location.
	 * 
	 * @param loc
	 * @return
	 */
	public static String locToString(Location loc) {
		return round(loc.getX()) + ", " + round(loc.getY()) + ", " + round(loc.getZ());
	}
	
	/**
	 * String representation of a vector.
	 * 
	 * @param vec
	 * @return
	 */
	public static String vecToString(Vector vec) {
		return round(vec.getX()) + ", " + round(vec.getY()) + ", " + round(vec.getZ());
	}
	
	/**
	 * One-level-deep string representation of a document.
	 * 
	 * @param doc
	 * @return
	 */
	public static String docToString(Document doc) {
		String result = "";
		for(Entry<String, Object> entry : doc.entrySet()) {
			result += entry.getKey() + "=" + entry.getValue().toString() + "; ";
		}
		if(result.equals("")) return "";
		return result.substring(0, result.length() - 1);
	}
	
	public static String concatArgs(String[] args, int startIndex) {
		String result = "";
		for(int i = startIndex; i < args.length; i++) {
			result += args[i] + " ";
		}
		return result.substring(0, result.length() - 1);
	}
}