package events.brainsynder.games.team;

import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.TeamGameMaker;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.BlockSave;
import events.brainsynder.utils.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ItemMaker;

import java.util.HashMap;
import java.util.Map;


/**
 * Ideas:
 * - Use a Map that contains the Team,BlockLocation and check that Map to check how many blocks the Team painted
 * - if TEAM painted over OTHER TEAM it will subtract that from the other teams blocks.
 * - Paint Bombs (Radius: Random [3-5])
 * - 3 min game time (180 seconds)
 */
public class Splatoon extends TeamGameMaker {
    /**
     * String = Team name
     * Map.String = BlockLocation.toDataString()
     * Map.BlockSave = Original block Data.
     */
    private Map<String, Map<String, BlockSave>> teamBlocks = null;

    public Splatoon() {
        super();
        teamBlocks = new HashMap<>();
        setGameSettings(new GameSettings(true));
    }

    @Override
    public void onStart() {
        super.onStart();
        new BukkitRunnable() {
            @Override
            public void run() {
                players.forEach(player -> {
                    try {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pet remove " + player.getPlayer().getName());
                    }catch (Throwable ignored){}
                });
            }
        }.runTaskLater(plugin, 30);
    }

    @Override
    public String getName() {
        return "Splatoon";
    }

    @Override
    public void equipPlayer(Player player) {
    }

    private ItemStack getRedGun() {
        ItemMaker maker = new ItemMaker (Material.GOLD_BARDING);
        maker.setName("§c§lRed Paint Cannon");
        return maker.create();
    }
    private ItemStack getBlueGun() {
        ItemMaker maker = new ItemMaker (Material.DIAMOND_BARDING);
        maker.setName("§9§lBlue Paint Cannon");
        return maker.create();
    }

    private void colorBlocks(Location location, IGamePlayer gamePlayer) {
        BlockUtils.getBlocksInRadius(location, -1, false).forEach(block -> {
            if (block == null) return;
            if (block.getType() == Material.AIR) return;
            if (block.getType().isSolid()) {
                if (block.getType() == Material.STONE_SLAB2) return;
                if (block.getType() == Material.DOUBLE_STONE_SLAB2) return;
                if (block.getType() == Material.PURPUR_DOUBLE_SLAB) return;
                if (block.getType() == Material.PURPUR_SLAB) return;

            }
        });
    }

    @Override
    public void equipDefaultPlayer(Player player) {
        player.getInventory().clear();
        Inventory inventory = player.getInventory();
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (gamePlayer.getTeam() != null) {
            if (gamePlayer.getTeam().getName().equals("Red")) {
                inventory.setItem(0, getRedGun());
            }else{
                inventory.setItem(0, getBlueGun());
            }
        }
    }

    @Override
    public boolean allowsPVP() {
        return true;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && (!(event.getDamager() instanceof Projectile))) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        IGamePlayer player = GameManager.getPlayer(p);
        if (player.isPlaying()) {
            if (player.getGame() instanceof Splatoon) {
                if (!plugin.getEventMain().eventstarted) {
                    event.setCancelled(true);
                    return;
                }
                Player enemy = null;
                if (event.getDamager() instanceof Player) {
                    enemy = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) event.getDamager();
                    if (!(projectile.getShooter() instanceof Player)) return;
                    enemy = (Player) projectile.getShooter();
                }

                if (enemy != null) {
                    IGamePlayer<ITeamGame> hitter = GameManager.getPlayer(enemy);
                    if (hitter.getTeam().getName().equals(player.getTeam().getName())) {
                        event.setCancelled(true);
                        return;
                    }
                }


                if ((p.getHealth() - event.getDamage()) <= 1) {
                    event.setCancelled(true);
                    p.setHealth(p.getMaxHealth());
                    p.teleport(getSpawn(player.getTeam()));
                }
            }
        }
    }

    @EventHandler
    public void onRegen (EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        IGamePlayer player = GameManager.getPlayer((Player) e.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof Splatoon) {
                if (plugin.getEventMain().eventstarted)
                    e.setCancelled(true);
            }
        }
    }

    @Override
    public String[] description() {
        return new String[]{
                "§6Splatoon Objective:",
                "§ePaint the most blocks"
        };
    }
}
