package mc.dragons.core.util;

import static mc.dragons.core.util.MathUtil.round;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
	
	public static String entityToString(Entity e) {
		return e.getType() + " " + e + " (#" + e.getEntityId() + ")";
	}
	
	public static String concatArgs(String[] args, int startIndex, int endIndex) {
		if(endIndex <= startIndex) return "";
		if(startIndex >= args.length) return ""; 
		String result = "";
		for(int i = startIndex; i < Math.min(endIndex, args.length); i++) {
			result += args[i] + " ";
		}
		return result.substring(0, result.length() - 1);
	}
	
	public static String concatArgs(String[] args, int startIndex) {
		return concatArgs(args, startIndex, args.length);
	}
	
	public static int getFlagIndex(String[] args, String flag, int startIndex) {
		for(int i = startIndex; i < args.length; i++) {
			if(args[i].equalsIgnoreCase(flag)) {
				return i;
			}
		}
		return -1;
	}
	
	public static long parseTimespanToSeconds(String timespan) {
		if(timespan.equals("")) return -1L;
		long timespanSeconds = 0L;
		int buffer = 0;
		char[] chars = timespan.toCharArray();
		for(char ch : chars) {
			switch(ch) {
			case 'y':
				timespanSeconds += 60 * 60 * 24 * 365 * buffer;
				buffer = 0;
				break;
			case 'w':
				timespanSeconds += 60 * 60 * 24 * 7 * buffer;
				buffer = 0;
				break;
			case 'd':
				timespanSeconds += 60 * 60 * 24 * buffer;
				buffer = 0;
				break;
			case 'h':
				timespanSeconds += 60 * 60 * buffer;
				buffer = 0;
				break;
			case 'm':
				timespanSeconds += 60 * buffer;
				buffer = 0;
				break;
			case 's':
				timespanSeconds += buffer;
				buffer = 0;
				break;
			default:
				buffer *= 10;
				buffer += Integer.parseInt(new String(new char[] { ch }));
			}
		}
		return timespanSeconds;
	}
	
	public static <T> String parseList(List<T> list) {
		return parseList(list, ", ");
	}
	
	public static <T> String parseList(List<T> list, String separator) {
		return list.stream().map(elem -> elem.toString()).collect(Collectors.joining(separator));
	}
	
	public static <T> String parseList(T[] array) {
		return parseList(array, ", ");
	}
	
	public static <T> String parseList(T[] array, String separator) {
		return parseList(Arrays.asList(array), separator);
	}
	
	public static Material parseMaterialType(CommandSender sender, String str) {
		Material type = null;
		try {
			type = Material.valueOf(str.toUpperCase());
		}
		catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "Invalid material type! For a full list, see " + ChatColor.UNDERLINE + "https://papermc.io/javadocs/paper/1.12/org/bukkit/Material.html");
		}
		return type;
	}
	
	public static EntityType parseEntityType(CommandSender sender, String str) {
		EntityType type = null;
		try {
			type = EntityType.valueOf(str.toUpperCase());
		}
		catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "Invalid entity type! For a full list, see " + ChatColor.UNDERLINE + "https://papermc.io/javadocs/paper/1.12/org/bukkit/entity/EntityType.html");
		}
		return type;
	}
}
