package net.coasterman10.Annihilation.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class MinerKit extends AbstractKit {
	private static final List<ItemStack> spawnItems = new ArrayList<ItemStack>();
	private static final List<String> description = new ArrayList<String>();

	static {
		spawnItems.add(new ItemStack(Material.WOOD_SWORD));
		ItemStack pick = new ItemStack(Material.STONE_PICKAXE);
		pick.addEnchantment(Enchantment.DIG_SPEED, 1);
		spawnItems.add(pick);
		spawnItems.add(new ItemStack(Material.STONE_PICKAXE));
		spawnItems.add(new ItemStack(Material.WOOD_AXE));
		spawnItems.add(new ItemStack(Material.FURNACE));
		spawnItems.add(new ItemStack(Material.COAL, 4));
		spawnItems.add(new ItemStack(Material.COMPASS));
	}

	@Override
	public String getName() {
		return "Miner";
	}

	@Override
	public List<String> getDescription() {
		return description;
	}

	@Override
	protected List<ItemStack> getSpawnItems() {
		return spawnItems;
	}
}
