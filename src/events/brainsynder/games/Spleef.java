package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;

public class Spleef extends GameMaker {
    private static BlockStorage storage = null;

    public Spleef () {
        super();
        storage = new BlockStorage();
    }
    
    @Override public void onWin(IGamePlayer gamePlayer) {
        super.onWin(gamePlayer);
        storage.reset();
        storage = null;
    }
    
    @Override public void perTick() {
        super.perTick();
        if (endTask) return;
        for (IGamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
            if (plugin.getEventMain().eventstarted && (player.getGameMode() != GameMode.SURVIVAL))
                player.setGameMode(GameMode.SURVIVAL);
            if (player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER)
                    || player.getLocation().getBlock().getType().equals(Material.WATER)
                    || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.STATIONARY_WATER)
                    || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.WATER)) {
                lost(gamePlayer);
                if (aliveCount() == 1) {
                    endTask = true;
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
            for (IGamePlayer gamePlayer : players) {
                Player player = gamePlayer.getPlayer();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.spleef-before")));
            }
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                super.onStart();
                players.forEach(gamePlayer -> {
                    Player player = gamePlayer.getPlayer();
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.spleef-invins-over")));
                });
            }, 120L);

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
                LinkedList<String> set = new LinkedList<>(settings.getData().getSection("setup." + getName() + ".inv.").getKeys(false));
                while (set.peekFirst() != null) {
                    String slot = set.pollFirst();
                    player.getInventory().setItem(Integer.parseInt(slot), settings.getData().getItemStack("setup." + getName() + ".inv." + slot));
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
        ItemStack dspade = new ItemStack(Material.DIAMOND_SPADE, 1);
        dspade.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
        dspade.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 3));
        inventory.setItem(0, dspade);
    }
    
    @Override public String getName() {
        return "Spleef";
    }
    
    @EventHandler
    public void onBreak (BlockBreakEvent e) {
        IGamePlayer player = GameManager.getPlayer(e.getPlayer());
        if (player.isPlaying()) {
            if (!(player.getGame() instanceof Spleef)) return;
            if (players.contains(player)) {
                Block b = e.getBlock();
                if (b == null) return;
                if (b.getType() == Material.AIR) return;
                if (b.getType() != Material.SNOW_BLOCK) {
                    e.setCancelled(true);
                    return;
                }
                if (!plugin.getEventMain().eventstarted) {
                    e.setCancelled(true);
                    return;
                }
                storage.addBlock(b);
                if (!hasStarted() && !plugin.getEventMain().eventstarted && !plugin.getEventMain().eventstarting) {
                    e.setCancelled(true);
                    return;
                }
                b.setType(Material.AIR);
            }
        }
    }
    
    @Override public String[] description() {
        return new String[]{
                "§7Spleef §eis a game where you use your shovel",
                "§eto make holes in the ground and",
                "§etrying to make people fall into them and",
                "§ebe out, but be careful they are doing the same to you."
        };
    }
}

