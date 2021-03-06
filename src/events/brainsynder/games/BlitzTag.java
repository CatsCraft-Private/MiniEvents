package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import events.brainsynder.utils.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ItemMaker;
import simple.brainsynder.api.ParticleMaker;
import simple.brainsynder.nms.IActionMessage;
import simple.brainsynder.nms.ITitleMessage;
import simple.brainsynder.sound.SoundMaker;
import simple.brainsynder.utils.Reflection;
import simple.brainsynder.wrappers.MaterialWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BlitzTag extends GameMaker {
    private IGamePlayer tagged = null;
    private int countDown = 15;
    private ParticleMaker maker;

    public BlitzTag(String mapID) {
        super(mapID);
        maker = new ParticleMaker(ParticleMaker.Particle.LAVA, 0.1, 2, 0, 0.8, 0);
    }

    public BlitzTag() {
        super();
        maker = new ParticleMaker(ParticleMaker.Particle.LAVA, 0.1, 2, 0, 0.8, 0);
    }

    @Override
    public String getName() {
        return "BlitzTag";
    }
    
    @Override public void onScoreboardLoad(IGamePlayer player) {
        if (player.getScoreHandler() == null) {
            ScoreboardHandler handler = new ScoreboardHandler(player.getPlayer().getUniqueId());
            handler.setTitle(0, "&3BlitzTag");
            handler.setLine(0, 14, "&3Tagger: ", "&bRandomizing...");
            handler.setLine(0, 13, "&3Blitz Timer: ", "&b?");
            handler.toggleScoreboard();
            player.setScoreHandler(handler);
        }
    }
    
    @Override public void onScoreboardUpdate(IGamePlayer player) {
        if (tagged == null) return;
        if (player.getScoreHandler() != null) {
            ScoreboardHandler handler = player.getScoreHandler();
            handler.setLine(0, 14, "&3Tagger: ", "&b" + tagged.getPlayer().getName());
            handler.setLine(0, 13, "&3Blitz Timer: ", getColor(ChatColor.AQUA) + countDown);
        }
    }
    
    @Override
    public void onLeave(IGamePlayer player) {
        if (tagged != null) {
            if (tagged.getPlayer().isOnline() && tagged.getPlayer().getName().equals(player.getPlayer().getName())) {
                player.getPlayer().sendMessage("§cYou can not leave this event while you are the Tagger.");
                return;
            }
        }
        super.onLeave(player);
    }

    @Override
    public void perTick() {
        super.perTick();
        if (tagged == null) return;
        if (maker == null) maker = new ParticleMaker(ParticleMaker.Particle.LAVA, 0.1, 2, 0, 0.8, 0);
        maker.sendToLocation(tagged.getPlayer().getLocation());
    }

    private void runTagged(IGamePlayer target, boolean lastRound) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if ((tagged == null) || (target == null)) {
                    cancel();
                    return;
                }

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
                        for (String pname : getPlayers ()) {
                            if (pname.equals(target.getPlayer().getName())) continue;
                            if (deadPlayers.contains(pname)) continue;
                            onWin(GameManager.getPlayer(pname));
                            plugin.getEventMain().end();
                            break;
                        }
                        return;
                    }
                    countDown = 15;
                    target.getPlayer().sendMessage(ChatColor.AQUA + "You §7has been disqualified.");
                    lost(target);
                    for (String name : getPlayers ()) {
                        if (deadPlayers.contains(name)) continue;
                        Player o = GameManager.getPlayer(name).getPlayer();
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
        return getColor(ChatColor.WHITE);
    }
    
    private String getColor(ChatColor color) {
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
        List<String> alive = new ArrayList<>();
        getPlayers ().stream().filter(player -> !deadPlayers.contains(GameManager.getPlayer(player).getPlayer().getName())).forEach(alive::add);
        if (alive.size() <= 1) {
            onWin(GameManager.getPlayer(alive.get(0)));
            onEnd();
            plugin.getEventMain().end();
            return;
        }
        int a = r.nextInt(alive.size());
        IGamePlayer target = GameManager.getPlayer(alive.get(a));
        this.tagged = target;
        ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.VILLAGER_HAPPY, 15, 0.5);
        maker.sendToLocation(target.getPlayer().getLocation());
        target.getPlayer().getInventory().setHelmet(new ItemMaker(MaterialWrapper.WOOL, (byte) 14).create());
        IActionMessage message = Reflection.getActionMessage();
        getPlayers ().stream().filter(player -> !deadPlayers.contains(GameManager.getPlayer(player).getPlayer().getName())).forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            message.sendMessage(player.getPlayer(), "§3§l" + tagged.getPlayer().getName() + " §8§lhas been Randomly Tagged, RUN!!!");
        });
        runTagged(target, (aliveCount() == 2));
    }

    @Override
    public void onStart() {
        gameSettings = new GameSettings(true);
        for (String name : getPlayers ()) {
            Player player = GameManager.getPlayer(name).getPlayer();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 2 seconds to spread out..."));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (getPlayers ().size() != 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.AQUA + "Selecting Tagger..."));
                }
            }, 120L);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                BlitzTag.super.onStart();
                randomTagged();
            }
        }.runTaskLater(plugin, 130);
    }

    @Override
    public void equipPlayer(Player player) {
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
                LinkedList<String> set = new LinkedList<>(settings.getData().getSection("setup." + getName() + ".inv").getKeys(false));
                while (set.peekFirst() != null) {
                    String slot = set.pollFirst();
                    player.getInventory().setItem(Integer.parseInt(slot), settings.getData().getItemStack("setup." + getName() + ".inv." + slot));
                }

                player.getInventory().setHelmet(settings.getData().getItemStack("setup." + getName() + ".armor.103"));
                player.getInventory().setChestplate(settings.getData().getItemStack("setup." + getName() + ".armor.102"));
                player.getInventory().setLeggings(settings.getData().getItemStack("setup." + getName() + ".armor.101"));
                player.getInventory().setBoots(settings.getData().getItemStack("setup." + getName() + ".armor.100"));
                if (settings.getData().getSection("setup." + getName() + ".potion") != null) {
                    for (String m : settings.getData().getSection("setup." + getName() + ".potion.name").getKeys(false)) {
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

    @Override
    public void equipDefaultPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5));
    }

    @Override
    public boolean allowsPVP() {
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
                    tagged.getPlayer().getInventory().setHelmet(new ItemMaker(MaterialWrapper.AIR).create());
                    tagged = player;
                    if (aliveCount() > 2) {
                        countDown = 15;
                    }
                    player.getPlayer().getInventory().setHelmet(new ItemMaker(MaterialWrapper.WOOL, (byte) 14).create());

                    IActionMessage message = Reflection.getActionMessage();
                    getPlayers ().stream()
                            .filter(p -> !deadPlayers.contains(p))
                            .forEach(name -> {
                                IGamePlayer p = GameManager.getPlayer(name);
                                message.sendMessage(p.getPlayer(), "§3§l" + tagged.getPlayer().getName() + " §8§lhas been Tagged, RUN!!!");
                            });
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

    @Override
    public String[] description() {
        return new String[]{
                "§eBlitzTag§7 is an event made",
                "§7to be fast paced and exhilarating",
                "§7Simply be the last person to not get tagged.",
                "§7If you do not tag a player within the time",
                "§7You will be Disqualified, and a new tagger will be selected"
        };
    }

    @Override
    public void onEnd() {
        super.onEnd();
        tagged = null;
    }
}

