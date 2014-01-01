package net.coasterman10.Annihilation.enderfurnace;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.coasterman10.Annihilation.AnnihilationTeam;
import net.coasterman10.Annihilation.PlayerMeta;
import net.minecraft.server.v1_7_R1.EntityPlayer;

public class EnderFurnaceManager implements Listener {
	private HashMap<AnnihilationTeam, Location> furnaceLocations;
	private HashMap<String, VirtualFurnace> furnaces;

	public EnderFurnaceManager() {
		furnaceLocations = new HashMap<AnnihilationTeam, Location>();
		furnaces = new HashMap<String, VirtualFurnace>();
	}

	public void setFurnaceLocation(AnnihilationTeam team, Location loc) {
		furnaceLocations.put(team, loc);
	}

	@EventHandler
	public void onFurnaceOpen(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		if (b.getType() != Material.FURNACE
				|| e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Location loc = b.getLocation();
		Player player = e.getPlayer();
		AnnihilationTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == null || !furnaceLocations.containsKey(team))
			return;

		if (furnaceLocations.get(team).equals(loc)) {
			e.setCancelled(true);
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			handle.openFurnace(getFurnace(player));
			player.sendMessage(ChatColor.DARK_AQUA
					+ "This is your team's Ender Furnace. Any items you store or smelt here are safe from all other players.");
		}
	}

	private VirtualFurnace getFurnace(Player player) {
		if (!furnaces.containsKey(player.getName())) {
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			furnaces.put(player.getName(), new VirtualFurnace(handle));
		}
		return furnaces.get(player.getName());
	}
}
