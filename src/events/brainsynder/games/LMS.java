package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LMS extends GameMaker {
    
    @Override public void onWin(IGamePlayer gamePlayer) {
        super.onWin(gamePlayer);
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            EconomyResponse r = GamePlugin.econ.depositPlayer(gamePlayer.getPlayer(), i);
            if (r.transactionSuccess()) {
                gamePlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.got-money").replace("{0}", Double.toString(i))));
            }
        }
    }
    
    @Override public String getName() {
        return "LMS";
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
                player.getInventory().setArmorContents(null);
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
        ItemStack dsword = new ItemStack(Material.DIAMOND_SWORD, 1);
        dsword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().clear();
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        inventory.setItem(0, dsword);
    
        for(int i = 0; i < inventory.getSize(); ++i) {
            if(inventory.getItem(i) == null) {
                inventory.addItem(new ItemStack(Material.MUSHROOM_SOUP));
            }
        }
    }
    
    @Override public boolean allowsPVP() {
        return true;
    }
    
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && (!(event.getDamager() instanceof Projectile))) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        IGamePlayer player = GameManager.getPlayer(p);
        if (player.isPlaying()) {
            if (player.getGame() instanceof LMS) {
                if (!plugin.getEventMain().eventstarted) {
                    event.setCancelled(true);
                    return;
                }
                if ((p.getHealth() - event.getDamage()) <= 1) {
                    event.setCancelled(true);
                    p.setHealth(p.getMaxHealth());
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
        return new String[] {
                "§6What is LMS?",
                "§eLMS Stands for §7Last Man Standing",
                "§eBe the last player alive, and you win!"
        };
    }
    @Override
    public void onEnd() {
        super.onEnd();
    }
}
