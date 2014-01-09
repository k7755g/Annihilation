package net.coasterman10.Annihilation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.coasterman10.Annihilation.bar.BarUtil;

public class RestartTimer {
	private final Annihilation plugin;
	private long time;
	private long delay;
	private int taskID;

	public RestartTimer(Annihilation plugin, final long delay) {
		this.plugin = plugin;
		this.delay = delay;
	}

	public void start(final long gameTime) {
		time = delay;
		final String totalTime = PhaseTimer.timeString(gameTime);
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
				new Runnable() {
					@Override
					public void run() {
						if (time <= 0) {
							plugin.reset();
							stop();
							return;
						}
						String message = "§7Total time: §f" + totalTime
								+ " | " + ChatColor.GREEN + "Restarting in " + timeString(time);
						float percent = (float) time / (float) delay;
						for (Player p : Bukkit.getOnlinePlayers())
							BarUtil.setMessageAndPercent(p, message, percent);
						time--;
					}
				}, 0L, 20L);
	}

	private void stop() {
		Bukkit.getScheduler().cancelTask(taskID);
	}
	
	public static String timeString(long time) {
		long hours = time / 3600L;
		long minutes = (time - hours * 3600L) / 60L;
		long seconds = time - hours * 3600L - minutes * 60L;
		return String.format("§f%02d§7:§f%02d§7:§f%02d", hours, minutes, seconds);
	}
}
