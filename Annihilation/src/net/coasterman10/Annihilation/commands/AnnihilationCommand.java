package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Annihilation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AnnihilationCommand implements CommandExecutor {
	private Annihilation plugin;

	public AnnihilationCommand(Annihilation plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		String prefix = "§3[Annihilation] §7";
		
		if (args.length == 0) {
			sender.sendMessage(prefix + "§fAnnihilation v" + plugin.getDescription().getVersion() + " by coasterman10 & stuntguy3000.");
			sender.sendMessage(prefix + "§6Download Annihilation at §ehttp://dev.bukkit.org/bukkit-plugins/anni/");
			sender.sendMessage(prefix + "§7Command Help:");
			sender.sendMessage(prefix + "§7/anni §8- §7Show plugin information");
			sender.sendMessage(prefix + "§7/anni start§8- §7Begin the game");
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("start")) {
				if (sender.hasPermission("annihilation.command.start")) {
					if (!plugin.startTimer()) {
						sender.sendMessage(prefix + "§cThe game has already started");
					} else {
						sender.sendMessage(prefix + "§aThe game has been started.");
					}
				} else sender.sendMessage(prefix + "§cYou cannot use this command!");
			}
		}
		return false;
	}
}
