package mc.dragons.core.gameobject.user;

/**
 * Player permission levels. These determine
 * ability to use certain commands / features,
 * and are independent of
 * {@link mc.dragons.core.gameobject.user.Rank}
 * 
 * @author Rick
 *
 */
public enum PermissionLevel {
	
	USER,
	TESTER,
	MOD,
	GM,
	ADMIN,
	SYSOP;


}
