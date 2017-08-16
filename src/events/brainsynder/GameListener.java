package events.brainsynder;

import events.brainsynder.events.player.GameCountdownLeaveEvent;
import events.brainsynder.events.player.GamePlayerJoinEvent;
import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameListener implements Listener {
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
        IGamePlayer gamePlayer = event.getPlayer();

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
}
