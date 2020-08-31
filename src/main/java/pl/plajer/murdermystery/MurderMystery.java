package pl.plajer.murdermystery;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.tigerhix.lib.scoreboard.ScoreboardLib;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaEvents;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaUtils;
import pl.plajer.murdermystery.arena.special.SpecialBlockEvents;
import pl.plajer.murdermystery.arena.special.mysterypotion.MysteryPotionRegistry;
import pl.plajer.murdermystery.arena.special.pray.PrayerRegistry;
import pl.plajer.murdermystery.commands.arguments.ArgumentsRegistry;
import pl.plajer.murdermystery.events.*;
import pl.plajer.murdermystery.events.spectator.SpectatorEvents;
import pl.plajer.murdermystery.events.spectator.SpectatorItemEvents;
import pl.plajer.murdermystery.handlers.*;
import pl.plajer.murdermystery.handlers.items.SpecialItem;
import pl.plajer.murdermystery.handlers.language.LanguageManager;
import pl.plajer.murdermystery.handlers.rewards.RewardsFactory;
import pl.plajer.murdermystery.handlers.scheduler.Scheduler;
import pl.plajer.murdermystery.handlers.sign.SignManager;
import pl.plajer.murdermystery.logging.ILogger;
import pl.plajer.murdermystery.logging.LoggerImpl;
import pl.plajer.murdermystery.perk.PerkRegister;
import pl.plajer.murdermystery.plugin.bootstrap.MurderMysteryBootstrap;
import pl.plajer.murdermystery.plugin.scheduler.SchedulerAdapter;
import pl.plajer.murdermystery.user.RankManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajer.murdermystery.user.data.MysqlManager;
import pl.plajer.murdermystery.utils.config.ConfigUtils;
import pl.plajer.murdermystery.utils.database.MysqlDatabase;
import pl.plajer.murdermystery.utils.serialization.InventorySerializer;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class MurderMystery extends JavaPlugin implements MurderMysteryBootstrap {

    @Getter
    private static MurderMystery instance;

    SchedulerAdapter schedulerAdapter;

    String version;

    boolean forceDisable = false;

    ExecutorService executorService;

    ScheduledExecutorService scheduledExecutorService;

    BungeeManager bungeeManager;

    RewardsFactory rewardsHandler;

    MysqlDatabase database;

    SignManager signManager;

    CorpseHandler corpseHandler;

    ConfigPreferences configPreferences;

    HookManager hookManager;

    UserManager userManager;

    Economy economy;

    ILogger logger = null;


    @Override
    public ILogger getPluginLogger() {
        if (this.logger == null) {
            throw new IllegalStateException("Logger has not been initialised yet");
        }
        return this.logger;
    }

    @Override
    public int getPlayerCount() {
        return this.getServer().getOnlinePlayers().size();
    }


    @Override
    public boolean isPlayerOnline(UUID uniqueId) {
        return this.getServer()
                   .getOnlinePlayers()
                   .stream()
                   .map(Player::getUniqueId)
                   .collect(Collectors.toList())
                   .contains(uniqueId);
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return schedulerAdapter;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return this.scheduledExecutorService;
    }

    @Override
    public BungeeManager getBungeeManager() {
        return this.bungeeManager;
    }

    @Override
    public RewardsFactory getRewardsHandler() {
        return this.rewardsHandler;
    }

    @Override
    public MysqlDatabase getDatabase() {
        return this.database;
    }

    @Override
    public SignManager getSignManager() {
        return this.signManager;
    }

    @Override
    public CorpseHandler getCorpseHandler() {
        return this.corpseHandler;
    }

    @Override
    public ConfigPreferences getConfigPreferences() {
        return this.configPreferences;
    }

    @Override
    public HookManager getHookManager() {
        return this.hookManager;
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public Economy getEconomy() {
        return this.economy;
    }

    @Override
    public void onLoad() {
        this.schedulerAdapter = new MurderMysterySchedulerAdapter(this);
        this.logger = new LoggerImpl(this.getLogger());
        this.executorService = Scheduler.createExecutorService();
        this.scheduledExecutorService = Scheduler.createScheduledExecutorService();
    }

    @Override
    public void onEnable() {
        instance = this;
        if (!validateIfPluginShouldStart()) {
            return;
        }
        setupEconomy();
        saveDefaultConfig();
        configPreferences = new ConfigPreferences(this);
        setupFiles();
        initializeClasses();


        //start hook manager later in order to allow soft-dependencies to fully load
        Bukkit.getScheduler().runTaskLater(this, () -> hookManager = new HookManager(), 20L * 5);
        if (configPreferences.getOption(ConfigPreferences.Option.NAMETAGS_HIDDEN)) {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ArenaUtils.updateNameTagsVisibility(player);
                }
            }, 60, 140);
        }
    }

    private boolean validateIfPluginShouldStart() {
        version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        if (!version.equalsIgnoreCase("v1_12_R1")) {
            MurderMystery.getInstance().getPluginLogger().severe("Your server version is not supported by Murder Mystery!");
            MurderMystery.getInstance().getPluginLogger().severe("Sadly, we must shut off. Maybe you consider changing your server version?");
            forceDisable = true;
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (Exception e) {
            MurderMystery.getInstance().getPluginLogger().severe("Your server software is not supported by Murder Mystery!");
            MurderMystery.getInstance().getPluginLogger().severe( "We support only Spigot and Spigot forks only! Shutting off...");
            forceDisable = true;
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }


    @Override
    public void onDisable() {
        if (forceDisable) {
            return;
        }

        saveAllUserStatistics();
        if (hookManager != null && hookManager.isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
            for (Hologram hologram : HologramsAPI.getHolograms(this)) {
                hologram.delete();
            }
        }
        if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
            this.getDatabase().shutdownConnPool();
        }

        for (Arena arena : ArenaRegistry.getArenas()) {
            arena.getScoreboardManager().stopAllScoreboards();
            for (Player player : arena.getPlayers()) {
                arena.doBarAction(Arena.BarAction.REMOVE, player);
                arena.teleportToEndLocation(player);
                if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
                    InventorySerializer.loadInventory(this, player);
                } else {
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                    for (PotionEffect pe : player.getActivePotionEffects()) {
                        player.removePotionEffect(pe.getType());
                    }
                    player.setWalkSpeed(0.2f);
                }
            }
            arena.teleportAllToEndLocation();
            arena.cleanUpArena();
        }
    }

    private void initializeClasses() {
        ScoreboardLib.setPluginInstance(this);
        if (getConfig().getBoolean("BungeeActivated", false)) {
            bungeeManager = new BungeeManager(this);
        }
        if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
            FileConfiguration config = ConfigUtils.getConfig(this, "mysql");
            database = new MysqlDatabase(config.getString("user"), config.getString("password"), config.getString("address"));
        }
        new ArgumentsRegistry(this);
        userManager = new UserManager(this);
        SpecialItem.loadAll();
        new PerkRegister().initPerks();
        PermissionsManager.init();
        RankManager.setupRanks();
        LanguageManager.init(this);
        new ArenaEvents();
        new SpectatorEvents(this);
        new QuitEvent(this);
        new JoinEvent(this);
        new ChatEvents(this);
        registerSoftDependenciesAndServices();
        User.cooldownHandlerTask();
        ArenaRegistry.registerArenas();
        new Events(this);
        new LobbyEvent(this);
        new SpectatorItemEvents(this);
        rewardsHandler = new RewardsFactory(this);
        signManager = new SignManager(this);
        corpseHandler = new CorpseHandler(this);
        new BowTrailsHandler(this);
        MysteryPotionRegistry.init(this);
        PrayerRegistry.init(this);
        new SpecialBlockEvents(this);
    }

    private void registerSoftDependenciesAndServices() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderManager().register();
        }
    }

    private void setupFiles() {
        for (String fileName : Arrays.asList("arenas", "bungee", "rewards", "stats", "lobbyitems", "mysql", "specialblocks", "filter", "ranks", "donaters")) {
            File file = new File(getDataFolder() + File.separator + fileName + ".yml");
            if (!file.exists()) {
                saveResource(fileName + ".yml", false);
            }
        }
    }

    private void saveAllUserStatistics() {
        for (Player player : getServer().getOnlinePlayers()) {
            User user = userManager.getUser(player);

            //copy of userManager#saveStatistic but without async database call that's not allowed in onDisable method.
            for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
                if (!stat.isPersistent()) {
                    continue;
                }
                if (userManager.getDatabase() instanceof MysqlManager) {
                    ((MysqlManager) userManager.getDatabase()).getDatabase().executeUpdate("UPDATE playerstats SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
                    continue;
                }
                userManager.getDatabase().saveStatistic(user, stat);
            }
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        }
    }
}
