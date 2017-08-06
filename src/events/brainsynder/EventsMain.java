//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package events.brainsynder;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EventsMain {
    SettingsManager settings = SettingsManager.getInstance();
    public boolean eventstarting = false;
    public boolean eventstarted = false;
    public boolean cancelled = false;
    public Game waiting = null;
    public GamePlugin plugin;
    
    public EventsMain(GamePlugin plugin) {
        plugin = plugin;
    }
    
    public void end() {
        waiting = null;
        eventstarted = false;
        eventstarting = false;
        cancelled = true;
    }
}
