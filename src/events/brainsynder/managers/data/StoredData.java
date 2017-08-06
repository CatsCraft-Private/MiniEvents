package events.brainsynder.managers.data;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public class StoredData {
    public final UUID uuid;
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
    @Getter
    private boolean stored = false;
    
    public StoredData(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        } else {
            this.uuid = uuid;
        }
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }
    
    public StoredData storeData(boolean clear) {
        if (!stored) {
            Player player = getPlayer();
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
            
            stored = true;
        }
        return this;
    }
    
    public StoredData storeData() {
        storeData(false);
        return this;
    }
    
    public StoredData restoreData() {
        Player player = getPlayer();
        if (stored) {
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
        }
        return this;
    }
}