package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import simple.brainsynder.nms.IActionMessage;
import simple.brainsynder.utils.Reflection;

import java.util.HashMap;
import java.util.LinkedList;

public class KOTH extends GameMaker {
    private HashMap<String, Integer> points;
    private HashMap<String, Integer> per10;
    private Location topLocation = null;
    private int per20 = 0;
    private IActionMessage message = null;

    public KOTH () {
        super();
        setGameSettings(new GameSettings(true));
        points = new HashMap<>();
        per10 = new HashMap();
        message = Reflection.getActionMessage();
    }

    @Override
    public void onEnd() {
        super.onEnd();
        points.clear();
        per10.clear();
    }

    @Override
    public void perTick() {
        super.perTick();
        if (topLocation == null) return;

        if (per20 == 15) {
            per20 = 0;
            for (IGamePlayer gamePlayer : players) {
                Player o = gamePlayer.getPlayer();
                int point = points.getOrDefault(o.getUniqueId().toString(), 0);
                int l = ((point * 100) / 100);
                
                StringBuilder text = new StringBuilder();
                text.append("Progress Bar: ");
                text.append("| ");
                int greenNum = per10.getOrDefault(o.getUniqueId().toString(), 0);
                if (l % 10 == 0) {
                    greenNum++;
                    per10.put(o.getUniqueId().toString(), greenNum);
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
                
                message.sendMessage(o, text.toString());

                if (o.getLocation().distance(topLocation) <= 3.0) {
                    if (point < 100) {
                        points.put(o.getUniqueId().toString(), (point + 1));
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
            }
        } else {
            per20++;
        }
    }

    @Override
    public void onStart() {
        World world = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ".top.world"));
        double x = settings.getData().getDouble("setup." + getName() + ".top.x");
        double y = settings.getData().getDouble("setup." + getName() + ".top.y");
        double z = settings.getData().getDouble("setup." + getName() + ".top.z");
        float yaw = (settings.getData().getInt("setup." + getName() + ".top.yaw"));
        float pitch = (settings.getData().getInt("setup." + getName() + ".top.pitch"));
        topLocation = new Location(world, x, y, z, yaw, pitch);
        for (IGamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 5 seconds of invincibility."));
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            super.onStart();
            players.forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are no longer invincible."));
            });
        }, 120L);

    }

    @Override
    public boolean allowsPVP() {
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

    @Override
    public void equipDefaultPlayer(Player player) {
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

    @Override
    public String getName() {
        return "KOTH";
    }

    @Override
    public String[] description() {
        return new String[]{
                "§eKOTH or known as §7King of the Hill",
                "§eis a game of capture, meaning you have to",
                "§estand at a certain spot to gain points",
                "§efirst person to 100 wins!"
        };
    }
}
