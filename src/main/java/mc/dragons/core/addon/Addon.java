package mc.dragons.core.addon;

import java.util.logging.Logger;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;

public interface Addon {
	static final Logger LOGGER = Dragons.getInstance().getLogger();
	
	public String getName();
	public AddonType getType();
	public void initialize(GameObject gameObject);
}
