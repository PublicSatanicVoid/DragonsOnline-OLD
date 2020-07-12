package mc.dragons.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class InventoryEventListeners implements Listener {

	private ItemLoader itemLoader;
	
	public InventoryEventListeners() {
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		User user = UserLoader.fromPlayer(player);
		user.debug("Inventory click event. Action=" + e.getAction() + ", clickType=" + e.getClick() + ", rawSlot=" + e.getSlot());
		if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getClick() == ClickType.LEFT) {
			user.debug("- Merge candidate");
			ItemStack mergeWith = player.getInventory().getItem(e.getSlot());
			ItemStack mergeFrom = e.getCursor();
			if(mergeWith == null || mergeFrom == null) {
				user.debug("- Cannot merge: one or more item stacks is null (with="+mergeWith+",from="+mergeFrom+")");
				return;
			}
			Item rpgMergeWith = ItemLoader.fromBukkit(mergeWith);
			Item rpgMergeFrom = ItemLoader.fromBukkit(mergeFrom);
			if(rpgMergeWith == null || rpgMergeFrom == null) {
				user.debug("- Cannot merge: one or more rpg items is null (with="+rpgMergeWith+",from="+rpgMergeFrom+")");
				return;
			}
			if(rpgMergeWith.getClassName().contentEquals(rpgMergeFrom.getClassName()) && !rpgMergeWith.isCustom() && !rpgMergeFrom.isCustom()) {
				user.debug("- Merge allowed");
				int quantity = rpgMergeWith.getItemStack().getAmount() + rpgMergeFrom.getItemStack().getAmount();
				if(quantity > 64) {
					rpgMergeFrom.setQuantity(quantity - 64);
					e.setCursor(rpgMergeFrom.getItemStack());
				}
				else {
					user.takeItem(rpgMergeFrom, rpgMergeFrom.getQuantity(), true, false);
					//e.getCursor().addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					e.setCursor(null);
				}
				rpgMergeWith.setQuantity(Math.min(quantity, 64));
				e.setCurrentItem(rpgMergeWith.getItemStack());
				user.debug("- Merge complete (total quantity=" + quantity + ")");
			}
		}
		
		if(e.getAction() == InventoryAction.PICKUP_HALF || e.getAction() == InventoryAction.PICKUP_SOME) {
			user.debug("- Split candidate");
			ItemStack splitFrom = e.getCurrentItem();
			Item rpgSplitFrom = ItemLoader.fromBukkit(splitFrom);
			user.debug("- from=" + rpgSplitFrom);
			if(rpgSplitFrom == null) {
				user.debug("- Cannot split: split from is null");
				return;
			}
			int quantity = splitFrom.getAmount();
			int fromQuantity = quantity / 2;
			int toQuantity = quantity / 2;
			if(quantity % 2 == 1) {
				fromQuantity++;
			}
			Item rpgSplitToCopy = itemLoader.registerNew(rpgSplitFrom);
			rpgSplitToCopy.setQuantity(toQuantity);
			rpgSplitToCopy.setItemStack(splitFrom.clone());
			rpgSplitFrom.setQuantity(fromQuantity);
			e.setCursor(rpgSplitToCopy.getItemStack());
			e.setCurrentItem(rpgSplitFrom.getItemStack());
			user.debug("- Split complete (fromQuantity=" + rpgSplitFrom.getQuantity() + "[" + splitFrom.getAmount() + "], toQuantity=" + rpgSplitToCopy.getQuantity() + "[" + rpgSplitToCopy.getItemStack().getAmount() + "])");
		}
	}
	
}
