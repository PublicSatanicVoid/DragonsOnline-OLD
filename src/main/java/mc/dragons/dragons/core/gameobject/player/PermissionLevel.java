package mc.dragons.dragons.core.gameobject.player;

/**
 * Player permission levels. These determine
 * ability to use certain commands / features,
 * and are independent of
 * {@link mc.dragons.dragons.core.gameobject.player.Rank}
 * 
 * @author Rick
 *
 */
public enum PermissionLevel {
	
	USER(0),
	MONITOR(1),
	GM(2),
	SYSOP(3);
		
	private int level;
	
	PermissionLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public static PermissionLevel fromInt(int level) {
		for(PermissionLevel plvl : values()) {
			if(plvl.getLevel() == level) {
				return plvl;
			}
		}
		return null;
	}
}
