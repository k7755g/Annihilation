package net.coasterman10.Annihilation.kits;

import java.util.HashMap;
import java.util.Map;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.commands.ClassCommand;

import org.bukkit.entity.Player;

public class KitManager {
	private static final Map<String, KitType> playerKits = new HashMap<String, KitType>();

	public KitManager(Annihilation plugin) {
		new ClassCommand(plugin, this);
	}
	
	public KitType getKit(Player p) {
		if (playerKits.containsKey(p.getName())) {
			return playerKits.get(p.getName());
		} else {
			playerKits.put(p.getName(), KitType.CIVILIAN);
			return KitType.CIVILIAN;
		}
	}

	public void setKit(Player p, KitType kit) {
		if (kit == null)
			playerKits.put(p.getName(), KitType.CIVILIAN);
		else
			playerKits.put(p.getName(), kit);
	}

	public boolean hasKit(Player player, KitType kit) {
		// TODO Return false if the player doesn't have the kit
		return true;
	}
}
