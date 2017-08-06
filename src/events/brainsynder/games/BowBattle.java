package events.brainsynder.games;

import events.brainsynder.key.*;
import events.brainsynder.managers.GameManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BowBattle extends GameMaker {
    
    @Override public void onWin(IGamePlayer gamePlayer) {
        super.onWin(gamePlayer);
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            EconomyResponse r = plugin.econ.depositPlayer(gamePlayer.getPlayer().getName(), i);
            if (r.transactionSuccess()) {
                gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.got-money").replace("{0}", Double.toString(i))));
            }
        }
    }
    
    @Override public String getName() {
        return "BowBattle";
    }
    
    @Override public void onStart() {
        for (IGamePlayer gamePlayer : players) {
            gamePlayer.getPlayerData().storeData(true);
            Player player = gamePlayer.getPlayer();
            equipPlayer(player);
            player.teleport(getSpawn());
    
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 5 seconds of invincibility."));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (players.size() != 0) {
                    super.onStart();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are no longer invincible."));
                }
                
            }, 120L);
        }
    }
    
    @Override public void equipPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        try {
            if (settings.getData().getSection("setup." + getName()) == null) {
                equipDefaultPlayer(player);
            } else {
                player.getInventory().clear();
                player.getInventory().setArmorContents((ItemStack[]) null);
                for (String m : settings.getData().getSection("setup." + getName() + ".inv.").getKeys(false)) {
                    player.getInventory().setItem(Integer.parseInt(m), settings.getData().getItemStack("setup." + getName() + ".inv." + m));
                }
            
                player.getInventory().setHelmet(settings.getData().getItemStack("setup." + getName() + ".armor.103"));
                player.getInventory().setChestplate(settings.getData().getItemStack("setup." + getName() + ".armor.102"));
                player.getInventory().setLeggings(settings.getData().getItemStack("setup." + getName() + ".armor.101"));
                player.getInventory().setBoots(settings.getData().getItemStack("setup." + getName() + ".armor.100"));
                if (settings.getData().getSection("setup." + getName() + ".potion") != null) {
                    for (String m : settings.getData().getSection("setup." + getName() + ".potion.name.").getKeys(false)) {
                        String name = settings.getData().getString("setup." + getName() + ".potion.name." + m + ".type");
                        int amplifier = settings.getData().getInt("setup." + getName() + ".potion.name." + m + ".level");
                        int duration = settings.getData().getInt("setup." + getName() + ".potion.name." + m + ".duration");
                        player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(name), duration, amplifier));
                    }
                }
            }
        } catch (Exception e) {
            equipDefaultPlayer(player);
        }
    }
    
    @Override public void equipDefaultPlayer(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
        player.getInventory().clear();
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        inventory.setItem(0, bow);
        inventory.setItem(9, new ItemStack(Material.ARROW, 1));
        inventory.setItem(1, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(2, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(3, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(4, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(5, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(6, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(7, new ItemStack(Material.MUSHROOM_SOUP));
        inventory.setItem(8, new ItemStack(Material.MUSHROOM_SOUP));
    }
    
    @Override public boolean allowsPVP() {
        return true;
    }
    
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && (!(event.getDamager() instanceof Projectile))) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
            return;
        }
        IGamePlayer player = GameManager.getPlayer((Player) event.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof BowBattle) {
                if (!plugin.getEventMain().eventstarted) {
                    event.setCancelled(true);
                    return;
                }
                Player p = (Player) event.getEntity();
                if ((p.getHealth() - event.getDamage()) <= 1) {
                    event.setCancelled(true);
                    if (aliveCount() > 2) {
                        lost(player);
                    } else if (aliveCount() == 2) {
                        lost(player);
                        for (IGamePlayer o : players) {
                            if (o.getPlayer().getUniqueId().equals(p.getUniqueId())) continue;
                            if (deadPlayers.contains(o)) continue;
                            onWin(o);
                            onEnd();
                            plugin.getEventMain().end();
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @Override public String[] description() {
        return new String[]{
                "§eBattle other players with Bows",
                "§eNone of those fancy 'Sword' and stuff",
                "§eJust good ol' fashion Bow and Arrows..."
        };
    }
}
