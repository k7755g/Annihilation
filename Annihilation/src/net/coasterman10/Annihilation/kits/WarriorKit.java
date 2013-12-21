package net.coasterman10.Annihilation.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class WarriorKit extends AbstractKit{
	private static final List<ItemStack> spawnItems = new ArrayList<ItemStack>();
	private static final List<String> description = new ArrayList<String>();
	
	static {
		ItemStack sword = new ItemStack(Material.STONE_SWORD);
		sword.addEnchantment(Enchantment.KNOCKBACK, 1);
		Potion potion = new Potion(PotionType.INSTANT_HEAL, 2);
		spawnItems.add(sword);
		spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
		spawnItems.add(new ItemStack(Material.WOOD_AXE));
		spawnItems.add(potion.toItemStack(1));
		spawnItems.add(new ItemStack(Material.COMPASS));
		
		String AQUA = ChatColor.AQUA.toString();
		
		description.add(AQUA + "You are the sword.");
		description.add("");
		description.add(AQUA + "You deal +1 damage with");
		description.add(AQUA + "swords and axes.");
		description.add("");
		description.add(AQUA + "Spawn with a knockback stone");
		description.add(AQUA + "sword and a health potion");
		description.add(AQUA + "which enable you to immediately");
		description.add(AQUA + "move on the enemy and attack!");
	}

	@Override
	public String getName() {
		return "Warrior";
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
