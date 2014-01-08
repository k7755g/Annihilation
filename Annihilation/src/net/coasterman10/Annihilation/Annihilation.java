package net.coasterman10.Annihilation;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import net.coasterman10.Annihilation.Updater.UpdateResult;
import net.coasterman10.Annihilation.api.GameStartEvent;
import net.coasterman10.Annihilation.api.PhaseChangeEvent;
import net.coasterman10.Annihilation.bar.BarUtil;
import net.coasterman10.Annihilation.chat.ChatListener;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.commands.AnnihilationCommand;
import net.coasterman10.Annihilation.commands.ClassCommand;
import net.coasterman10.Annihilation.commands.StatsCommand;
import net.coasterman10.Annihilation.commands.TeamCommand;
import net.coasterman10.Annihilation.commands.VoteCommand;
import net.coasterman10.Annihilation.listeners.ClassAbilityListener;
import net.coasterman10.Annihilation.listeners.CraftingListener;
import net.coasterman10.Annihilation.listeners.EnderChestListener;
import net.coasterman10.Annihilation.listeners.EnderFurnaceListener;
import net.coasterman10.Annihilation.listeners.PlayerListener;
import net.coasterman10.Annihilation.listeners.ResourceListener;
import net.coasterman10.Annihilation.listeners.SoulboundListener;
import net.coasterman10.Annihilation.listeners.WandListener;
import net.coasterman10.Annihilation.listeners.WorldListener;
import net.coasterman10.Annihilation.maps.MapManager;
import net.coasterman10.Annihilation.maps.VotingManager;
import net.coasterman10.Annihilation.stats.DatabaseHandler;
import net.coasterman10.Annihilation.stats.StatType;
import net.coasterman10.Annihilation.stats.StatsManager;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Annihilation extends JavaPlugin {
	private ConfigManager configManager;
	private VotingManager voting;
	private MapManager maps;
	private PhaseTimer timer;
	private ResourceListener resources;
	private EnderFurnaceListener enderFurnaces;
	private EnderChestListener enderChests;
	private StatsManager stats;
	private SignHandler sign;
	private DatabaseHandler db;
	public boolean useMysql = false;
	public boolean updateAvailable = false;
	public String newVersion;
	
	@Override
	public void onEnable() {
		 UpdateResult updateResult = null;
         Updater u = null;
         
         if (this.getConfig().getBoolean("autoUpdater"))
                 u = new Updater(this, 00000, this.getFile(), Updater.UpdateType.DEFAULT, true);
 
         if (u != null)
                 updateResult = u.getResult();
         
         if (updateResult != null) {
                 if (updateResult == UpdateResult.SUCCESS) {
                         updateAvailable = true;
                         newVersion = u.getLatestName();
                 }
         }
		
		configManager = new ConfigManager(this);
		configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml",
				"stats.yml");

		maps = new MapManager(this, configManager.getConfig("maps.yml"));

		Configuration shops = configManager.getConfig("shops.yml");
		new Shop(this, "Weapon", shops);
		new Shop(this, "Brewing", shops);

		stats = new StatsManager(this, configManager);
		resources = new ResourceListener(this);
		enderFurnaces = new EnderFurnaceListener();
		enderChests = new EnderChestListener();
		sign = new SignHandler(this);
		Configuration config = configManager.getConfig("config.yml");
		timer = new PhaseTimer(this, config.getInt("start-delay"),
				config.getInt("phase-period"));
		voting = new VotingManager(this);

		PluginManager pm = getServer().getPluginManager();

		sign.loadSigns();

		pm.registerEvents(resources, this);
		pm.registerEvents(enderFurnaces, this);
		pm.registerEvents(enderChests, this);
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new WorldListener(), this);
		pm.registerEvents(new SoulboundListener(), this);
		pm.registerEvents(new WandListener(this), this);
		pm.registerEvents(new CraftingListener(), this);
		pm.registerEvents(new ClassAbilityListener(this), this);

		getCommand("annihilation").setExecutor(new AnnihilationCommand(this));
		getCommand("class").setExecutor(new ClassCommand());
		getCommand("stats").setExecutor(new StatsCommand(stats));
		getCommand("team").setExecutor(new TeamCommand(this));
		getCommand("vote").setExecutor(new VoteCommand(voting));

		BarUtil.init(this);

		if (config.getString("stats").equalsIgnoreCase("sql"))
			useMysql = true;

		if (useMysql) {
			String host = config.getString("MySQL.host");
			Integer port = config.getInt("MySQL.port");
			String name = config.getString("MySQL.name");
			String user = config.getString("MySQL.user");
			String pass = config.getString("MySQL.pass");
			db = new DatabaseHandler(host, port, name, user, pass, this);

			db.query("CREATE TABLE IF NOT EXISTS `annihilation` ( `username` varchar(16) NOT NULL, "
					+ "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
					+ "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
					+ "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		} else
			db = new DatabaseHandler(this);

		for (AnnihilationTeam team : AnnihilationTeam.teams())
			ScoreboardUtil.registerTeam(team.name(), team.color());

		reset();

		ChatUtil.setRoman(getConfig().getBoolean("roman", false));
	}

	public boolean startTimer() {
		if (timer.isRunning())
			return false;

		timer.start();

		return true;
	}

	public void loadMap(final String map) {
		FileConfiguration config = configManager.getConfig("maps.yml");
		ConfigurationSection section = config.getConfigurationSection(map);

		World w = getServer().getWorld(map);

		for (AnnihilationTeam team : AnnihilationTeam.teams()) {
			String name = team.name().toLowerCase();
			if (section.contains("spawns." + name)) {
				for (String s : section.getStringList("spawns." + name))
					team.addSpawn(Util.parseLocation(getServer().getWorld(map),
							s));
			}
			if (section.contains("nexuses." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("nexuses." + name));
				team.loadNexus(loc, 75);
			}
			if (section.contains("furnaces." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("furnaces." + name));
				enderFurnaces.setFurnaceLocation(team, loc);
				loc.getBlock().setType(Material.FURNACE);
			}
			if (section.contains("enderchests." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("enderchests." + name));
				enderChests.setEnderChestLocation(team, loc);
				loc.getBlock().setType(Material.ENDER_CHEST);
			}
		}

		if (section.contains("diamonds")) {
			Set<Location> diamonds = new HashSet<Location>();
			for (String s : section.getStringList("diamonds"))
				diamonds.add(Util.parseLocation(w, s));
			resources.loadDiamonds(diamonds);
		}
	}

	public void startGame() {
		Bukkit.getPluginManager().callEvent(
				new GameStartEvent(maps.getCurrentMap()));

		ScoreboardUtil.setTitle(ChatColor.GOLD + "Map: "
				+ WordUtils.capitalize(voting.getWinner()));
		ScoreboardUtil.removeAllScores();
		for (AnnihilationTeam t : AnnihilationTeam.teams())
			ScoreboardUtil.setScore(t.coloredName() + " Nexus", t.getNexus()
					.getHealth());

		for (Player p : getServer().getOnlinePlayers()) {
			Util.sendPlayerToGame(p);
		}

		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (Player p : getServer().getOnlinePlayers()) {
					if (PlayerMeta.getMeta(p).getKit() == Kit.SCOUT) {
						PlayerMeta.getMeta(p).getKit().addScoutParticles(p);
					}
				}
			}
		}, 0L, 1200L);
	}

	public void advancePhase() {
		ChatUtil.phaseMessage(timer.getPhase());
		if (timer.getPhase() == 3)
			resources.spawnDiamonds();
		Bukkit.getPluginManager().callEvent(
				new PhaseChangeEvent(timer.getPhase()));

		getSignHandler().updateSigns(AnnihilationTeam.RED);
		getSignHandler().updateSigns(AnnihilationTeam.BLUE);
		getSignHandler().updateSigns(AnnihilationTeam.GREEN);
		getSignHandler().updateSigns(AnnihilationTeam.YELLOW);
	}

	public void onSecond() {
		long time = timer.getTime();

		if (time == -5L) {
			String winner = voting.getWinner();
			maps.selectMap(winner);
			getServer().broadcastMessage(
					ChatColor.GREEN + winner + " selected, loading...");
			loadMap(winner);

			voting.end();
		}

		if (time == 0L)
			startGame();
	}

	public int getPhase() {
		return timer.getPhase();
	}

	public MapManager getMapManager() {
		return maps;
	}

	public StatsManager getStatsManager() {
		return stats;
	}

	public DatabaseHandler getDatabaseHandler() {
		return db;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public int getPhaseDelay() {
		return configManager.getConfig("config.yml").getInt("phase-period");
	}

	public void log(String m, Level l) {
		getLogger().log(l, m);
	}

	public VotingManager getVotingManager() {
		return voting;
	}

	public void endGame(AnnihilationTeam winner) {
		if (winner == null)
			return;

		ChatUtil.winMessage(winner);
		timer.stop();

		for (Player p : getServer().getOnlinePlayers())
			if (PlayerMeta.getMeta(p).getTeam() == winner)
				stats.incrementStat(StatType.WINS, p);
		long restartDelay = configManager.getConfig("config.yml").getLong(
				"restart-delay");
		new RestartTimer(this, restartDelay).start(timer.getTime());
	}

	@SuppressWarnings("deprecation")
	public void reset() {
		ScoreboardUtil.showForPlayers(getServer().getOnlinePlayers());
		maps.reset();
		timer.reset();
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerMeta.getMeta(p).setTeam(AnnihilationTeam.NONE);
			PlayerInventory inv = p.getInventory();
			inv.setHelmet(null);	
			inv.setChestplate(null);
			inv.setLeggings(null);
			inv.setBoots(null);
			p.updateInventory();
			p.teleport(maps.getLobbySpawnPoint());
			BarUtil.setMessageAndPercent(p, ChatColor.DARK_AQUA
					+ "Welcome to Annihilation!", 0.01F);
			p.setHealth(20D);
			p.setFoodLevel(20);
			p.setSaturation(20F);
		}

		voting.start();
	}

	public void checkWin() {
		int alive = 0;
		AnnihilationTeam aliveTeam = null;
		for (AnnihilationTeam t : AnnihilationTeam.teams()) {
			if (t.getNexus().isAlive()) {
				alive++;
				aliveTeam = t;
			}
		}
		if (alive == 1) {
			endGame(aliveTeam);
		}
	}

	public SignHandler getSignHandler() {
		return sign;
	}

	public void setSignHandler(SignHandler sign) {
		this.sign = sign;
	}

	public static class Util {
		public static Location parseLocation(World w, String in) {
			String[] params = in.split(",");
			if (params.length == 3 || params.length == 5) {
				double x = Double.parseDouble(params[0]);
				double y = Double.parseDouble(params[1]);
				double z = Double.parseDouble(params[2]);
				Location loc = new Location(w, x, y, z);
				if (params.length == 5) {
					loc.setYaw(Float.parseFloat(params[4]));
					loc.setPitch(Float.parseFloat(params[5]));
				}
				return loc;
			}
			return null;
		}

		public static void sendPlayerToGame(Player player) {
			PlayerMeta meta = PlayerMeta.getMeta(player);
			if (meta.getTeam() != null) {
				meta.setAlive(true);
				player.teleport(meta.getTeam().getRandomSpawn());
				meta.getKit().give(player, meta.getTeam());
				player.setCompassTarget(meta.getTeam().getNexus().getLocation());
				player.setGameMode(GameMode.ADVENTURE);
				player.setHealth(player.getMaxHealth());
				player.setFoodLevel(20);
				player.setSaturation(20F);
			}
		}

		public static boolean isEmptyColumn(Location loc) {
			boolean hasBlock = false;
			Location test = loc.clone();
			for (int y = 0; y < loc.getWorld().getMaxHeight(); y++) {
				test.setY(y);
				if (test.getBlock().getType() != Material.AIR)
					hasBlock = true;
			}
			return !hasBlock;
		}
	}
}
