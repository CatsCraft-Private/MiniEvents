package events.brainsynder;

import events.brainsynder.events.player.GameCountdownLeaveEvent;
import events.brainsynder.games.Spleef;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import me.vagdedes.spartan.api.CheckCancelEvent;
import me.vagdedes.spartan.api.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;

/**
 * Handles all the Events in the plugin. (Well... Most of them XD)
 */
public class Handle implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getEntity());
            if (gamePlayer.isPlaying()) {
                if (gamePlayer.getGame().getGameSettings().canPvp()) return;
                event.setCancelled(true);
                return;
            }
        }
        if (event.getDamager() instanceof Player) {
            IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getDamager());
            if (gamePlayer.isPlaying()) {
                if (gamePlayer.getGame().getGameSettings().canPvp()) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCheck(PlayerViolationEvent e) {
        IGamePlayer gamePlayer = GameManager.getPlayer(e.getPlayer());
        if (gamePlayer.isPlaying()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCheck(CheckCancelEvent e) {
        IGamePlayer gamePlayer = GameManager.getPlayer(e.getPlayer());
        if (gamePlayer.isPlaying()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHurt(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getEntity());
            if (gamePlayer.isPlaying()) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    if (gamePlayer.getGame().getGameSettings().canTakeFallDmg()) return;
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (!gamePlayer.isPlaying() && (gamePlayer.getState() != IGamePlayer.State.WAITING)) {
            return;
        }

        if (GamePlugin.instance.getEventMain().eventstarted) {
            gamePlayer.getGame().onLeave(gamePlayer);
            return;
        }
        if ((!GamePlugin.instance.getEventMain().eventstarting) && (!GamePlugin.instance.getEventMain().eventstarted)) {
            return;
        }
        GameCountdownLeaveEvent<Game> e = new GameCountdownLeaveEvent<>(gamePlayer.getGame(), gamePlayer);
        Bukkit.getPluginManager().callEvent(e);
    }

    @EventHandler
    public void onLeave(PlayerKickEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (!gamePlayer.isPlaying() && (gamePlayer.getState() != IGamePlayer.State.WAITING)) {
            return;
        }

        if (GamePlugin.instance.getEventMain().eventstarted) {
            gamePlayer.getGame().onLeave(gamePlayer);
            return;
        }
        if ((!GamePlugin.instance.getEventMain().eventstarting) && (!GamePlugin.instance.getEventMain().eventstarted)) {
            return;
        }
        GameCountdownLeaveEvent<Game> e = new GameCountdownLeaveEvent<>(gamePlayer.getGame(), gamePlayer);
        Bukkit.getPluginManager().callEvent(e);
    }

    @EventHandler
    public void onChat(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        try {
            IGamePlayer gamePlayer = GameManager.getPlayer(player);
            if (gamePlayer.isPlaying()) {
                if (player.hasPermission("event.usecommands")) return;
                if (event.getMessage().contains("event") || event.getMessage().contains("leave")) return;
                event.setCancelled(true);
            }
        }catch (Exception ignored){}
    }

    @EventHandler
    public void blockBreakEvent(final BlockBreakEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting) {
                if (gamePlayer.getGame() instanceof Spleef) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockBreakEvent(final BlockPlaceEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreakEvent(PlayerDropItemEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreakEvent(PlayerPickupItemEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getEntity().getShooter());
            if (gamePlayer.isPlaying()) {
                if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting)
                    if (event.getEntity() instanceof Arrow) event.getEntity().remove();
            }
        }
    }

    @EventHandler
    public void hungerLoss(FoodLevelChangeEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getEntity());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }
}
