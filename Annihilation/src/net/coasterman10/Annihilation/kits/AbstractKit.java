package net.coasterman10.Annihilation.kits;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public abstract class AbstractKit {
	public void give(Player recipient) {
		PlayerInventory inv = recipient.getInventory();
		inv.clear();
		List<ItemStack> spawnItems = getSpawnItems();
		for (ItemStack item : spawnItems)
			inv.addItem(item);
		inv.setArmorContents(getSpawnArmor());
	}

	public abstract String getName();

	public abstract List<String> getDescription();

	protected abstract List<ItemStack> getSpawnItems();

	protected ItemStack[] getSpawnArmor() {
		ItemStack[] armor = new ItemStack[4];
		armor[0] = new ItemStack(Material.LEATHER_BOOTS);
		armor[1] = new ItemStack(Material.LEATHER_LEGGINGS);
		armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
		armor[3] = new ItemStack(Material.LEATHER_HELMET);
		return armor;
	}
}
