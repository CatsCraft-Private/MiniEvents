package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KO extends GameMaker {
    
    @Override public boolean allowsPVP() {
        return true;
    }
    
    @Override public void onWin(IGamePlayer gamePlayer) {
        onEnd();
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b{PLAYER} &7just won &b" + getName() + '!').replace("{PLAYER}", gamePlayer.getPlayer().getName()));
        Player o = gamePlayer.getPlayer();
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            EconomyResponse r = GamePlugin.econ.depositPlayer(o, i);
            if (r.transactionSuccess()) {
                o.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.got-money").replace("{0}", Double.toString(i))));
            }
        }
    }
    
    @Override public void perTick() {
        super.perTick();
        if (endTask) return;
        for (IGamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
            if (player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER)
                    || player.getLocation().getBlock().getType().equals(Material.WATER)
                    || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.STATIONARY_WATER)
                    || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.WATER)) {
                if (aliveCount() > 2) {
                    lost(gamePlayer);
                } else if (aliveCount() == 2) {
                    endTask = true;
                    lost(gamePlayer);
                    for (IGamePlayer o : players) {
                        if (o.getPlayer().getUniqueId().equals(player.getUniqueId())) continue;
                        if (deadPlayers.contains(o)) continue;
                        onWin(o);
                        plugin.getEventMain().end();
                        break;
                    }
                }
                return;
            }
        }
    }
    
    @Override public void onStart() {
        if (settings.getData().getSection("setup." + getName()) == null) {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.spleef-spawn-not-set")));
            plugin.getEventMain().end();
        } else {
            for (IGamePlayer gamePlayer : players) {
                gamePlayer.getPlayerData().storeData(true);
                Player player = gamePlayer.getPlayer();
                equipPlayer(player);
                player.teleport(getSpawn());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 5 seconds of invincibility."));
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (players.size() != 0) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are no longer invincible."));
                        super.onStart();
                    }
                }, 120L);
            }
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
        ItemStack stick = new ItemStack(Material.STICK);
        stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        ItemStack bhelmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta abhelmet = (LeatherArmorMeta)bhelmet.getItemMeta();
        abhelmet.setColor(Color.fromRGB(255, 179, 0));
        bhelmet.setItemMeta(abhelmet);
        player.getInventory().setHelmet(bhelmet);
        ItemStack bchestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta abchest = (LeatherArmorMeta)bchestplate.getItemMeta();
        abchest.setColor(Color.fromRGB(255, 179, 0));
        bchestplate.setItemMeta(abchest);
        player.getInventory().setChestplate(bchestplate);
        ItemStack bleggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta ableg = (LeatherArmorMeta)bleggings.getItemMeta();
        ableg.setColor(Color.fromRGB(255, 179, 0));
        bleggings.setItemMeta(ableg);
        player.getInventory().setLeggings(bleggings);
        ItemStack bboots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta abboots = (LeatherArmorMeta)bboots.getItemMeta();
        abboots.setColor(Color.fromRGB(255, 179, 0));
        bboots.setItemMeta(abboots);
        player.getInventory().setBoots(bboots);
        player.getInventory().clear();
        inventory.setItem(0, stick);
    }
    
    @Override public String getName() {
        return "KO";
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            IGamePlayer gamePlayer = GameManager.getPlayer(player);
            if (gamePlayer.isPlaying()) {
                if (!(gamePlayer.getGame() instanceof KO)) return;
                if (players.contains(gamePlayer)) {
                    if (!plugin.getEventMain().eventstarted) {
                        event.setCancelled(true);
                        return;
                    }
                    event.setDamage(0.0);
                }
            }
        }
    }
    
    @Override public String[] description() {
        return new String[]{
                "§eKO or known as §7Knock Out",
                "§eis a game where you have to knock",
                "§eplayers into the water below...",
                "§ewatch out, they are doing the same to you..."
        };
    }
    @Override
    public void onEnd() {
        super.onEnd();
    }
}

