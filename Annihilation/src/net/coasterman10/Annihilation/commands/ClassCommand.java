package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.kits.KitManager;
import net.coasterman10.Annihilation.kits.KitType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {
	private final KitManager kitManager;

	public ClassCommand(Annihilation plugin, KitManager kitManager) {
		this.kitManager = kitManager;
		plugin.getCommand("class").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "Classes pertain only to players");
		} else {
			if (args.length == 0) {
				listKits((Player) sender);
			} else {
				KitType kit = KitType.getKitType(args[0]);
				if (kit == kitManager.getKit((Player) sender)) {
					sender.sendMessage(ChatColor.DARK_AQUA
							+ "You are already a "
							+ kit.getKitClass().getName());
				} else if (kit != null) {
					kitManager.setKit((Player) sender, kit);
					((Player) sender).setHealth(0.0);
				} else {
					sender.sendMessage("\"" + args[0]
							+ "\" is not a valid class.");
					listKits((Player) sender);
				}
			}
		}
		return false;
	}

	private void listKits(Player player) {
		String GRAY = ChatColor.GRAY.toString();
		String DARK_AQUA = ChatColor.DARK_AQUA.toString();
		player.sendMessage(GRAY + "==========[ " + DARK_AQUA + "Classes" + GRAY
				+ " ]==========");
		for (KitType kit : KitType.values()) {
			String color;
			if (kitManager.hasKit(player, kit))
				color = ChatColor.GREEN.toString();
			else
				color = ChatColor.RED.toString();
			if (kitManager.getKit(player) == kit)
				player.sendMessage(color + kit.getKitClass().getName() + GRAY
						+ " - Selected");
			else
				player.sendMessage(color + kit.getKitClass().getName());
		}
		player.sendMessage(GRAY + "=========================");
	}
}
