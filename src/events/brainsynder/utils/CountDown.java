package events.brainsynder.utils;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.nms.ITellraw;
import simple.brainsynder.utils.Reflection;

/**
 * Runs the Countdown message
 */
public class CountDown implements Listener {
    public GamePlugin plugin;
    
    public CountDown(GamePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void start(Game game) {
        if (plugin.getEventMain().eventstarting) {
            new BukkitRunnable() {
                int i = plugin.getConfig().getInt("events.time-to-start");
                
                public void run() {
                    switch (i) {
                        case 0:
                            if (plugin.getEventMain().cancelled) {
                                plugin.getEventMain().cancelled = false;
                                cancel();
                            } else {
                                int size = game.getPlayer().size();
                                if (size <= 1) {
                                    if (size == 1) {
                                        IGamePlayer player = game.getPlayer().get(0);
                                        game.removePlayer(player);
                                        player.setGame(null);
                                        
                                    }
                                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.event-not-enough-players").replace("{EVENT}", game.getName().toUpperCase())));
                                    plugin.getEventMain().end();
                                    cancel();
                                } else {
                                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.event-started").replace("{EVENT}", game.getName().toUpperCase())));
                                    game.onStart();
                                    cancel();
                                }
                            }
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 10:
                        case 20:
                        case 30:
                        case 45:
                        case 60:
                            if (plugin.getEventMain().cancelled) {
                                plugin.getEventMain().cancelled = false;
                                cancel();
                            } else {
                                ITellraw raw = Reflection.getTellraw("§b{EVENT} §7will start in §b{TIME} §7seconds! "
                                        .replace("{TIME}", Integer.toString(i)).replace("{EVENT}", game.getName().toUpperCase()));
                                raw.then("§7Type §b/join §7to join.");
                                raw.tooltip("§7Or Click Here ;)");
                                raw.command("/join");
                                Bukkit.getOnlinePlayers().forEach(raw::send);
                            }
                    }
                    
                    --i;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }
}

