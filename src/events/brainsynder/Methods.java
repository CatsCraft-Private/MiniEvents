//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package events.brainsynder;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.nms.ITellraw;
import simple.brainsynder.utils.Reflection;

public class Methods implements Listener {
    SettingsManager settings = SettingsManager.getInstance();
    public GamePlugin plugin;
    
    public Methods(GamePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void EventWait(Game game) {
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
                                    ITellraw raw = Reflection.getTellraw("§b{EVENT} §7will start in §b{TIME} §7seconds! "
                                            .replace("{TIME}", Integer.toString(i)).replace("{EVENT}", game.getName().toUpperCase()));
                                    raw.then("§7Type §b/join §7to join.");
                                    raw.tooltip("§7Or Click Here ;)");
                                    raw.command("/join");
                                    Bukkit.getOnlinePlayers().stream().forEach(raw::send);
                                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.event-started").replace("{EVENT}", game.getName().toUpperCase())));
                                    game.registerListeners();
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
                                Bukkit.getOnlinePlayers().stream().forEach(raw::send);
                            }
                    }
                    
                    --i;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        
    }
    /*
    public void JoinEvent(final Player player) {
        
        plugin.getEventMain().save(player);
        plugin.getEventMain().savePotion(player);
        if (plugin.getEventMain().eventstarted) {
            //TODO: LMS starting
            if (plugin.getEventMain().lms) {
                if (settings.getData().getConfigurationSection("lms") == null) {
                    plugin.getEventMain().end();
                } else {
                    plugin.getEventMain().slms.add(player);
                    plugin.getEventMain().sbefore.add(player);
                    plugin.getEventMain().Put(player);
                    World w = Bukkit.getServer().getWorld(settings.getData().getString("lms.world"));
                    double x = settings.getData().getDouble("lms.x");
                    double y = settings.getData().getDouble("lms.y");
                    double z = settings.getData().getDouble("lms.z");
                    float yaw = Float.intBitsToFloat(settings.getData().getInt("lms.yaw"));
                    float pitch = Float.intBitsToFloat(settings.getData().getInt("lms.pitch"));
                    Location lko = new Location(w, x, y, z, yaw, pitch);
                    plugin.getLMS().equipLMS(player);
                    for (Player o : Bukkit.getOnlinePlayers())
                        if (plugin.getEventMain().slms.contains(o)) {
                            o.teleport(lko);
                        }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.lms-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.lms-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: KOTH startint
        if (plugin.getEventMain().koth) {
            if (settings.getData().getConfigurationSection("koth") == null) {
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().skoth.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("koth.world"));
                double x = settings.getData().getDouble("koth.x");
                double y = settings.getData().getDouble("koth.y");
                double z = settings.getData().getDouble("koth.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("koth.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("koth.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                World ww = Bukkit.getServer().getWorld(settings.getData().getString("koth.top.world"));
                double xw = settings.getData().getDouble("koth.top.x");
                double yw = settings.getData().getDouble("koth.top.y");
                double zw = settings.getData().getDouble("koth.top.z");
                float yaww = Float.intBitsToFloat(settings.getData().getInt("koth.top.yaw"));
                float pitchw = Float.intBitsToFloat(settings.getData().getInt("koth.top.pitch"));
                Location loc = new Location(ww, xw, yw, zw, yaww, pitchw);
                plugin.getKOTH().equipKOTH(player);
                
                for (Player o : Bukkit.getOnlinePlayers())
                    if (plugin.getEventMain().skoth.contains(o)) {
                        o.teleport(lko);
                        plugin.getKOTH().startKOTH(loc);
                    }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.koth-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.koth-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: Parkour Starting
        if (plugin.getEventMain().parkour) {
            if (settings.getData().getConfigurationSection("parkour") == null) {
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().sparkour.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("parkour.world"));
                double x = settings.getData().getDouble("parkour.x");
                double y = settings.getData().getDouble("parkour.y");
                double z = settings.getData().getDouble("parkour.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("parkour.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("parkour.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                World ww = Bukkit.getServer().getWorld(settings.getData().getString("parkour.top.world"));
                double xw = settings.getData().getDouble("parkour.top.x");
                double yw = settings.getData().getDouble("parkour.top.y");
                double zw = settings.getData().getDouble("parkour.top.z");
                float yaww = Float.intBitsToFloat(settings.getData().getInt("parkour.top.yaw"));
                float pitchw = Float.intBitsToFloat(settings.getData().getInt("parkour.top.pitch"));
                Location loc = new Location(ww, xw, yw, zw, yaww, pitchw);
                plugin.getParkour().equipParkour(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().sparkour.contains(o)) {
                        o.teleport(lko);
                        plugin.getParkour().startKOTH(loc);
                    }
                }
                
                if (plugin.getEventMain().inevent.size() != 0) {
                    plugin.getEventMain().eventstarting = false;
                    plugin.getEventMain().eventstarted = true;
                }
            }
        }
        
        //TODO: Paintball Starting
        if (plugin.getEventMain().paint) {
            if (settings.getData().getConfigurationSection("paint") == null) {
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().spaint.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("paint.world"));
                double x = settings.getData().getDouble("paint.x");
                double y = settings.getData().getDouble("paint.y");
                double z = settings.getData().getDouble("paint.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("paint.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("paint.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                plugin.getPaintBall().equipKO(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().spaint.contains(o)) {
                        o.teleport(lko);
                    }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.paint-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.paint-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: OITC starting
        if (plugin.getEventMain().oitc) {
            if (settings.getData().getConfigurationSection("oitc") == null) {
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().soitc.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("oitc.world"));
                double x = settings.getData().getDouble("oitc.x");
                double y = settings.getData().getDouble("oitc.y");
                double z = settings.getData().getDouble("oitc.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("oitc.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("oitc.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                plugin.getOitc().equipOITC(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().soitc.contains(o)) {
                        o.teleport(lko);
                    }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.oitc-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.oitc-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: TntRun Starting
        if (plugin.getEventMain().tnt) {
            if (settings.getData().getConfigurationSection("tnt") == null) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.tnt-spawn-not-set")));
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().stnt.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("tnt.world"));
                double x = settings.getData().getDouble("tnt.x");
                double y = settings.getData().getDouble("tnt.y");
                double z = settings.getData().getDouble("tnt.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("tnt.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("tnt.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                plugin.getTNT().equipLMS(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().stnt.contains(o)) {
                        o.teleport(lko);
                    }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.tnt-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.tnt-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: Spleef Starting
        if (plugin.getEventMain().spleef) {
            if (settings.getData().getConfigurationSection("spleef") == null) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.spleef-spawn-not-set")));
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().sspleef.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("spleef.world"));
                double x = settings.getData().getDouble("spleef.x");
                double y = settings.getData().getDouble("spleef.y");
                double z = settings.getData().getDouble("spleef.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("spleef.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("spleef.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                plugin.getSpleef().equipSpleef(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().sspleef.contains(o)) {
                        o.teleport(lko);
                    }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.spleef-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.spleef-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: Bow batter starting
        if (plugin.getEventMain().bow) {
            if (settings.getData().getConfigurationSection("bow") == null) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bow-spawn-not-set")));
                plugin.getEventMain().end();
            } else {
                plugin.getEventMain().sbow.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("bow.world"));
                double x = settings.getData().getDouble("bow.x");
                double y = settings.getData().getDouble("bow.y");
                double z = settings.getData().getDouble("bow.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("bow.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("bow.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                plugin.getBow().equipBow(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().sbow.contains(o)) {
                        o.teleport(lko);
                    }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bow-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bow-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            }
        }
        
        //TODO: KnockOut starting
        if (plugin.getEventMain().ko) {
            if (settings.getData().getConfigurationSection("ko") != null) {
                plugin.getEventMain().sko.add(player);
                plugin.getEventMain().sbefore.add(player);
                plugin.getEventMain().Put(player);
                World w = Bukkit.getServer().getWorld(settings.getData().getString("ko.world"));
                double x = settings.getData().getDouble("ko.x");
                double y = settings.getData().getDouble("ko.y");
                double z = settings.getData().getDouble("ko.z");
                float yaw = Float.intBitsToFloat(settings.getData().getInt("ko.yaw"));
                float pitch = Float.intBitsToFloat(settings.getData().getInt("ko.pitch"));
                Location lko = new Location(w, x, y, z, yaw, pitch);
                plugin.getKO().equipKO(player);
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEventMain().sko.contains(o)) {
                        o.teleport(lko);
                        o.setHealth(player.getMaxHealth());
                    }
                }
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.ko-before")));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (plugin.getEventMain().inevent.size() != 0) {
                        plugin.getEventMain().sbefore.clear();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.ko-invins-over")));
                        plugin.getEventMain().eventstarting = false;
                        plugin.getEventMain().eventstarted = true;
                    }
                    
                }, 120L);
            } else {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.ko-spawn-not-set")));
                plugin.getEventMain().end();
            }
        }
    }*/
}

