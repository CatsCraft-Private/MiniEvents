package events.brainsynder.games.team;

import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.TeamGameMaker;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.PetHandler;
import events.brainsynder.utils.PlayerUtils;
import events.brainsynder.utils.ScoreboardHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ItemMaker;

import java.util.LinkedList;

public class TDM extends TeamGameMaker {
    private int win = 15;
    public TDM(String mapID) {
        super(mapID);
    }

    public TDM (){}

    @Override
    public String getName() {
        return "TDM";
    }

    @Override
    public void onStart() {
        super.onStart();
        gameSettings = new GameSettings(true);
        win = (getPlayers ().size() * 2);

        getPlayers ().forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            player.getPlayer().sendMessage("§7Get §b" + win + " §7Kills to win the game!");
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayers ().forEach(name -> {
                    IGamePlayer player = GameManager.getPlayer(name);
                    PetHandler.removePet(player.getPlayer());
                });
            }
        }.runTaskLater(plugin, 60);
    }
    
    @Override public void onScoreboardLoad(IGamePlayer player) {
        if (player.getScoreHandler() == null) {
            ScoreboardHandler handler = new ScoreboardHandler(player.getPlayer().getUniqueId());
            handler.setTitle(0, "&3TeamDeathmatch");
            handler.setLine(0, 14, "&bBlue Score: ", "&70/" + win);
            handler.setLine(0, 13, "&cRed Score: ", "&70/" + win);
            handler.toggleScoreboard();
            player.setScoreHandler(handler);
        }
    }
    
    @Override public void onScoreboardUpdate(IGamePlayer player) {
        if (player.getScoreHandler() != null) {
            ScoreboardHandler handler = player.getScoreHandler();
            handler.setLine(0, 14, "&bBlue Score: ", "&7" + ((int)getBlueTeam().getScore()) + "/" + win);
            handler.setLine(0, 13, "&cRed Score: ", "&7" + ((int)getRedTeam().getScore()) + "/" + win);
        }
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
            if (settings.getData().getSection("setup." + getName() + ".inv") == null) {
                equipDefaultPlayer(player);
            } else {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                LinkedList<String> set = new LinkedList<>(settings.getData().getSection("setup." + getName() + ".inv").getKeys(false));
                while (set.peekFirst() != null) {
                    String slot = set.pollFirst();
                    player.getInventory().setItem(Integer.parseInt(slot), settings.getData().getItemStack("setup." + getName() + ".inv." + slot));
                }
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
        Inventory inventory = player.getInventory();
        ItemStack dsword = new ItemStack(Material.IRON_SWORD, 1);
        dsword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().clear();
        inventory.setItem(0, dsword);
        inventory.setItem(1, new ItemMaker(Material.BOW).enchant(Enchantment.ARROW_INFINITE, 1).create());
        inventory.setItem(8, new ItemMaker(Material.ARROW).create());
    }

    @Override
    public boolean allowsPVP() {
        return true;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && (!(event.getDamager() instanceof Projectile))) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        IGamePlayer player = GameManager.getPlayer(p);
        if (player.isPlaying()) {
            if (player.getGame() instanceof TDM) {
                if (!plugin.getEventMain().eventstarted) {
                    event.setCancelled(true);
                    return;
                }
                Player enemy = null;
                if (event.getDamager() instanceof Player) {
                    enemy = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) event.getDamager();
                    if (!(projectile.getShooter() instanceof Player)) return;
                    enemy = (Player) projectile.getShooter();
                }

                if (enemy != null) {
                    IGamePlayer<ITeamGame> hitter = GameManager.getPlayer(enemy);
                    if (hitter.getTeam().getName().equals(player.getTeam().getName())) {
                        event.setCancelled(true);
                        return;
                    } else {
                        if (event.getDamager() instanceof Projectile) PlayerUtils.sendBlood(p);
                        if ((p.getHealth() - event.getDamage()) <= 1) {
                            double score = (hitter.getTeam().getScore() + 1);
                            hitter.getTeam().setScore(score);
                            if (win <= score) {
                                onWin(hitter.getTeam());
                                return;
                            }
                            event.setCancelled(true);
                            p.setHealth(p.getMaxHealth());
                            p.teleport(getSpawn(player.getTeam()));
                            getPlayers ().forEach(name -> {
                                IGamePlayer gamePlayer = GameManager.getPlayer(name);
                                gamePlayer.getPlayer().sendMessage(player.getTeam().getChatColor() + p.getName() + " §7was killed by " + hitter.getTeam().getChatColor() + hitter.getPlayer().getName());
                            });
                            return;
                        }
                    }
                }


                if ((p.getHealth() - event.getDamage()) <= 1) {
                    event.setCancelled(true);
                    p.setHealth(p.getMaxHealth());
                }
            }
        }
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        IGamePlayer player = GameManager.getPlayer((Player) e.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof TDM) {
                if (plugin.getEventMain().eventstarted)
                    e.setCancelled(true);
            }
        }
    }

    @Override
    public String[] description() {
        return new String[]{
                "§6What is TDM?",
                "§eTDM Stands for §7Team Death Match",
                "§eYou must kill the other teams players",
                "§eFirst team to reach the score wins!"
        };
    }
}
