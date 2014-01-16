package net.coasterman10.Annihilation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
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
import net.coasterman10.Annihilation.commands.DistanceCommand;
import net.coasterman10.Annihilation.commands.MapCommand;
import net.coasterman10.Annihilation.commands.StatsCommand;
import net.coasterman10.Annihilation.commands.TeamCommand;
import net.coasterman10.Annihilation.commands.TeamShortcutCommand;
import net.coasterman10.Annihilation.commands.VoteCommand;
import net.coasterman10.Annihilation.listeners.BossListener;
import net.coasterman10.Annihilation.listeners.ClassAbilityListener;
import net.coasterman10.Annihilation.listeners.CraftingListener;
import net.coasterman10.Annihilation.listeners.EnderBrewingStandListener;
import net.coasterman10.Annihilation.listeners.EnderChestListener;
import net.coasterman10.Annihilation.listeners.EnderFurnaceListener;
import net.coasterman10.Annihilation.listeners.PlayerListener;
import net.coasterman10.Annihilation.listeners.ResourceListener;
import net.coasterman10.Annihilation.listeners.SoulboundListener;
import net.coasterman10.Annihilation.listeners.WandListener;
import net.coasterman10.Annihilation.listeners.WorldListener;
import net.coasterman10.Annihilation.manager.BossManager;
import net.coasterman10.Annihilation.manager.ConfigManager;
import net.coasterman10.Annihilation.manager.DatabaseManager;
import net.coasterman10.Annihilation.manager.PhaseManager;
import net.coasterman10.Annihilation.manager.RestartHandler;
import net.coasterman10.Annihilation.manager.ScoreboardManager;
import net.coasterman10.Annihilation.manager.SignManager;
import net.coasterman10.Annihilation.maps.MapLoader;
import net.coasterman10.Annihilation.maps.MapManager;
import net.coasterman10.Annihilation.maps.VotingManager;
import net.coasterman10.Annihilation.object.Boss;
import net.coasterman10.Annihilation.object.GameTeam;
import net.coasterman10.Annihilation.object.Kit;
import net.coasterman10.Annihilation.object.PlayerMeta;
import net.coasterman10.Annihilation.object.Shop;
import net.coasterman10.Annihilation.stats.StatType;
import net.coasterman10.Annihilation.stats.StatsManager;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

public final class Annihilation extends JavaPlugin {
	private ConfigManager configManager;
	private VotingManager voting;
	private MapManager maps;
	private PhaseManager timer;
	private ResourceListener resources;
	private EnderFurnaceListener enderFurnaces;
	private EnderBrewingStandListener enderBrewingStands;
	private EnderChestListener enderChests;
	private StatsManager stats;
	private SignManager sign;
	private ScoreboardManager sb;
	private DatabaseManager db;
	private BossManager boss;
	
	public boolean useMysql = false;
	public boolean updateAvailable = false;
	public String newVersion;

	public int build = 1;
	public int respawn = 10;
	
	@Override
	public void onEnable() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    
		}
		
		UpdateResult updateResult = null;
		Updater u = null;

		if (this.getConfig().getBoolean("allowUpdater"))
			u = new Updater(this, 72127, this.getFile(),
					Updater.UpdateType.DEFAULT, true);

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
		
		MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

		maps = new MapManager(this, mapLoader, configManager.getConfig("maps.yml"));

		Configuration shops = configManager.getConfig("shops.yml");
		new Shop(this, "Weapon", shops);
		new Shop(this, "Brewing", shops);

		stats = new StatsManager(this, configManager);
		resources = new ResourceListener(this);
		enderFurnaces = new EnderFurnaceListener(this);
		enderBrewingStands = new EnderBrewingStandListener(this);
		enderChests = new EnderChestListener();
		sign = new SignManager(this);
		Configuration config = configManager.getConfig("config.yml");
		timer = new PhaseManager(this, config.getInt("start-delay"),
				config.getInt("phase-period"));
		voting = new VotingManager(this);
		sb = new ScoreboardManager();
		boss = new BossManager(this);
		
		PluginManager pm = getServer().getPluginManager();

		sign.loadSigns();

		sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
				+ " | " + ChatColor.GOLD + "/vote <name>");

		build = this.getConfig().getInt("build", 5);
		respawn = this.getConfig().getInt("bossRespawnDelay", 10);
		
		pm.registerEvents(resources, this);
		pm.registerEvents(enderFurnaces, this);
		pm.registerEvents(enderBrewingStands, this);
		pm.registerEvents(enderChests, this);
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new WorldListener(), this);
		pm.registerEvents(new SoulboundListener(), this);
		pm.registerEvents(new WandListener(this), this);
		pm.registerEvents(new CraftingListener(), this);
		pm.registerEvents(new ClassAbilityListener(this), this);
		pm.registerEvents(new BossListener(this), this);
		
		getCommand("annihilation").setExecutor(new AnnihilationCommand(this));
		getCommand("class").setExecutor(new ClassCommand());
		getCommand("stats").setExecutor(new StatsCommand(stats));
		getCommand("team").setExecutor(new TeamCommand(this));
		getCommand("vote").setExecutor(new VoteCommand(voting));
		getCommand("red").setExecutor(new TeamShortcutCommand());
		getCommand("green").setExecutor(new TeamShortcutCommand());
		getCommand("yellow").setExecutor(new TeamShortcutCommand());
		getCommand("blue").setExecutor(new TeamShortcutCommand());
		getCommand("distance").setExecutor(new DistanceCommand(this));
		getCommand("map").setExecutor(new MapCommand(this, mapLoader));

		BarUtil.init(this);

		if (config.getString("stats").equalsIgnoreCase("sql"))
			useMysql = true;

		if (useMysql) {
			String host = config.getString("MySQL.host");
			Integer port = config.getInt("MySQL.port");
			String name = config.getString("MySQL.name");
			String user = config.getString("MySQL.user");
			String pass = config.getString("MySQL.pass");
			db = new DatabaseManager(host, port, name, user, pass, this);

			db.query("CREATE TABLE IF NOT EXISTS `annihilation` ( `username` varchar(16) NOT NULL, "
					+ "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
					+ "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
					+ "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		} else
			db = new DatabaseManager(this);

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

		for (GameTeam team : GameTeam.teams()) {
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
			if (section.contains("brewingstands." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("brewingstands." + name));
				enderBrewingStands.setBrewingStandLocation(team, loc);
				loc.getBlock().setType(Material.BREWING_STAND);
			}
			if (section.contains("enderchests." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("enderchests." + name));
				enderChests.setEnderChestLocation(team, loc);
				loc.getBlock().setType(Material.ENDER_CHEST);
			}
		}
		
		if (section.contains("bosses")) {
			HashMap<String, Boss> bosses = new HashMap<String, Boss>();
			ConfigurationSection sec = section.getConfigurationSection("bosses");
			for (String boss : sec.getKeys(false))
				bosses.put(boss, 
				new Boss(boss, sec.getInt(boss + ".hearts") * 2, sec.getString(boss + ".name"), 
				Util.parseLocation(w, sec.getString(boss + ".spawn")), 
				Util.parseLocation(w, sec.getString(boss + ".chest"))));
			boss.loadBosses(bosses);
		}

		if (section.contains("diamonds")) {
			Set<Location> diamonds = new HashSet<Location>();
			for (String s : section.getStringList("diamonds"))
				diamonds.add(Util.parseLocation(w, s));
			resources.loadDiamonds(diamonds);
		}
	}

	public void startGame() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (Player pp : Bukkit.getOnlinePlayers()) {
				p.showPlayer(pp);
				pp.showPlayer(p);
			}
		}
		
		Bukkit.getPluginManager().callEvent(
				new GameStartEvent(maps.getCurrentMap()));
		sb.scores.clear();

		for (OfflinePlayer score : sb.sb.getPlayers())
			sb.sb.resetScores(score);

		sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
				+ WordUtils.capitalize(voting.getWinner()));

		for (GameTeam t : GameTeam.teams()) {
			sb.scores.put(t.name(), sb.obj.getScore(Bukkit
					.getOfflinePlayer(WordUtils.capitalize(t.name()
							.toLowerCase() + " Nexus"))));
			sb.scores.get(t.name()).setScore(t.getNexus().getHealth());

			Team sbt = sb.sb.registerNewTeam(t.name() + "SB");
			sbt.addPlayer(Bukkit.getOfflinePlayer(WordUtils
					.capitalize(WordUtils.capitalize(t.name().toLowerCase()
							+ " Nexus"))));
			sbt.setPrefix(t.color().toString());
		}

		sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
				+ WordUtils.capitalize(voting.getWinner()));

		for (Player p : getServer().getOnlinePlayers())
			if (PlayerMeta.getMeta(p).getTeam() != GameTeam.NONE)
				Util.sendPlayerToGame(p);

		sb.update();

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
		
		if (timer.getPhase() == 2)
			boss.spawnBosses();
		
		if (timer.getPhase() == 3)
			resources.spawnDiamonds();
		
		Bukkit.getPluginManager().callEvent(
				new PhaseChangeEvent(timer.getPhase()));

		getSignHandler().updateSigns(GameTeam.RED);
		getSignHandler().updateSigns(GameTeam.BLUE);
		getSignHandler().updateSigns(GameTeam.GREEN);
		getSignHandler().updateSigns(GameTeam.YELLOW);
	}

	public void onSecond() {
		long time = timer.getTime();

		if (time == -5L) {
			String winner = voting.getWinner();
			maps.selectMap(winner);
			getServer().broadcastMessage(
					ChatColor.GREEN + WordUtils.capitalize(winner)
							+ " was chosen!");
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

	public DatabaseManager getDatabaseHandler() {
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

	public ScoreboardManager getScoreboardHandler() {
		return sb;
	}

	public void endGame(GameTeam winner) {
		if (winner == null)
			return;

		ChatUtil.winMessage(winner);
		timer.stop();

		for (Player p : getServer().getOnlinePlayers())
			if (PlayerMeta.getMeta(p).getTeam() == winner)
				stats.incrementStat(StatType.WINS, p);
		long restartDelay = configManager.getConfig("config.yml").getLong(
				"restart-delay");
		new RestartHandler(this, restartDelay).start(timer.getTime());
	}

	public void reset() {
		sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
				+ " | " + ChatColor.GOLD + "/vote <name>");
		maps.reset();
		timer.reset();
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerMeta.getMeta(p).setTeam(GameTeam.NONE);
			p.teleport(maps.getLobbySpawnPoint());
			BarUtil.setMessageAndPercent(p, ChatColor.DARK_AQUA
					+ "Welcome to Annihilation!", 0.01F);
			p.setMaxHealth(20D);
			p.setHealth(20D);
			p.setFoodLevel(20);
			p.setSaturation(20F);
		}

		voting.start();
		sb.update();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (Player pp : Bukkit.getOnlinePlayers()) {
				p.showPlayer(pp);
				pp.showPlayer(p);
			}
		}
		
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				for (Player p : getServer().getOnlinePlayers()) {
					PlayerInventory inv = p.getInventory();
					inv.setHelmet(null);
					inv.setChestplate(null);
					inv.setLeggings(null);
					inv.setBoots(null);
					
					p.getInventory().clear();
					
					for(PotionEffect effect : p.getActivePotionEffects())
						p.removePotionEffect(effect.getType());
					
					p.setLevel(0);
					p.setExp(0);
					p.setSaturation(20F);
					
					ItemStack selector = new ItemStack(Material.FEATHER);
					ItemMeta itemMeta = selector.getItemMeta();
					itemMeta.setDisplayName(ChatColor.AQUA
							+ "Right click to select class");
					selector.setItemMeta(itemMeta);

					p.getInventory().setItem(0, selector);
					
					p.updateInventory();
				}
				
				for (GameTeam t : GameTeam.values())
					if (t != GameTeam.NONE) sign.updateSigns(t);
				
				checkStarting();
			}
		}, 2L);
	}

	public void checkWin() {
		int alive = 0;
		GameTeam aliveTeam = null;
		for (GameTeam t : GameTeam.teams()) {
			if (t.getNexus().isAlive()) {
				alive++;
				aliveTeam = t;
			}
		}
		if (alive == 1) {
			endGame(aliveTeam);
		}
	}

	public SignManager getSignHandler() {
		return sign;
	}

	public void setSignHandler(SignManager sign) {
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
		
		public static void showClassSelector(Player player, String title) {
			int size = ((Kit.values().length + 8) / 9) * 9;
			Inventory inv = Bukkit.createInventory(null, size, title);
			for (Kit kit : Kit.values())
				inv.addItem(kit.getIcon());
			player.openInventory(inv);
		}
		
		public static void spawnFirework(Location loc) {
			Random colour = new Random();
			
			Firework fw = loc.getWorld().spawn(loc, Firework.class);
			FireworkMeta fwMeta = fw.getFireworkMeta();
			
			Type fwType = Type.BALL_LARGE;
			
			int c1i = colour.nextInt(17) + 1;
			int c2i = colour.nextInt(17) + 1;
			
			Color c1 = getFWColor(c1i);
			Color c2 = getFWColor(c2i);
			
			FireworkEffect effect = FireworkEffect.builder().withFade(c2).withColor(c1).with(fwType).build();
			
			fwMeta.addEffect(effect);
			fwMeta.setPower(1);
			fw.setFireworkMeta(fwMeta);
		}
		
		public static Color getFWColor(int c) {
			switch (c) {
			case 1:
				return Color.TEAL;
			default:
			case 2:
				return Color.WHITE;
			case 3:
				return Color.YELLOW;
			case 4:
				return Color.AQUA;
			case 5:
				return Color.BLACK;
			case 6:
				return Color.BLUE;
			case 7:
				return Color.FUCHSIA;
			case 8:
				return Color.GRAY;
			case 9:
				return Color.GREEN;
			case 10:
				return Color.LIME;
			case 11:
				return Color.MAROON;
			case 12:
				return Color.NAVY;
			case 13:
				return Color.OLIVE;
			case 14:
				return Color.ORANGE;
			case 15:
				return Color.PURPLE;
			case 16:
				return Color.RED;
			case 17:
				return Color.SILVER;
			}
		}
	}

	public void checkStarting() {
		if (!timer.isRunning()) {
			if (Bukkit.getOnlinePlayers().length >= getConfig().getInt("requiredToStart"))
				timer.start();
		}
	}

	public BossManager getBossManager() {
		return boss;
	}
}
