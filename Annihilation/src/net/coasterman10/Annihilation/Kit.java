package net.coasterman10.Annihilation;

import java.util.List;

import net.coasterman10.Annihilation.listeners.SoulboundListener;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public enum Kit {
	CIVILIAN {
		{
			spawnItems.add(new ItemStack(Material.WOOD_SWORD));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			spawnItems.add(new ItemStack(Material.WORKBENCH));
		}
	},
	WARRIOR {
		{
			spawnItems.add(new ItemStack(Material.STONE_SWORD));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			spawnItems.add(new Potion(PotionType.INSTANT_HEAL, 1)
					.toItemStack(1));
			spawnItems.get(0).addEnchantment(Enchantment.KNOCKBACK, 1);
		}
	},
	MINER {
		{
			spawnItems.add(new ItemStack(Material.WOOD_SWORD));
			spawnItems.add(new ItemStack(Material.STONE_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			spawnItems.add(new ItemStack(Material.FURNACE));
			spawnItems.add(new ItemStack(Material.COAL, 4));
			spawnItems.get(1).addEnchantment(Enchantment.DIG_SPEED, 1);
		}
	};

	List<ItemStack> spawnItems;
	ItemStack[] spawnArmor = new ItemStack[] {
			new ItemStack(Material.LEATHER_HELMET),
			new ItemStack(Material.LEATHER_CHESTPLATE),
			new ItemStack(Material.LEATHER_LEGGINGS),
			new ItemStack(Material.LEATHER_BOOTS) };

	public static Kit getKit(String name) {
		for (Kit type : values()) {
			if (type.name().equalsIgnoreCase(name))
				return type;
		}
		return null;
	}

	public void give(Player recipient, AnnihilationTeam team) {
		PlayerInventory inv = recipient.getInventory();
		inv.clear();

		for (ItemStack item : spawnItems) {
			ItemStack toGive = item.clone();
			SoulboundListener.soulbind(toGive);
			inv.addItem(toGive);
		}

		inv.setArmorContents(spawnArmor);
		colorizeArmor(inv, getTeamColor(team));

		for (ItemStack armor : inv.getArmorContents())
			SoulboundListener.soulbind(armor);
	}

	private Color getTeamColor(AnnihilationTeam team) {
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
			return Color.WHITE;
		}
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

	public String getName() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}

	public boolean isOwnedBy(Player player) {
		return true;
	}
}
