package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.maps.MapLoader;
import net.coasterman10.Annihilation.maps.VoidGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand implements CommandExecutor {
	private MapLoader loader;
	private Annihilation plugin;

	public MapCommand(Annihilation plugin, MapLoader loader) {
		this.plugin = plugin;
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
					sender.sendMessage(ChatColor.GREEN + "Teleporting...");
					World w = Bukkit.getWorld(args[1]);
					Location loc = w.getSpawnLocation();
					loc.setY(w.getHighestBlockYAt(loc));
					((Player) sender).teleport(loc);
				}
			}
			if (args[0].equalsIgnoreCase("save")) {
				if (Bukkit.getWorld(args[1]) != null) {
					Bukkit.getWorld(args[1]).save();
					final CommandSender s = sender;
					final String mapName = args[1];
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							s.sendMessage(ChatColor.GREEN + "Map " + mapName
									+ " saved.");
							loader.saveMap(mapName);
						}
					}, 40L);
				}
			}
		}
		return true;
	}

}
