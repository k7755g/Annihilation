package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Annihilation;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class WorldListener implements Listener {
    
	private Annihilation plugin;
	
	public WorldListener(Annihilation plugin) {
	plugin.getServer().getPluginManager().registerEvents(this, plugin);
	this.plugin = plugin;
    }

    @EventHandler
    public void onWaterFlow(BlockFromToEvent e) {
	if (plugin.isEmptyColumn(e.getToBlock().getLocation()))
	    e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
	if (plugin.isEmptyColumn(e.getBlock().getLocation()))
	    e.setCancelled(true);
    }
}
