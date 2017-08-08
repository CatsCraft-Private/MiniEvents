package events.brainsynder.managers;

import events.brainsynder.EventsMain;
import events.brainsynder.Handle;
import events.brainsynder.SettingsManager;
import events.brainsynder.commands.GameCommands;
import events.brainsynder.commands.api.CommandManager;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.utils.CountDown;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class GamePlugin extends JavaPlugin {
    private SettingsManager settings = SettingsManager.getInstance();
    private EventsMain eventsmain = new EventsMain(this);
    private CountDown method;
    public static Economy econ = null;
    public static GamePlugin instance = null;
    public static List<String> starting = Arrays.asList(
            "§b§kO§7*****> §bEVENT§7 <*****§b§kO",
            " ",
            "§b§kO §7Event: §b{EVENT}",
            "§b§kO §7Prize: §b{PRIZE}",
            "§b§kO §7Host: §b{PLAYER}",
            "§b§kO §7Hover [HERE] §7for a description",
            "§b§kO §7To join Type §b/join",
            " ",
            "§b§kO§7*****> §bEVENT§7 <*****§b§kO"
    );
    
    public void onEnable() {
        instance = this;
        GameManager.initiate();
        settings.setup(this);
        saveResource("config.yml", false);
        method = new CountDown(this);
        Bukkit.getServer().getPluginManager().registerEvents(new Handle(), this);
        CommandManager.register(new GameCommands());
        this.getCommand("event").setTabCompleter(new EventCommandTabManager());
        if (!setupEconomy()) {
            getLogger().info("Could not find Vault. Make sure you set money.enabled to false.");
        }
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                econ = rsp.getProvider();
                return econ != null;
            }
        }
    }
    
    public void onDisable() {
        try {
            for (Player o : Bukkit.getOnlinePlayers()) {
                IGamePlayer gamePlayer = GameManager.getPlayer(o);
                if (gamePlayer.isPlaying()) {
                    Game current = gamePlayer.getGame();
                    current.onEnd();
                }
            }
        }catch (Exception ignored){}
    
        eventsmain.end();
        saveConfig();
    }
    
    public CountDown getMethod() {
        return method;
    }
    
    public EventsMain getEventMain() {
        return eventsmain;
    }
    
    public SettingsManager getSettings() {
        return this.settings;
    }
}
