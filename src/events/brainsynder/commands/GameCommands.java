package events.brainsynder.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
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
import events.brainsynder.utils.EntityLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import simple.brainsynder.nbt.StorageTagCompound;
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
            player.sendMessage("/setgamespawn <game> <mapID>");
        } else {
            if (!player.hasPermission("events.setSpawn")) return;
            Game game = GameManager.getGame(args[0]);
            if (game == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no event by that name."));
                return;
            }

            EntityLocation l = new EntityLocation (player.getLocation());
            if (args.length == 1) {
                player.sendMessage("/setgamespawn <game> <mapID>");
            } else {
                if (!game.hasRegion(args[1])) {
                    player.sendMessage("§cPlease make a region for the map first (/registermap <game> <mapID>)");
                    return;
                }
                if (game.hasMapID(args[1])) {
                    StorageTagCompound compound = game.getCompound(args[1]);
                    compound.setTag("spawn", l.toCompound());
                    game.save(compound, args[1]);
                    player.sendMessage("§cSet spawn point for §7" + game.getName());
                }
            }
        }
    }

    @Command(name = "registermap")
    public void registerMap(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/registermap <game> <mapID>");
        } else {
            if (!player.hasPermission("events.setSpawn")) return;
            Game game = GameManager.getGame(args[0]);
            if (game == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is no event by that name."));
                return;
            }

            WorldEditPlugin we = (WorldEditPlugin) GamePlugin.instance.getServer().getPluginManager().getPlugin("WorldEdit");
            Selection sel = we.getSelection(player);
            if(sel == null){
                player.sendMessage("§cYou must make a WorldEdit Selection of the arena first.");
                return;
            }
            BlockLocation min = new BlockLocation (sel.getMinimumPoint());
            BlockLocation max = new BlockLocation (sel.getMaximumPoint());
            if (args.length == 1) {
                player.sendMessage("/registermap <game> <mapID>");
            } else {
                game.setCuboid(args[1], min, max);
                player.sendMessage("§aArena has now been registered with a region");
            }
        }
    }

    @Command(name = "setteamspawn")
    public void setTeamSpawn(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/setteamspawn <game> <mapID> <team>");
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
            ITeamGame game = (ITeamGame) g;
            EntityLocation l = new EntityLocation (player.getLocation());

            if (args.length == 1) {
                player.sendMessage("/setteamspawn <game> <mapID> <team>");
            } else {
                if (!game.hasRegion(args[1])) {
                    player.sendMessage("§cPlease make a region for the map first (/registermap <game> <mapID>)");
                    return;
                }
                if (game.hasMapID(args[1])) {
                    StorageTagCompound compound = game.getCompound(args[1]);

                    if (args.length == 2) {
                        player.sendMessage("/setteamspawn <game> <mapID> <team>");
                    } else {
                        Team team = null;
                        if (game.getRedTeam().getName().equalsIgnoreCase(args[2])) team = game.getRedTeam();
                        if (game.getBlueTeam().getName().equalsIgnoreCase(args[2])) team = game.getBlueTeam();
                        if (team == null) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo team was found for: " + args[2]));
                            return;
                        }
                        compound.setTag(team.getName(), l.toCompound());
                        game.save(compound, args[1]);
                        player.sendMessage("§cSet spawn point for §7" + game.getName());
                    }
                }
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
        if (gamePlayer.isPlaying() || (gamePlayer.getState() != IGamePlayer.State.NOT_PLAYING)) {
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

    @Command(name = "leave")
    public void leave(Player player) {
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (!gamePlayer.isPlaying() && (gamePlayer.getState() != IGamePlayer.State.NOT_PLAYING)) {
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
        EntityLocation l = new EntityLocation(player.getLocation());
        Game game = GameManager.getGame(KOTH.class);
        if (args.length == 0) {
            player.sendMessage("/setkothtop <mapID>");
        } else {
            if (!game.hasRegion(args[0])) {
                player.sendMessage("§cPlease make a region for the map first (/registermap <game> <mapID>)");
                return;
            }
            if (game.hasMapID(args[0])) {
                StorageTagCompound compound = game.getCompound(args[0]);
                compound.setTag("scorePoint", l.toCompound());
                game.save(compound, args[0]);
            }
            player.sendMessage("§cSet score point for §7" + game.getName());
        }
    }

    @Command(name = "setparkourwin")
    public void setParkourScore(Player player, String[] args) {
        if (!player.hasPermission("events.setParkour")) return;
        EntityLocation l = new EntityLocation(player.getLocation());
        Game game = GameManager.getGame(Parkour.class);
        if (args.length == 0) {
            player.sendMessage("/setparkourwin <mapID>");
        } else {
            if (!game.hasRegion(args[0])) {
                player.sendMessage("§cPlease make a region for the map first (/registermap <game> <mapID>)");
                return;
            }
            if (game.hasMapID(args[0])) {
                StorageTagCompound compound = game.getCompound(args[0]);
                compound.setTag("scorePoint", l.toCompound());
                game.save(compound, args[0]);
            }
            player.sendMessage("§cSet score point for §7" + game.getName());
        }
    }

    @Command(name = "event")
    public void event(Player player, String[] args) {
        if (args.length == 0) {
            sendUssage(player);
        } else {
            if (args[0].equalsIgnoreCase("toggle")) {
                if (!player.hasPermission("event.toggle") && !player.hasPermission("event.*")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission."));
                    return;
                }
                if (args.length == 1) {
                    player.sendMessage("§c/event toggle <game>");
                    return;
                }

                Game game = GameManager.getGame(args[1]);
                if (game == null) {
                    sendUssage(player);
                    return;
                }
                boolean var = settings.getData().getBoolean("setup." + game.getName() + ".Enabled");
                settings.getData().set("setup." + game.getName() + ".Enabled", (!var));
                String value = "Disabled";
                if (!var) value = "Enabled";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7" + game.getName() + " has been " + value));
            } else if (args[0].equalsIgnoreCase("reload")) {
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
                if (plugin.getEventMain().waiting != null || plugin.getEventMain().eventstarted || plugin.getEventMain().eventstarting) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere is already an events in progress."));
                    return;
                }
                Game game = GameManager.getGame(args[0]);
                if (game == null) {
                    sendUssage(player);
                    return;
                }
                if (!settings.getData().isSet("setup." + game.getName() + ".Enabled"))
                    settings.getData().set("setup." + game.getName() + ".Enabled", true);
                if (!game.isSetup()) {
                    player.sendMessage("§cThe spawn point for " + game.getName() + " has not yet been set.");
                    return;
                }
                if (!settings.getData().getBoolean("setup." + game.getName() + ".Enabled")) {
                    player.sendMessage("§cThis Event is disabled.");
                    return;
                }
                double i = plugin.getConfig().getDouble("events.money.amount");
                plugin.getEventMain().eventstarting = true;
                plugin.getEventMain().cancelled = false;
                if (!game.getPlayers().isEmpty()) game.getPlayers().clear();
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
            if (!settings.getData().isSet("setup." + game.getName() + ".Enabled"))
                settings.getData().set("setup." + game.getName() + ".Enabled", true);
            if (settings.getData().getBoolean("setup." + game.getName() + ".Enabled")) {
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
