package events.brainsynder.managers;

import events.brainsynder.*;
import events.brainsynder.commands.GameCommands;
import events.brainsynder.commands.api.CommandManager;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.utils.CountDown;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import simple.brainsynder.api.ItemMaker;

import java.util.Arrays;
import java.util.List;

public class GamePlugin extends JavaPlugin {
    private SettingsManager settings;
    private EventsMain eventsmain;
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
    private CubeHandler cubeHandler;
    
    public void onEnable() {
        instance = this;
        settings = SettingsManager.getInstance();
        eventsmain = new EventsMain(this);
        GameManager.initiate();
        settings.setup(this);
        saveResource("config.yml", false);
        method = new CountDown(this);
        Bukkit.getServer().getPluginManager().registerEvents(new Handle(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GameListener(), this);
        CommandManager.register(new GameCommands());
        cubeHandler = new CubeHandler ();
        cubeHandler.load(this);
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
        cubeHandler.unload(this);
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

    public ItemStack getCubeWand() {
        ItemMaker maker = new ItemMaker(Material.STICK);
        maker.setName("&eCube Wand");
        return maker.create();
    }
}
