package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Kit;
import net.coasterman10.Annihilation.PlayerMeta;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.util.Vector;

public class ClassAbilityListener implements Listener {
	@EventHandler
	public void onScoutGrapple(PlayerFishEvent e) {
		Player player = e.getPlayer();
		if (e.getCaught() != null || e.getState() == State.FISHING)
			return;
		if (PlayerMeta.getMeta(player).getKit() != Kit.SCOUT)
			return;
		Location hookLoc = e.getHook().getLocation();
		Vector velocity = hookLoc.subtract(player.getLocation()).toVector();
		velocity.divide(new Vector(5.0, 5.0, 5.0));
		velocity.setY(1.3);
		player.setVelocity(velocity);
	}
}
