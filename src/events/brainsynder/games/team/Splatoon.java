package events.brainsynder.games.team;

import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.TeamGameMaker;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.BlockSave;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
    public String getName() {
        return "Splatoon";
    }

    @Override
    public void equipPlayer(Player player) {
    }

    @Override
    public void equipDefaultPlayer(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack dsword = new ItemStack(Material.IRON_SWORD, 1);
        dsword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().clear();
        inventory.setItem(0, dsword);
        inventory.setItem(1, new ItemMaker(Material.BOW).enchant(Enchantment.ARROW_INFINITE, 1).create());
        inventory.setItem(8, new ItemMaker(Material.ARROW).create());
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
                "§6What is Splatoon?",
                "§e"
        };
    }
}
