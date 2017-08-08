package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class KOTH extends GameMaker {
    private HashMap<UUID, Integer> points = new HashMap<>();
    private Location topLocation = null;
    private int per20 = 0;
    
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
    
    @Override public void onEnd() {
        super.onEnd();
        points.clear();
    }
    
    @Override public void perTick() {
        super.perTick();
        if (topLocation == null) return;
        
        for (IGamePlayer gamePlayer : players) {
            Player o = gamePlayer.getPlayer();
            int point = points.getOrDefault(o.getUniqueId(), 1);
            int l = ((point * 100) / 100);
            if (per20 == 20) {
                per20 = 0;
                
                /*StringBuilder text = new StringBuilder();
                text.append("Progress Bar: ");
                text.append("| ");
                int greenNum = per10.getOrDefault(o.getUniqueId(), 0);
                if (l % 10 == 0) {
                    greenNum++;
                    per10.put(o.getUniqueId(), greenNum);
                }
                text.append(ChatColor.GREEN);
                for (int i = 0; i < greenNum; i++) {
                    text.append('■');
                }
                text.append(ChatColor.RED);
                for (int i = 0; i < (10 - greenNum); i++) {
                    text.append('■');
                }
                text.append(ChatColor.RESET).append(" | ").append(l).append('%');
                
                IActionMessage message = Reflection.getActionMessage();
                message.sendMessage(o, text.toString());*/
                
                if (o.getLocation().distance(topLocation) <= 3.0) {
                    if (point < 100) {
                        points.put(o.getUniqueId(), (point + 1));
                        if (l % 10 == 0) {
                            o.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getConfig().getString("messages.koth-announce-player")
                                            .replace("{0}", Integer.toString(l))));
                        }
                        
                        if (l >= 50 && l % 10 == 0) {
                            for (IGamePlayer p : players) {
                                Player pl = p.getPlayer();
                                pl.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getConfig().getString("messages.koth-announce")
                                                .replace("{0}", o.getName())
                                                .replace("{1}", Integer.toString(l))));
                            }
                        }
                    } else {
                        onWin(gamePlayer);
                        plugin.getEventMain().end();
                        break;
                    }
                }
            } else {
                per20++;
            }
        }
    }
    
    @Override public void onStart() {
        if (settings.getData().getSection("setup." + getName()) == null) {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cThis game is not fully setup."));
            plugin.getEventMain().end();
        } else {
            if (settings.getData().getSection("setup." + getName() + ".top") == null) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cThis game is not fully setup."));
                plugin.getEventMain().end();
                return;
            }
            Location spawn = getSpawn();
            World world = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ".top.world"));
            double x = settings.getData().getDouble("setup." + getName() + ".top.x");
            double y = settings.getData().getDouble("setup." + getName() + ".top.y");
            double z = settings.getData().getDouble("setup." + getName() + ".top.z");
            float yaw = (settings.getData().getInt("setup." + getName() + ".top.yaw"));
            float pitch = (settings.getData().getInt("setup." + getName() + ".top.pitch"));
            topLocation = new Location(world, x, y, z, yaw, pitch);
            
            for (IGamePlayer gamePlayer : players) {
                gamePlayer.getPlayerData().storeData(true);
                Player player = gamePlayer.getPlayer();
                equipPlayer(player);
                player.teleport(spawn);
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
    
    @Override public boolean allowsPVP() {
        return true;
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            IGamePlayer gamePlayer = GameManager.getPlayer(player);
            if (gamePlayer.isPlaying()) {
                if (!(gamePlayer.getGame() instanceof KOTH)) return;
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
    }
    
    @Override public String getName() {
        return "KOTH";
    }
    
    @Override public String[] description() {
        return new String[]{
                "§eKOTH or known as §7King of the Hill",
                "§eis a game of capture, meaning you have to",
                "§estand at a certain spot to gain points",
                "§efirst person to 100 wins!"
        };
    }
}
