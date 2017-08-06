package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ParticleMaker;
import simple.brainsynder.sound.SoundMaker;

import java.util.Arrays;
import java.util.Random;

public class Paintball extends GameMaker {
    
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
        return "Paintball";
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
    
    public void runThis(final Item item, final Player player) {
        item.setVelocity(player.getLocation().getDirection().multiply(3.0D));
        item.setPickupDelay(2147483647);
        SoundMaker.ENTITY_CHICKEN_EGG.playSound(item.getLocation(), 1.0F, 1.0F);
        new BukkitRunnable() {
            public void run() {
                for (Entity entity : item.getNearbyEntities(0.8D, 0.8D, 0.8D)) {
                    if (entity instanceof Player) {
                        Player ent = (Player) entity;
                        if (!ent.getUniqueId().equals(player.getUniqueId())) {
                            ent.damage(plugin.getConfig().getDouble("events.paintball-damage"), player);
                            ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.SMOKE_LARGE, 20, 0.5);
                            maker.sendToLocation(entity.getLocation());
                            item.remove();
                        }
                    }
                }
                cancel();
            }
        }.runTaskTimer(plugin, 0, 1);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (item != null && item.isValid())
            item.remove();
        }, 15);
    }
    
    @Override public void equipDefaultPlayer(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack stick = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta sstick = stick.getItemMeta();
        sstick.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("events.paintball-item-name")));
        sstick.setLore(Arrays.asList(new String[]{ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("events.paintball-item-lore"))}));
        stick.setItemMeta(sstick);
        ItemStack bhelmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta abhelmet = (LeatherArmorMeta) bhelmet.getItemMeta();
        abhelmet.setColor(Color.fromRGB(135, 206, 250));
        bhelmet.setItemMeta(abhelmet);
        player.getInventory().setHelmet(bhelmet);
        ItemStack bchestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta abchest = (LeatherArmorMeta) bchestplate.getItemMeta();
        abchest.setColor(Color.fromRGB(0, 255, 127));
        bchestplate.setItemMeta(abchest);
        player.getInventory().setChestplate(bchestplate);
        ItemStack bleggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta ableg = (LeatherArmorMeta) bleggings.getItemMeta();
        ableg.setColor(Color.fromRGB(135, 206, 250));
        bleggings.setItemMeta(ableg);
        player.getInventory().setLeggings(bleggings);
        ItemStack bboots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta abboots = (LeatherArmorMeta) bboots.getItemMeta();
        abboots.setColor(Color.fromRGB(0, 255, 127));
        bboots.setItemMeta(abboots);
        player.getInventory().setBoots(bboots);
        player.getInventory().clear();
        inventory.setItem(0, stick);
    }
    
    @Override public boolean allowsPVP() {
        return true;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        IGamePlayer player = GameManager.getPlayer((Player) event.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof Paintball) {
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
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        int blockId = player.getItemInHand().getType().getId();
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (gamePlayer.isPlaying() && gamePlayer.getGame().hasStarted()) {
            if (gamePlayer.getGame() instanceof Paintball) {
                event.setCancelled(true);
                if (!plugin.getEventMain().eventstarted) {
                    return;
                }
    
                int i = plugin.getConfig().getInt("events.paintball-item");
                if ((blockId == i
                        && event.getAction().equals(Action.RIGHT_CLICK_AIR)
                        || blockId == 293 && event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
                    Random dice = new Random();
                    int number = dice.nextInt(6);
                    switch (number) {
                        case 1:
                            Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.INK_SACK, 1, (short) 8));
                            runThis(item, player);
                            break;
                        case 2:
                            Item item2 = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.INK_SACK, 1, (short) 9));
                            runThis(item2, player);
                            break;
                        case 3:
                            Item item3 = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.INK_SACK, 1, (short) 10));
                            runThis(item3, player);
                            break;
                        case 4:
                            Item item4 = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.INK_SACK, 1, (short) 5));
                            runThis(item4, player);
                            break;
                        case 5:
                            Item item5 = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.INK_SACK, 1, (short) 13));
                            runThis(item5, player);
                    }
                }
            }
        }
    }
    
    @Override public String[] description() {
        return new String[] {
                "§6What is Paintball?",
                "§ePaintball is a game where",
                "§eyou shoot players with 'Paint'",
                "in order to kill them."
        };
    }
}
