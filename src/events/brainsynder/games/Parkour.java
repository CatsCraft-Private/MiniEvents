package events.brainsynder.games;

import events.brainsynder.BlockLocation;
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

public class Parkour extends GameMaker {
    private BlockLocation topLocation = null;
    
    @Override public void onWin(IGamePlayer gamePlayer) {
        super.onWin(gamePlayer);
        Player o = gamePlayer.getPlayer();
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            EconomyResponse r = GamePlugin.econ.depositPlayer(o.getName(), i);
            if (r.transactionSuccess()) {
                o.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.got-money").replace("{0}", Double.toString(i))));
            }
        }
    }
    
    @Override public void onEnd() {
        super.onEnd();
    }
    
    @Override public void perTick() {
        super.perTick();
        if (topLocation == null) return;
        for (IGamePlayer player : players) {
            Player p = player.getPlayer();
            BlockLocation loc = new BlockLocation(p.getLocation());
            if (topLocation.atLocation(loc)) {
                endTask = true;
                onWin(player);
                onEnd();
                plugin.getEventMain().end();
            }
        }
    }
    
    @Override public void onStart() {
        if (settings.getData().getSection("setup." + getName()) == null) {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cThis game is not fully setup."));
            plugin.getEventMain().end();
        } else {
            if (!settings.getData().isSet("setup." + getName() + ".winLocation")) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cThis game is not fully setup."));
                plugin.getEventMain().end();
                return;
            }
            topLocation = BlockLocation.fromString(settings.getData().getString("setup." + getName() + ".winLocation"));
            
            for (IGamePlayer gamePlayer : players) {
                gamePlayer.getPlayerData().storeData(true);
                Player player = gamePlayer.getPlayer();
                equipPlayer(player);
                player.teleport(getSpawn());
            }
            super.onStart();
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
        player.getInventory().setArmorContents((ItemStack[]) null);
        player.getInventory().clear();
    }
    
    @Override public String getName() {
        return "Parkour";
    }
    
    @Override public String[] description() {
        return new String[] {
                "§7Parkour §eis a game where you jump from block",
                "§eto block in levels, but be careful if you fall",
                "§ethen you have to restart the level!!"
        };
    }
}
