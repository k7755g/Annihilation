package net.coasterman10.Annihilation.kits;

import java.util.List;

import net.coasterman10.Annihilation.teams.TeamName;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public abstract class AbstractKit {
	public void give(Player recipient, TeamName team) {
		PlayerInventory inv = recipient.getInventory();
		inv.clear();
		List<ItemStack> spawnItems = getSpawnItems();
		for (ItemStack item : spawnItems)
			inv.addItem(item);
		inv.setArmorContents(getSpawnArmor());
		if (team != null) {
			for (ItemStack item : inv.getArmorContents()) {
				if (item.getItemMeta() instanceof LeatherArmorMeta) {
					LeatherArmorMeta meta = (LeatherArmorMeta) item
							.getItemMeta();
					Color color;
					switch (team) {
					case RED:
						color = Color.fromRGB(0xFF0000);
						break;
					case YELLOW:
						color = Color.fromRGB(0xFFFF00);
						break;
					case GREEN:
						color = Color.fromRGB(0x008000);
						break;
					case BLUE:
						color = Color.fromRGB(0x0000FF);
						break;
					default:
						color = Color.fromRGB(255, 255, 255);
					}
					meta.setColor(color);
					item.setItemMeta(meta);
				}
			}
		}
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
