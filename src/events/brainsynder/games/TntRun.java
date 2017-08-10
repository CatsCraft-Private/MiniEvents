package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import events.brainsynder.utils.BlockStorage;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ParticleMaker;

public class TntRun extends GameMaker {
    private static BlockStorage storage = null;
    
    @Override public void onWin(IGamePlayer gamePlayer) {
        super.onWin(gamePlayer);
        storage.reset();

        Player o = gamePlayer.getPlayer();
        if (plugin.getConfig().getBoolean("events.money.enabled")) {
            double i = plugin.getConfig().getDouble("events.money.amount");
            EconomyResponse r = GamePlugin.econ.depositPlayer(o, i);
            if (r.transactionSuccess()) {
                o.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.got-money").replace("{0}", Double.toString(i))));
            }
        }
        storage = null;
    }
    
    @Override public void onStart() {
        for (IGamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
            equipPlayer(player);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.tnt-before")));
        }
        storage = new BlockStorage();
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (players.size() != 0) {
                super.onStart();
                for (IGamePlayer gamePlayer : players) {
                    Player player = gamePlayer.getPlayer();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.tnt-invins-over")));
                    Location location = player.getLocation();
                    
                    new BukkitRunnable() {
                        @Override public void run() {
                            if (!gamePlayer.isPlaying()) {
                                cancel();
                                return;
                            }
                            if (gamePlayer.getGame() == null) {
                                cancel();
                                return;
                            }
                            if (!(gamePlayer.getGame() instanceof TntRun)) {
                                cancel();
                                return;
                            }
                            
                            
                            final Block b = location.getBlock().getRelative(BlockFace.DOWN);
                            if (b == null) return;
                            if (b.getType() != Material.TNT) return;
                            if (!plugin.getEventMain().eventstarted) return;
                            storage.addBlock(b);
                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                if (!hasStarted() && !plugin.getEventMain().eventstarted && !plugin.getEventMain().eventstarting) {
                                    return;
                                }
                                ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.BLOCK_DUST, 30, 0.5);
                                maker.setData(b.getType(), b.getState().getData().toItemStack().getDurability());
                                maker.sendToLocation(b.getLocation());
                                b.setType(Material.AIR);
                            }, 5L);
                        }
                    }.runTaskTimer(plugin, 0, 10);
                }
            }
            
        }, 120L);
    }
    
    @Override public String getName() {
        return "TntRun";
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
    
    @Override public void perTick() {
        super.perTick();
        if (endTask) return;
        for (IGamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
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
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        IGamePlayer gamePlayer = GameManager.getPlayer(event.getPlayer());
        Player player = gamePlayer.getPlayer();
        if (gamePlayer.isPlaying()) {
            if (gamePlayer.getGame() instanceof TntRun) {
                if (endTask) return;
                if (!plugin.getEventMain().eventstarted) return;
                if ((event.getFrom().getBlockX() != event.getTo().getBlockX()
                        || event.getFrom().getBlockY() != event.getTo().getBlockY()
                        || event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
                    final Block b = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    if (b == null) return;
                    if (b.getType() != Material.TNT) return;
                    storage.addBlock(b);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (!hasStarted() && !plugin.getEventMain().eventstarted && !plugin.getEventMain().eventstarting) {
                            return;
                        }
                        ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.BLOCK_DUST, 50, 1.0);
                        maker.setData(b.getType(), b.getState().getData().toItemStack().getDurability());
                        maker.sendToLocation(b.getLocation());
                        b.setType(Material.AIR);
                    }, 5L);
                }
            }
        }
    }
    
    @Override public String[] description() {
        return new String[]{
                "§6What is TntRun?",
                "§eTntRun is where you run around",
                "§eon Tnt and try not to land in the",
                "§ewater, be careful the TNT Disappears..."
        };
    }
}
