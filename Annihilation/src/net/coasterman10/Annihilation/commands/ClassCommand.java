package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Kit;
import net.coasterman10.Annihilation.PlayerMeta;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "Classes pertain only to players");
		} else {
			Player player = (Player) sender;
			if (args.length == 0) {
				listKits(player);
			} else {
				Kit kit = Kit.getKit(args[0]);
				PlayerMeta meta = PlayerMeta.getMeta(player);
				if (meta.getKit() == kit) {
					player.sendMessage(ChatColor.DARK_AQUA
							+ "You are already a " + kit.getName());
				} else if (kit != null) {
					meta.setKit(kit);
					player.setHealth(0.0);
				} else {
					player.sendMessage("\"" + args[0]
							+ "\" is not a valid class.");
					listKits(player);
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
		for (Kit kit : Kit.values()) {
			String color;
			if (kit.isOwnedBy(player))
				color = ChatColor.GREEN.toString();
			else
				color = ChatColor.RED.toString();
			if (PlayerMeta.getMeta(player).getKit() == kit)
				player.sendMessage(color + kit.getName() + GRAY + " - Selected");
			else
				player.sendMessage(color + kit.getName());
		}
		player.sendMessage(GRAY + "=========================");
	}
}
