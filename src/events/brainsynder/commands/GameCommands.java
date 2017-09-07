package events.brainsynder.commands;

import events.brainsynder.SettingsManager;
import events.brainsynder.commands.api.Command;
import events.brainsynder.commands.api.CommandListener;
import events.brainsynder.events.player.GameCountdownLeaveEvent;
import events.brainsynder.events.player.GamePlayerJoinEvent;
import events.brainsynder.games.KOTH;
import events.brainsynder.games.Parkour;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import events.brainsynder.utils.BlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import simple.brainsynder.nms.ITellraw;
import simple.brainsynder.utils.Reflection;

import java.util.Arrays;

public class GameCommands implements CommandListener {
    private GamePlugin plugin = GamePlugin.instance;
    private SettingsManager settings = plugin.getSettings();
    
    public void end() {
        plugin.getEventMain().waiting = null;
        plugin.getEventMain().eventstarted = false;
        plugin.getEventMain().eventstarting = false;
        plugin.getEventMain().cancelled = true;
    }

    @Command(name = "setgamespawn")
    public void setSpawn(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/setgamespawn <game>");
        } else {
            if (!player.hasPermission("events.setSpawn")) return;
            Game game = GameManager.getGame(args[0]);
            if (game == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no event by that name."));
                return;
            }
            Location l = player.getLocation();
            settings.getData().set("setup." + game.getName() + ".world", l.getWorld().getName());
            settings.getData().set("setup." + game.getName() + ".x", l.getX());
            settings.getData().set("setup." + game.getName() + ".y", l.getY());
            settings.getData().set("setup." + game.getName() + ".z", l.getZ());
            settings.getData().set("setup." + game.getName() + ".yaw", Float.floatToIntBits(l.getYaw()));
            settings.getData().set("setup." + game.getName() + ".pitch", Float.floatToIntBits(l.getPitch()));
            player.sendMessage("§cSet spawn point for §7" + game.getName());
        }
    }

    @Command(name = "setteamspawn")
    public void setTeamSpawn(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/setteamspawn <game> <team>");
        } else {
            if (!player.hasPermission("events.setSpawn")) return;
            Game g = GameManager.getGame(args[0]);
            if (g == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no event by that name."));
                return;
            }
            if (!(g instanceof ITeamGame)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis Event is not a Team based Event"));
                return;
            }
            ITeamGame game = (ITeamGame)g;
            Location l = player.getLocation();

            if (args.length == 1) {
                player.sendMessage("/setteamspawn <game> <team>");
            }else{
                Team team = null;
                if (game.getRedTeam().getName().equalsIgnoreCase(args[1])) team = game.getRedTeam();
                if (game.getBlueTeam().getName().equalsIgnoreCase(args[1])) team = game.getBlueTeam();
                if (team == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo team was found for: " + args[1]));
                    return;
                }

                settings.getData().set("setup." + game.getName() + ".team." + team.getName() + ".world", l.getWorld().getName());
                settings.getData().set("setup." + game.getName() + ".team." + team.getName() + ".x", l.getX());
                settings.getData().set("setup." + game.getName() + ".team." + team.getName() + ".y", l.getY());
                settings.getData().set("setup." + game.getName() + ".team." + team.getName() + ".z", l.getZ());
                settings.getData().set("setup." + game.getName() + ".team." + team.getName() + ".yaw", Float.floatToIntBits(l.getYaw()));
                settings.getData().set("setup." + game.getName() + ".team." + team.getName() + ".pitch", Float.floatToIntBits(l.getPitch()));
                player.sendMessage("§cSet spawn point for §7" + game.getName());
            }
        }
    }
    
    private void save(Player player, Game game) {
        int slot = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            settings.getData().set("setup." + game.getName() + ".inv." + slot, stack);
            slot++;
        }
        
        int sslot = 100;
        for (ItemStack stack : player.getInventory().getArmorContents()) {
            settings.getData().set("setup." + game.getName() + ".armor." + sslot, stack);
            ++sslot;
        }
        
        int v = 0;
        for (PotionEffect p : player.getActivePotionEffects()) {
            settings.getData().set("setup." + game.getName() + ".potion.name." + v + ".type", p.getType().getName());
            settings.getData().set("setup." + game.getName() + ".potion.name." + v + ".level", p.getAmplifier());
            settings.getData().set("setup." + game.getName() + ".potion.name." + v + ".duration", p.getDuration());
            v++;
        }
    }
    
    @Command(name = "setinv")
    public void setInv(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/setinv <game>");
        } else {
            Game game = GameManager.getGame(args[0]);
            if (game == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no event by that name."));
                return;
            }
            save(player, game);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Saved inventory loadout for {0}.".replace("{0}", game.getName())));
        }
    }
    
    @Command(name = "delinv")
    public void delInv(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/delinv <game>");
        } else {
            Game game = GameManager.getGame(args[0]);
            if (game == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no event by that name."));
                return;
            }
            settings.getData().set("setup." + game.getName(), null);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Deleted inventory loadout for {0}.".replace("{0}", game.getName())));
        }
    }
    
    @Command(name = "join")
    public void join(Player player) {
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (gamePlayer.isPlaying() || (gamePlayer.getState() == IGamePlayer.State.WAITING)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are already in the event."));
            return;
        }
    
        if (!plugin.getEventMain().eventstarting) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere are no events to join."));
            return;
        }
    
        if (plugin.getEventMain().eventstarted) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEvent has already started."));
            return;
        }
        gamePlayer.setGame(plugin.getEventMain().waiting);
        GamePlayerJoinEvent<Game> event = new GamePlayerJoinEvent<>(plugin.getEventMain().waiting, gamePlayer);
        Bukkit.getPluginManager().callEvent(event);
    }

    /*@Command(name = "tester")
    public void test (Player player, String[] args) {
        if (args.length == 0) {
            run(10, 2);
        }else{
            if (args.length == 1) {
                run(Integer.parseInt(args[0]), 2);
            }else{
                run(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            }
        }
    }

    private void run (int max, int players) {
        for (int tries = 0; tries < max; tries++) {
            List<String> red = new ArrayList<>();
            List<String> blue = new ArrayList<>();
            for (int count = 0; count < players; count++) {
                if (blue.size() == red.size()) {
                    Random rand = new Random();
                    if (rand.nextBoolean()) {
                        red.add(String.valueOf(count));
                    } else {
                        blue.add(String.valueOf(count));
                    }
                } else {
                    if (red.size() > blue.size()) {
                        blue.add(String.valueOf(count));
                    } else {
                        red.add(String.valueOf(count));
                    }
                }
            }

            Bukkit.broadcastMessage("Try #" + (tries + 1) + " §4Red(§c" + red.size() + "§4) §9Blue(§b" + blue.size() + "§9)");
        }
    }*/

    @Command(name = "leave")
    public void leave(Player player) {
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (!gamePlayer.isPlaying() && (gamePlayer.getState() != IGamePlayer.State.WAITING)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are not in an event."));
            return;
        }

        if (plugin.getEventMain().eventstarted) {
            gamePlayer.getGame().onLeave(gamePlayer);
            return;
        }
        if ((!plugin.getEventMain().eventstarting) && (!plugin.getEventMain().eventstarted)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere are no events are running."));
            return;
        }
        GameCountdownLeaveEvent<Game> event = new GameCountdownLeaveEvent<>(gamePlayer.getGame(), gamePlayer);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    @Command(name = "setkothtop")
    public void setScore(Player player, String[] args) {
        if (!player.hasPermission("events.setKOTH")) return;
        Location l = player.getLocation();
        Game game = GameManager.getGame(KOTH.class);
        settings.getData().set("setup." + game.getName() + ".top.world", l.getWorld().getName());
        settings.getData().set("setup." + game.getName() + ".top.x", l.getX());
        settings.getData().set("setup." + game.getName() + ".top.y", l.getY());
        settings.getData().set("setup." + game.getName() + ".top.z", l.getZ());
        settings.getData().set("setup." + game.getName() + ".top.yaw", l.getYaw());
        settings.getData().set("setup." + game.getName() + ".top.pitch", l.getPitch());
        player.sendMessage("§cSet score point for §7" + game.getName());
    }
    
    @Command(name = "setparkourwin")
    public void setParkourScore(Player player, String[] args) {
        if (!player.hasPermission("events.setParkour")) return;
        BlockLocation l = new BlockLocation(player.getLocation());
        Game game = GameManager.getGame(Parkour.class);
        settings.getData().set("setup." + game.getName() + ".winLocation", l.toDataString());
        player.sendMessage("§cSet score point for §7" + game.getName());
    }
    
    @Command(name = "event")
    public void event(Player player, String[] args) {
        if (args.length == 0) {
            sendUssage(player);
        } else {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("event.reload") && !player.hasPermission("event.*")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission."));
                    return;
                }

                GameManager.resetGames();
                plugin.reloadConfig();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Reloaded MiniEvent's config."));
            } else if (args[0].equalsIgnoreCase("end")) {
                
                if (!player.hasPermission("event.end") && !player.hasPermission("event.*") && !player.isOp()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission."));
                } else {
                    for (Player o : Bukkit.getOnlinePlayers()) {
                        IGamePlayer gamePlayer = GameManager.getPlayer(o);
                        if (gamePlayer.isPlaying()) {
                            Game current = gamePlayer.getGame();
                            current.onEnd();
                        }
                    }
                    
                    plugin.getEventMain().end();
                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cAny event in progress was ended."));
                }
            } else {
                if (plugin.getEventMain().waiting != null ||  plugin.getEventMain().eventstarted || plugin.getEventMain().eventstarting) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is already an events in progress."));
                    return;
                }
                Game game = GameManager.getGame(args[0]);
                if (game == null) {
                    sendUssage(player);
                    return;
                }
                if (!game.isSetup()) {
                    player.sendMessage("§cThe spawn point for " + game.getName() + " has not yet been set.");
                    return;
                }
                double i = 100;
                plugin.getEventMain().eventstarting = true;
                plugin.getEventMain().cancelled = false;
                if (!game.getPlayers().isEmpty())
                    game.getPlayers().clear();
                plugin.getEventMain().waiting = game;
                plugin.getMethod().start(game);
                for (String e : GamePlugin.starting) {
                    if (e.contains("[HERE]")) {
                        String[] var = e.split("(?:\\[HERE\\])");
                        ITellraw raw = Reflection.getTellraw(var[0]);
                        raw.then("§b[HERE]");
                        if (!Arrays.asList(game.description()).isEmpty()) {
                            raw.tooltip(game.description());
                        } else {
                            raw.tooltip("§7There is no description", "§7for this Event");
                        }
                        raw.then(var[1]);
                        Bukkit.getOnlinePlayers().forEach(raw::send);
                        continue;
                    }
                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', e).replace("{EVENT}", game.getName()).replace("{PLAYER}", player.getName()).replace("{PRIZE}", Double.toString(i)));
                }
            }
        }
    }
    
    private void sendUssage(Player player) {
        ITellraw raw = Reflection.getTellraw("§7/event §c[§7end§c, §7reload");
        for (Game game : GameManager.getGames()) {
            if (game.isSetup()) {
                raw.then(", ").color(ChatColor.RED);
                raw.then(game.getName()).color(ChatColor.GRAY);
                if (!Arrays.asList(game.description()).isEmpty()) {
                    raw.tooltip(game.description());
                } else {
                    raw.tooltip("§7There is no description", "§7for this Event");
                }
            }
        }
        raw.then("]").color(ChatColor.RED);
        raw.send(player);
    }
}
