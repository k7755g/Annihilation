package net.coasterman10.Annihilation.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CivilianKit extends AbstractKit {
	private static final List<ItemStack> spawnItems = new ArrayList<ItemStack>();
	private static final List<String> description = new ArrayList<String>();
	
	static {
		spawnItems.add(new ItemStack(Material.WOOD_SWORD));
		spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
		spawnItems.add(new ItemStack(Material.WOOD_AXE));
		spawnItems.add(new ItemStack(Material.WORKBENCH));
		spawnItems.add(new ItemStack(Material.COMPASS));
		
		String AQUA = ChatColor.AQUA.toString();
		
		description.add(AQUA + "You are the backbone.");
		description.add("");
		description.add(AQUA + "Fuel all facets of the");
		description.add(AQUA + "war machine with your");
		description.add(AQUA + "set of wooden tools and");
		description.add(AQUA + "prepare for battle!");
	}

	@Override
	public String getName() {
		return "Civilian";
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
