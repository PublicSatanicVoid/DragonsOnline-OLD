package mc.dragons.core.storage.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;

public class SystemProfile {
	public static class SystemProfileFlags {
		public static enum SystemProfileFlag {
			BUILD,
			WORLDEDIT,
			CMD,
			MODERATION,
			GM_ITEM,
			GM_NPC,
			GM_QUEST,
			GM_REGION,
			GM_FLOOR,
			GM_DELETE;
			
			public String getName() {
				return toString().toLowerCase();
			}
			
			public static SystemProfileFlag parse(String str) {
				for(SystemProfileFlag flag : values()) {
					if(flag.getName().equalsIgnoreCase(str)) {
						return flag;
					}
				}
				return null;
			}
		};
		
		private Map<SystemProfileFlag, Boolean> flags;
		
		public static String flagToAccess(boolean flag) {
			return flag ? "YES" : "NO";
		}
		
		public static Document emptyFlagsDocument() {
			Document document = new Document();
			for(SystemProfileFlag flag : SystemProfileFlag.values()) {
				document.append(flag.toString(), false);
			}
			return document;
		}
		
		public SystemProfileFlags(Document flags) {
			this.flags = new HashMap<>();
			for(Entry<String, Object> entry : flags.entrySet()) {
				this.flags.put(SystemProfileFlag.valueOf(entry.getKey()), (boolean) entry.getValue());
			}
		}
		
		public boolean hasFlag(SystemProfileFlag flag) {
			return flags.getOrDefault(flag, false);
		}
		
		public void setLocalFlag(SystemProfileFlag flag, boolean value) {
			flags.put(flag, value);
		}
		
		@Override
		public String toString() {
			String result = "";
			for(Entry<SystemProfileFlag, Boolean> flag : flags.entrySet()) {
				result += flag.getKey().getName() + "(" + flagToAccess(flag.getValue()) + ") ";
			}
			return result.trim();
		}
	}
	
	private String profileName;
	private String passwordHash;
	private PermissionLevel maxPermissionLevel;
	private SystemProfileFlags flags;
	private boolean active;
	private User currentUser;
	
	SystemProfile(User currentUser, String profileName, String passwordHash, PermissionLevel maxPermissionLevel, SystemProfileFlags flags, boolean active) {
		this.profileName = profileName;
		this.passwordHash = passwordHash;
		this.maxPermissionLevel = maxPermissionLevel;
		this.flags = flags;
		this.active = active;
		this.currentUser = currentUser;
	}
	
	public String getProfileName() {
		return profileName;
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public void setLocalPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	public PermissionLevel getMaxPermissionLevel() {
		return maxPermissionLevel;
	}
	
	public void setLocalMaxPermissionLevel(PermissionLevel level) {
		this.maxPermissionLevel = level;
	}
	
	public SystemProfileFlags getFlags() {
		return flags;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setLocalActive(boolean active) {
		this.active = active;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}
	
	public void setLocalCurrentUser(User user) {
		currentUser = user;
	}
}
