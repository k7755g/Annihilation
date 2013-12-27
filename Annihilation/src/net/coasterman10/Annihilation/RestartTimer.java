package net.coasterman10.Annihilation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.coasterman10.Annihilation.bar.BarManager;

public class RestartTimer {
	private final BarManager bar;
	private final Annihilation plugin;
	private long time;
	private long delay;

	public RestartTimer(Annihilation plugin, final long delay) {
		this.plugin = plugin;
		this.delay = delay;
		bar = new BarManager(plugin);
	}

	public void start(final long gameTime) {
		time = delay;
		final String totalTime = PhaseTimer.timeString(gameTime);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
				new BukkitRunnable() {
					@Override
					public void run() {
						if (time <= 0) {
							plugin.reset();
							cancel();
							return;
						}
						String message = "Total time: " + totalTime
								+ " | Restarting in " + time;
						float percent = (float) time / (float) delay;
						for (Player p : Bukkit.getOnlinePlayers())
							bar.setMessageAndPercent(p, message, percent);
						time--;
					}
				}, 0L, 20L);
	}
}
