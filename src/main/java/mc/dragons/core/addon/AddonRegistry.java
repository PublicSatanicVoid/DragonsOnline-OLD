package mc.dragons.core.addon;

import java.util.ArrayList;
import java.util.List;

public class AddonRegistry {
	private List<Addon> addons;
	
	public AddonRegistry() {
		addons = new ArrayList<>();
	}
	
	public void register(Addon addon) {
		addons.add(addon);
	}
	
	public Addon getAddonByName(String name) {
		for(Addon addon : addons) {
			if(addon.getName().equalsIgnoreCase(name)) {
				return addon;
			}
		}
		return null;
	}
	
	public List<Addon> getAllAddons() {
		return addons;
	}
}
