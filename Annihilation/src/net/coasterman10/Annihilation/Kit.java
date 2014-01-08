package net.coasterman10.Annihilation;

import java.util.ArrayList;
import java.util.List;

import net.coasterman10.Annihilation.listeners.SoulboundListener;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
	ARCHER {
		{
			spawnItems.add(new ItemStack(Material.WOOD_SWORD));
			spawnItems.add(new ItemStack(Material.BOW));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			spawnItems.add(new ItemStack(Material.WOOD_SPADE));
			spawnItems.add(new Potion(PotionType.INSTANT_HEAL, 1)
					.toItemStack(1));
			spawnItems.add(new ItemStack(Material.ARROW, 16));
			spawnItems.get(1).addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		}
	},
	SCOUT {
		{
			spawnItems.add(new ItemStack(Material.GOLD_SWORD));
			spawnItems.add(new ItemStack(Material.FISHING_ROD));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			ItemMeta meta = spawnItems.get(1).getItemMeta();
			meta.setDisplayName("Grapple");
			spawnItems.get(1).setItemMeta(meta);
		}
	},
	BERSERKER {
		{
			spawnItems.add(new ItemStack(Material.STONE_SWORD));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			spawnItems.add(new Potion(PotionType.INSTANT_HEAL, 1)
					.toItemStack(1));
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
	},
	LUMBERJACK {
		{
			spawnItems.add(new ItemStack(Material.WOOD_SWORD));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.STONE_AXE));
			spawnItems.get(2).addEnchantment(Enchantment.DIG_SPEED, 1);
		}
	},
	OPERATIVE {
		{
			spawnItems.add(new ItemStack(Material.WOOD_SWORD));
			spawnItems.add(new ItemStack(Material.WOOD_PICKAXE));
			spawnItems.add(new ItemStack(Material.WOOD_AXE));
			spawnItems.add(new ItemStack(Material.SOUL_SAND));
			ItemMeta meta = spawnItems.get(3).getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Return Point");
			spawnItems.get(3).setItemMeta(meta);
		}
	};

	List<ItemStack> spawnItems = new ArrayList<ItemStack>();
	ItemStack[] spawnArmor = new ItemStack[] {
			new ItemStack(Material.LEATHER_BOOTS),
			new ItemStack(Material.LEATHER_LEGGINGS),
			new ItemStack(Material.LEATHER_CHESTPLATE),
			new ItemStack(Material.LEATHER_HELMET) };

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

		recipient.removePotionEffect(PotionEffectType.SPEED);

		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta compassMeta = compass.getItemMeta();
		compassMeta.setDisplayName(team.color() + "Pointing to "
				+ team.toString() + " Nexus");
		compass.setItemMeta(compassMeta);
		SoulboundListener.soulbind(compass);

		inv.addItem(compass);
		recipient.setCompassTarget(team.getNexus().getLocation());

		inv.setArmorContents(spawnArmor);
		colorizeArmor(inv, getTeamColor(team));

		for (ItemStack armor : inv.getArmorContents())
			SoulboundListener.soulbind(armor);

		if (this == SCOUT)
			addScoutParticles(recipient);

		if (this == BERSERKER)
			recipient.setMaxHealth(14);
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

	public void addScoutParticles(Player p) {
		if (this != SCOUT)
			return;

		for (ItemStack stack : p.getInventory().getArmorContents()) {
			if (!stack.getType().name().toLowerCase().contains("leather")
					&& !stack.getType().name().toLowerCase().contains("chain")) {
				p.removePotionEffect(PotionEffectType.SPEED);
				p.sendMessage(ChatColor.DARK_AQUA
						+ "That armor is too heavy for you to move quickly");
				return;
			}
		}
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
				Integer.MAX_VALUE, 0, true), true);
	}
}
