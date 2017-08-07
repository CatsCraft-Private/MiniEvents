package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ParticleMaker;

import simple.brainsynder.nms.ITitleMessage;
import simple.brainsynder.sound.SoundMaker;
import simple.brainsynder.utils.Reflection;

import java.util.*;

public class BlitzTag extends GameMaker {
    private IGamePlayer tagged = null;
    private int countDown = 15;
    
    @Override public String getName() {
        return "BlitzTag";
    }
    
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
    
    private void runTagged(IGamePlayer target, boolean lastRound) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!target.getPlayer().getName().equals(tagged.getPlayer().getName())) {
                    ITitleMessage message = Reflection.getTitleMessage();
                    message.sendMessage(target.getPlayer(), " ", " ");
                    cancel();
                    return;
                }
                if (countDown <= 0) {
                    cancel();
                    if (lastRound) {
                        lost(target);
                        for (IGamePlayer o : players) {
                            if (o.getPlayer().getUniqueId().equals(target.getPlayer().getUniqueId())) continue;
                            if (deadPlayers.contains(o)) continue;
                            onWin(o);
                            target.getGame().onEnd();
                            plugin.getEventMain().end();
                            break;
                        }
                        return;
                    }
                    countDown = 15;
                    lost(target);
                    for (IGamePlayer gamePlayer : players) {
                        if (deadPlayers.contains(gamePlayer)) continue;
                        Player o = gamePlayer.getPlayer();
                        if (!o.getName().equals(target.getPlayer().getName())) {
                            o.sendMessage(ChatColor.AQUA + target.getPlayer().getName() + " §7has been disqualified.");
                        }
                    }
                    ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.CLOUD, 0.5, 15, 0.5);
                    maker.sendToLocation(target.getPlayer().getLocation());
                    
                    SoundMaker.ENTITY_GENERIC_EXPLODE.playSound(target.getPlayer().getLocation(), 1.0F, 2.0F);
                    randomTagged();
                    return;
                }
                ITitleMessage message = Reflection.getTitleMessage();
                message.sendMessage(target.getPlayer(), "§3§lTag another player...", "§3§lTime till disqualification: " + getColor() + countDown);
                countDown--;
            }
        }.runTaskTimer(GamePlugin.instance, 0, 16);
    }
    
    private String getColor() {
        ChatColor color = ChatColor.WHITE;
        switch (countDown) {
            case 1:
                SoundMaker.BLOCK_NOTE_SNARE.playSound(tagged.getPlayer().getLocation(), 1.0F, 1.0F);
                color = ChatColor.DARK_RED;
                break;
            case 2:
                SoundMaker.BLOCK_NOTE_SNARE.playSound(tagged.getPlayer().getLocation(), 0.654F, 0.654F);
                color = ChatColor.RED;
                break;
            case 3:
                SoundMaker.BLOCK_NOTE_SNARE.playSound(tagged.getPlayer().getLocation(), 0.325F, 0.325F);
                color = ChatColor.GOLD;
                break;
            case 4:
                color = ChatColor.YELLOW;
                break;
        }
        return color + String.valueOf(ChatColor.BOLD);
    }
    
    private void randomTagged() {
        Random r = new Random();
        List<IGamePlayer> alive = new ArrayList<>();
        players.stream()
                .filter(player -> !deadPlayers.contains(player))
                .forEach(alive::add);
        if (alive.size() <= 1) {
            onWin(alive.get(0));
            onEnd();
            plugin.getEventMain().end();
            // Return?
        }
            int a = r.nextInt(alive.size());
            IGamePlayer target = alive.get(a);
            this.tagged = target;
            ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.VILLAGER_HAPPY, 15, 0.5);
            maker.sendToLocation(target.getPlayer().getLocation());
            
            players.stream()
                    .filter(player -> !deadPlayers.contains(player))
                    .forEach(player -> player.getPlayer().sendMessage("§b" + tagged.getPlayer().getName() + " §7has been Randomly Tagged, RUN!!!"));
            runTagged(target, false);
    }
    
    @Override public void onStart() {
        for (IGamePlayer gamePlayer : players) {
            gamePlayer.getPlayerData().storeData(true);
            Player player = gamePlayer.getPlayer();
            equipPlayer(player);
            player.teleport(getSpawn());
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 2 seconds to spread out..."));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (players.size() != 0) {
                    super.onStart();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.AQUA + "Selecting Tagger..."));
                }
                
            }, 120L);
        }
        new BukkitRunnable() {
            @Override public void run() {
                randomTagged();
            }
        }.runTaskLater(plugin, 130);
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
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5));
    }
    
    @Override public boolean allowsPVP() {
        return true;
    }
    
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        IGamePlayer player = GameManager.getPlayer((Player) event.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof BlitzTag) {
                if (!plugin.getEventMain().eventstarted) {
                    event.setCancelled(true);
                    return;
                }
                if (tagged == null) {
                    countDown = 15;
                    randomTagged();
                    return;
                }
                
                if (tagged.getPlayer().getName().equals(event.getDamager().getName())) {
                    tagged = player;
                    if (aliveCount() > 0) {
                        countDown = 15;
                    }
                    players.stream()
                            .filter(p -> !deadPlayers.contains(p))
                            .forEach(p -> p.getPlayer().sendMessage("§b" + tagged.getPlayer().getName() + " §7has been Tagged, RUN!!!"));
                    ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.VILLAGER_HAPPY, 15, 0.5);
                    maker.sendToLocation(player.getPlayer().getLocation());
                    SoundMaker.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(event.getDamager().getLocation(), 1.0F, 1.0F);
                    runTagged(player, (aliveCount() == 2));
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @Override public String[] description() {
        return new String[]{
                "§7BlitzTag§e is an event made",
                "§eto be fast paced and exhilarating",
                "§eSimply be the last person to not get tagged.",
                "§eIf you do not tag a player within the time",
                "§eYou will be Disqualified, and a new tagger will be selected"
        };
    }

    @Override public void onEnd() {
        super.onEnd();
        tagged = null;
    }
}

