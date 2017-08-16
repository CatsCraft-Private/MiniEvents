package events.brainsynder;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.PreGameStartEvent;
import events.brainsynder.events.player.*;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GamePlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class GameListener implements Listener {
    private GamePlugin plugin = GamePlugin.instance;

    @EventHandler
    public void onJoin(GamePlayerJoinEvent event) {
        IGamePlayer gamePlayer = event.getPlayer();
        event.getGame().addPlayer(gamePlayer);
        gamePlayer.setGame(event.getGame());
        gamePlayer.setState(IGamePlayer.State.WAITING);
        gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou &7joined the event. Players in the event: &b{1}&7.".replace("{1}", Integer.toString(event.getGame().getPlayer().size()))));
        for (IGamePlayer gamer : event.getGame().getPlayer()) {
            if (!gamer.getPlayer().getName().equals(gamePlayer.getPlayer().getName()))
                gamer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b{0} &7joined the event. Players in the event: &b{1}&7.".replace("{0}", gamePlayer.getPlayer().getName()).replace("{1}", Integer.toString(event.getGame().getPlayer().size()))));
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
            for (IGamePlayer gamePlayer : game.players) {
                if (gamePlayer.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId())) continue;
                if (game.deadPlayers.contains(gamePlayer)) continue;
                gamePlayer.getPlayer().sendMessage("§c" + player.getPlayer().getName() + " has left the event.");
            }
        } else {
            game.lost(player);
            for (IGamePlayer o : game.players) {
                if (o.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId())) continue;
                if (game.deadPlayers.contains(o)) continue;
                game.onWin(o);
                game.onEnd();
                plugin.getEventMain().end();
                break;
            }
        }
        game.players.remove(player);

    }

    @EventHandler
    public void onLeave(GameCountdownLeaveEvent event) {
        IGamePlayer gamePlayer = event.getPlayer();
        gamePlayer.getGame().removePlayer(gamePlayer);
        gamePlayer.setState(IGamePlayer.State.NOT_PLAYING);
        gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou &7left the event. Players in the event: &b{1}&7.".replace("{1}", Integer.toString(event.getGame().getPlayer().size()))));
        for (IGamePlayer gamer : gamePlayer.getGame().getPlayer()) {
            gamer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b{0} &7left the event. Players in the event: &b{1}&7.".replace("{0}", gamer.getPlayer().getName()).replace("{1}", Integer.toString((event.getGame().getPlayer().size())))));
        }
        gamePlayer.setGame(null);
    }

    @EventHandler
    public void onWin(GamePlayerWinEvent event) {
        IGamePlayer gamePlayer = event.getPlayer();
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b{PLAYER} &7just won &b" + event.getGame().getName() + '!').replace("{PLAYER}", gamePlayer.getPlayer().getName()));
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            EconomyResponse r = GamePlugin.econ.depositPlayer(gamePlayer.getPlayer(), i);
            if (r.transactionSuccess()) {
                gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.got-money").replace("{0}", Double.toString(i))));
            }
        }
    }

    @EventHandler
    public void onPreStart (PreGameStartEvent event) {
        Bukkit.getServer().getPluginManager().registerEvents(event.getGame(), plugin);
        Location spawn = event.getGame().getSpawn();
        for (IGamePlayer gamePlayer : event.getGame().players) {
            gamePlayer.getPlayerData().storeData(true);
            Player player = gamePlayer.getPlayer();
            player.teleport(spawn);
            gamePlayer.setState(IGamePlayer.State.IN_GAME_ARENA);
            event.getGame().equipPlayer(player);
        }
    }

    @EventHandler
    public void onLost(GamePlayerLostEvent event) {
        IGamePlayer player = event.getPlayer();
        if (player.getPlayerData().isStored())
            player.getPlayerData().restoreData();
        player.setGame(null);
        player.setState(IGamePlayer.State.NOT_PLAYING);
        event.getGame().deadPlayers.add(player);
    }

    @EventHandler
    public void onEnd (GameEndEvent event) {
        plugin.getEventMain().end();
        for (IGamePlayer player : event.getGame().players) {
            if (event.getGame().deadPlayers.contains(player)) continue;
            if (player.getPlayerData().isStored())
                player.getPlayerData().restoreData();
            player.setGame(null);
            player.setState(IGamePlayer.State.NOT_PLAYING);
        }
        event.getGame().deadPlayers.clear();
        event.getGame().players.clear();
        HandlerList.unregisterAll(event.getGame());
    }
}
