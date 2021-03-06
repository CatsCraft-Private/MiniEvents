package events.brainsynder;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.PreGameStartEvent;
import events.brainsynder.events.player.*;
import events.brainsynder.events.team.TeamPlayerLeaveEvent;
import events.brainsynder.events.team.TeamWinEvent;
import events.brainsynder.games.team.SnowPack;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.nms.ITellraw;
import simple.brainsynder.utils.Reflection;

import java.util.ArrayList;
import java.util.List;

public class GameListener implements Listener {
    private GamePlugin plugin = GamePlugin.instance;

    @EventHandler
    public void onJoin(GamePlayerJoinEvent event) {
        if (plugin.getEventMain().eventstarted) return;

        Game game = event.getGame();
        IGamePlayer gamePlayer = event.getPlayer();
        gamePlayer.setGame(event.getGame());
        gamePlayer.setState(IGamePlayer.State.WAITING);
        event.getGame().players.add(gamePlayer.getPlayer().getName());
        gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou &7joined the event. Players in the event: &b{1}&7.".replace("{1}", Integer.toString(event.getGame().getPlayers().size()))));

        for (String name : game.getPlayers ()) {
            IGamePlayer gamer = GameManager.getPlayer(name);
            if (!gamer.getPlayer().getName().equals(gamePlayer.getPlayer().getName()))
                gamer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b{0} &7joined the event. Players in the event: &b{1}&7.".replace("{0}", gamePlayer.getPlayer().getName()).replace("{1}", Integer.toString(event.getGame().getPlayers().size()))));
        }
    }

    @EventHandler
    public void onLeave(GamePlayerLeaveEvent event) {
        IGamePlayer player = event.getPlayer();
        Game game = event.getGame();
        if (player.getPlayerData().isStored())
            player.getPlayerData().restoreData();
        player.setGame(null);
        player.setState(IGamePlayer.State.NOT_PLAYING);
        if (game.aliveCount() > 2) {
            game.lost(player);
            for (String name : game.getPlayers ()) {
                IGamePlayer gamePlayer = GameManager.getPlayer(name);
                if (gamePlayer.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId())) continue;
                if (game.deadPlayers.contains(name)) continue;
                gamePlayer.getPlayer().sendMessage("§c" + player.getPlayer().getName() + " has left the event.");
            }
        } else {
            game.lost(player);
            for (String name : game.getPlayers ()) {
                IGamePlayer o = GameManager.getPlayer(name);
                if (o.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId())) continue;
                if (game.deadPlayers.contains(name)) continue;
                game.onWin(o);
                game.onEnd();
                plugin.getEventMain().end();
                break;
            }
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "scoretoggle " + player.getPlayer().getName() + " true");
        game.players.remove(player.getPlayer().getName());
    }

    @EventHandler
    public void onLeave(TeamPlayerLeaveEvent event) {
        IGamePlayer player = event.getPlayer();
        ITeamGame game = event.getGame();
        if (player.getPlayerData().isStored())
            player.getPlayerData().restoreData();
        player.setGame(null);
        player.setState(IGamePlayer.State.NOT_PLAYING);
        if (game.aliveCount() > 2) {
            if (player.getPlayerData().isStored())
                player.getPlayerData().restoreData();
            player.setGame(null);
            player.setState(IGamePlayer.State.NOT_PLAYING);
            event.getGame().deadPlayers.add(player.getPlayer().getName());
            for (String name : game.getPlayers ()) {
                IGamePlayer gamePlayer = GameManager.getPlayer(name);
                if (gamePlayer.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId())) continue;
                if (game.deadPlayers.contains(name)) continue;
                gamePlayer.getPlayer().sendMessage("§c" + player.getPlayer().getName() + " has left the event.");
            }
        } else {
            if (player.getPlayerData().isStored())
                player.getPlayerData().restoreData();
            player.setGame(null);
            player.setState(IGamePlayer.State.NOT_PLAYING);
            event.getGame().deadPlayers.add(player.getPlayer().getName());

            for (String name : game.getPlayers ()) {
                IGamePlayer o = GameManager.getPlayer(name);
                if (o.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId())) continue;
                if (game.deadPlayers.contains(name)) continue;
                game.onWin(event.getTeam());
                game.onEnd();
                break;
            }
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "scoretoggle " + player.getPlayer().getName() + " true");
        player.setTeam(null);
        game.players.remove(player.getPlayer().getName());
    }

    @EventHandler
    public void onLeave(GameCountdownLeaveEvent event) {
        Game game = event.getGame();
        IGamePlayer gamePlayer = event.getPlayer();
        gamePlayer.getGame().removePlayer(gamePlayer.getPlayer().getName());
        gamePlayer.setState(IGamePlayer.State.NOT_PLAYING);
        gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou &7left the event. Players in the event: &b{1}&7.".replace("{1}", Integer.toString(event.getGame().getPlayers().size()))));
        for (String name : game.getPlayers ()) {
            IGamePlayer gamer = GameManager.getPlayer(name);
            gamer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b{0} &7left the event. Players in the event: &b{1}&7.".replace("{0}", gamePlayer.getPlayer().getName()).replace("{1}", Integer.toString((event.getGame().getPlayers().size())))));
        }
        gamePlayer.setGame(null);
    }

    @EventHandler
    public void onWin(GamePlayerWinEvent event) {
        IGamePlayer gamePlayer = event.getPlayer();
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b{PLAYER} &7just won &b" + event.getGame().getName() + '!').replace("{PLAYER}", gamePlayer.getPlayer().getName()));
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "eco give " + gamePlayer.getPlayer().getName() + " " + i);
        }
    }

    @EventHandler
    public void onWin(TeamWinEvent event) {
        Team team = event.getTeam();

        if (event.getGame() instanceof SnowPack) {
            String winning;

            switch (team.getName()) {
                case "Red":
                    winning = team.getChatColor() + "Runners";
                    break;
                default:
                    winning = team.getChatColor() + "Snowmen";
                    break;
            }

            ITellraw raw = Reflection.getTellraw("§7The " + winning + " §7just won §b" + event.getGame().getName() + "§7!");
            Bukkit.getOnlinePlayers().forEach(raw::send);
            if (plugin.getConfig().getBoolean("events.money.enabled")) {
                double amount = plugin.getConfig().getDouble("events.money.amount");
                int c = 0;
                for (String name : team.getMembers()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            IGamePlayer gamePlayer = GameManager.getPlayer(name);
                            EconomyResponse response = GamePlugin.econ.depositPlayer(gamePlayer.getPlayer(), amount);
                            if (response.type == EconomyResponse.ResponseType.SUCCESS) {
                                gamePlayer.getPlayer().sendMessage("§aYou have been awarded $" + amount);
                            }
                        }
                    }.runTaskLater(GamePlugin.instance, c+20);
                    c++;
                }
            }
            event.getGame().getRedTeam().getMembers().clear();
            event.getGame().getBlueTeam().getMembers().clear();
            return;
        }

        List<String> redMem = new ArrayList<>();
        redMem.add("§4Red Team Members:");
        List<String> blueMem = new ArrayList<>();
        blueMem.add("§9Blue Team Members:");

        event.getGame().getRedTeam().getMembers().forEach(name -> redMem.add("§c- §7" + GameManager.getPlayer(name).getPlayer().getName()));
        event.getGame().getBlueTeam().getMembers().forEach(name -> blueMem.add("§b- §7" + GameManager.getPlayer(name).getPlayer().getName()));

        ITellraw raw = Reflection.getTellraw(team.getChatColor() + team.getName() + " Team §7just won §b" + event.getGame().getName() + "§7! Final Score of: ");
        raw.then("§4Red(§c" + ((int) event.getGame().getRedTeam().getScore()) + "§4)").tooltip(redMem).then(' ');
        raw.then("§9Blue(§b" + ((int) event.getGame().getBlueTeam().getScore()) + "§9)").tooltip(blueMem);
        Bukkit.getOnlinePlayers().forEach(raw::send);
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double amount = plugin.getConfig().getDouble("events.money.amount");
            team.getMembers().forEach(name -> {
                IGamePlayer gamePlayer = GameManager.getPlayer(name);
                EconomyResponse response = GamePlugin.econ.depositPlayer(gamePlayer.getPlayer(), amount);
                if (response.type == EconomyResponse.ResponseType.SUCCESS) {
                    gamePlayer.getPlayer().sendMessage("§aYou have been awarded $" + amount);
                }
            });
        }
        event.getGame().getRedTeam().getMembers().clear();
        event.getGame().getBlueTeam().getMembers().clear();
        redMem.clear();
        blueMem.clear();
    }

    @EventHandler
    public void onPreStart(PreGameStartEvent event) {
        plugin.getEventMain().eventstarting = false;
        plugin.getEventMain().eventstarted = true;
        plugin.getEventMain().waiting = null;
        Bukkit.getServer().getPluginManager().registerEvents(event.getGame(), plugin);
        if (event.getGame() instanceof ITeamGame) {
            ITeamGame game = (ITeamGame) event.getGame();
            for (String name : game.getPlayers ()) {
                IGamePlayer gamePlayer = GameManager.getPlayer(name);
                gamePlayer.getPlayerData().storeData(true);
                Player player = gamePlayer.getPlayer();
                gamePlayer.setState(IGamePlayer.State.IN_GAME_ARENA);
                event.getGame().equipPlayer(player);
                event.getGame().onScoreboardLoad(gamePlayer);
            }
            return;
        }
        Game game = event.getGame();

        Location spawn = event.getGame().getSpawn();
        for (String name : game.getPlayers ()) {
            IGamePlayer gamePlayer = GameManager.getPlayer(name);
            gamePlayer.getPlayerData().storeData(true);
            Player player = gamePlayer.getPlayer();
            player.teleport(spawn);
            gamePlayer.setState(IGamePlayer.State.IN_GAME_ARENA);
            event.getGame().equipPlayer(player);
            event.getGame().onScoreboardLoad(gamePlayer);
        }
    }

    @EventHandler
    public void onLost(GamePlayerLostEvent event) {
        IGamePlayer player = event.getPlayer();
        if (player.getPlayerData().isStored())
            player.getPlayerData().restoreData();
        player.setGame(null);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "scoretoggle " + player.getPlayer().getName() + " true");
        player.setState(IGamePlayer.State.NOT_PLAYING);
        event.getGame().deadPlayers.add(player.getPlayer().getName());
    }

    @EventHandler
    public void onEnd(GameEndEvent event) {
        plugin.getEventMain().end();
        Game game = event.getGame();
        for (String name : game.getPlayers ()) {
            IGamePlayer player = GameManager.getPlayer(name);
            player.setScoreHandler(null);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "scoretoggle " + player.getPlayer().getName() + " true");
            if (event.getGame().deadPlayers.contains(player.getPlayer().getName())) continue;
            if (player.getPlayerData().isStored())
                player.getPlayerData().restoreData();
            player.setGame(null);
            player.setState(IGamePlayer.State.NOT_PLAYING);
        }

        game.deadPlayers.clear();
        game.players.clear();
        GameManager.gamePlayerMap.clear();
        HandlerList.unregisterAll(event.getGame());
    }
}
