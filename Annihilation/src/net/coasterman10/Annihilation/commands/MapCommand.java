package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.maps.MapLoader;
import net.coasterman10.Annihilation.maps.VoidGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand implements CommandExecutor {
	private MapLoader loader;

	public MapCommand(MapLoader loader) {
		this.loader = loader;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("edit")) {
				loader.loadMap(args[1]);
				WorldCreator wc = new WorldCreator(args[1]);
				wc.generator(new VoidGenerator());
				Bukkit.createWorld(wc);
				sender.sendMessage(ChatColor.GREEN + "Map " + args[1]
						+ " loaded for editing.");
				if (sender instanceof Player) {
					sender.sendMessage(ChatColor.GREEN +" Teleporting...");
					((Player) sender).teleport(Bukkit.getWorld(args[1])
							.getSpawnLocation());
				}
			}
			if (args[0].equalsIgnoreCase("save")) {
				if (Bukkit.getWorld(args[1]) != null) {
					Bukkit.getWorld(args[1]).save();
					loader.saveMap(args[1]);
					sender.sendMessage(ChatColor.GREEN + "Map " + args[1] + " saved.");
				}
			}
		}
		return true;
	}

}
