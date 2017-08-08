package events.brainsynder;

import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles all the Events in the plugin. (Well... Most of them XD)
 */
public class Handle implements Listener {
    
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getEntity());
            if (gamePlayer.isPlaying()) {
                if (gamePlayer.getGame().allowsPVP ()) return;
                event.setCancelled(true);
                return;
            }
        }
        if (event.getDamager() instanceof Player) {
            IGamePlayer gamePlayer = GameManager.getPlayer((Player) event.getDamager());
            if (gamePlayer.isPlaying()) {
                if (gamePlayer.getGame().allowsPVP ()) return;
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            gamePlayer.getGame().onLeave(gamePlayer);
        }
    }
    
    @EventHandler
    public void onChat(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (gamePlayer.isPlaying()) {
            if (player.hasPermission("event.usecommands")) return;
            if (event.getMessage().contains("event") || event.getMessage().contains("leave")) return;
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void blockBreakEvent(final BlockBreakEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void blockBreakEvent(final BlockPlaceEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted
                    || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void blockBreakEvent(PlayerDropItemEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted
                    || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void blockBreakEvent(PlayerPickupItemEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        if (gamePlayer.isPlaying()) {
            if (GamePlugin.instance.getEventMain().eventstarted
                    || GamePlugin.instance.getEventMain().eventstarting)
                event.setCancelled(true);
        }
    }
}
