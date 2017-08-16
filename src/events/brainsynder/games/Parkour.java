package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.utils.BlockLocation;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Parkour extends GameMaker {
    private BlockLocation topLocation = null;
    
    @Override public void perTick() {
        super.perTick();
        if (topLocation == null) return;
        for (IGamePlayer player : players) {
            Player p = player.getPlayer();
            BlockLocation loc = new BlockLocation(p.getLocation());
            if (topLocation.atLocation(loc)) {
                endTask = true;
                onWin(player);
                plugin.getEventMain().end();
            }
        }
    }
    
    @Override public void onStart() {
        topLocation = BlockLocation.fromString(settings.getData().getString("setup." + getName() + ".winLocation"));
        super.onStart();
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
        player.getInventory().setArmorContents(null);
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
