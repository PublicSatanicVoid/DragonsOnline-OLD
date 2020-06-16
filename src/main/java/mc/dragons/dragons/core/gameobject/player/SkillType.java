package mc.dragons.dragons.core.gameobject.player;

/**
 * Represents a specific player or NPC ability.
 * 
 * Skills can be leveled up and used to achieve
 * certain tasks more quickly or provide gameplay
 * advantages.
 * 
 * @author Rick
 *
 */
public enum SkillType {
	MELEE("Melee"),
	ARCHERY("Archery"),
	MINING("Mining"),
	FISHING("Fishing"),
	COOKING("Cooking"),
	DUAL_WIELD("Dual Wield"),
	RIDING("Riding"),
	DEFENSE("Defense");
	
	private String friendlyName;
	
	private SkillType(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
}
