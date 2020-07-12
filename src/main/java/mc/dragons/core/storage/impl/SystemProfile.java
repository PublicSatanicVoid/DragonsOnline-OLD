package mc.dragons.core.storage.impl;

import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;

public class SystemProfile {
	private String profileName;
	private String passwordHash;
	private PermissionLevel maxPermissionLevel;
	private User currentUser;
	
	SystemProfile(User currentUser, String profileName, String passwordHash, PermissionLevel maxPermissionLevel) {
		this.profileName = profileName;
		this.passwordHash = passwordHash;
		this.maxPermissionLevel = maxPermissionLevel;
		this.currentUser = currentUser;
	}
	
	public String getProfileName() {
		return profileName;
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public PermissionLevel getMaxPermissionLevel() {
		return maxPermissionLevel;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}
}
