package net.coasterman10.Annihilation.kits;

import java.util.List;

import net.coasterman10.Annihilation.listeners.SoulboundListener;
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
		for (ItemStack item : spawnItems) {
			ItemStack toGive = item.clone();
			SoulboundListener.soulbind(toGive);
			inv.addItem(toGive);
		}
		
		inv.setArmorContents(getSpawnArmor());
		colorizeArmor(inv, getTeamColor(team));
		
		for (ItemStack armor : inv.getArmorContents())
			SoulboundListener.soulbind(armor);
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

	private void colorizeArmor(PlayerInventory inv, Color color) {
		for (ItemStack item : inv.getArmorContents()) {
			if (item.getItemMeta() instanceof LeatherArmorMeta) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(color);
				item.setItemMeta(meta);
			}
		}
	}

	private Color getTeamColor(TeamName team) {
		switch (team) {
		case RED:
			return Color.RED;
		case YELLOW:
			return Color.YELLOW;
		case GREEN:
			return Color.GREEN;
		case BLUE:
			return Color.BLUE;
		default:
			return Color.PURPLE;
		}
	}
}
