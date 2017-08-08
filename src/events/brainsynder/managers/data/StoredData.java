package events.brainsynder.managers.data;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class StoredData {
    private final Player player;
    private ItemStack[] storedInventory;
    private ItemStack[] storedArmor;
    private Location storedLocation;
    private Collection<PotionEffect> storedEffects;
    private GameMode storedGameMode;
    private float storedExp;
    private int storedLevel;
    private int storedFireTicks;
    private double storedMaxHealth;
    private double storedHealth;
    private int storedFood;
    private float storedSaturation;
    private float storedExhaustion;
    private float storedFlySpeed;
    private float storedWalkSpeed;
    private boolean storedAllowFlight;
    private boolean storedFlying;
    private boolean stored = false;
    
    public StoredData(Player player) {
        this.player = player;
    }

    public StoredData storeData(boolean clear) {
        if (!stored) {
            if (player == null) return this;
            try {
                storedInventory = player.getInventory().getContents();
                storedArmor = player.getInventory().getArmorContents();
                storedLocation = player.getLocation();
                storedGameMode = player.getGameMode();
                storedExp = player.getExp();
                storedLevel = player.getLevel();
                storedFireTicks = player.getFireTicks();
                storedMaxHealth = player.getMaxHealth();
                storedHealth = player.getHealth();
                storedFood = player.getFoodLevel();
                storedSaturation = player.getSaturation();
                storedExhaustion = player.getExhaustion();
                storedFlySpeed = player.getFlySpeed();
                storedWalkSpeed = player.getWalkSpeed();
                storedAllowFlight = player.getAllowFlight();
                storedFlying = player.isFlying();
                storedEffects = player.getActivePotionEffects();
                if (clear) {
                    player.getInventory().clear();
                    player.setExp(0.0F);
                    player.setLevel(0);
                    player.setFireTicks(0);
                    player.setMaxHealth(20.0D);
                    player.setHealth(20.0D);
                    player.setFoodLevel(20);
                    player.setFlying(false);
                    player.setGameMode(GameMode.ADVENTURE);
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }
                }
            }catch (Exception ignored) {}
            
            stored = true;
        }
        return this;
    }
    
    public void restoreData() {
        if (stored) {
            if (player == null) return;
            try {
                player.getInventory().clear();
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.getInventory().setContents(storedInventory);
                player.getInventory().setArmorContents(storedArmor);
                player.teleport(storedLocation);
                player.setGameMode(storedGameMode);
                player.setExp(storedExp);
                player.setLevel(storedLevel);
                player.setFireTicks(storedFireTicks);
                player.setMaxHealth(storedMaxHealth);
                player.setHealth(storedHealth);
                player.setFoodLevel(storedFood);
                player.setSaturation(storedSaturation);
                player.setExhaustion(storedExhaustion);
                player.setFlySpeed(storedFlySpeed);
                player.setWalkSpeed(storedWalkSpeed);
                player.setAllowFlight(storedAllowFlight);
                player.setFlying(storedFlying);
                for (PotionEffect effect : storedEffects)
                    player.addPotionEffect(effect);
                storedInventory = null;
                storedArmor = null;
                storedLocation = null;
                storedGameMode = null;
                storedExp = 0.0F;
                storedLevel = 0;
                storedFireTicks = 0;
                storedMaxHealth = 0.0D;
                storedHealth = 0.0D;
                storedFood = 0;
                storedSaturation = 0.0F;
                storedExhaustion = 0.0F;
                storedFlySpeed = 0.0F;
                storedWalkSpeed = 0.0F;
                storedAllowFlight = false;
                storedFlying = false;
                stored = false;
                storedEffects = null;
            }catch (Exception ignored){}
        }
    }
    
    public boolean isStored() {
        return this.stored;
    }
}