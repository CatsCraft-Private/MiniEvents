package events.brainsynder.games.team;

import de.robingrether.idisguise.disguise.DisguiseType;
import de.robingrether.idisguise.disguise.MobDisguise;
import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;
import events.brainsynder.key.teams.TeamGameMaker;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import simple.brainsynder.api.ItemMaker;
import simple.brainsynder.api.ParticleMaker;
import simple.brainsynder.math.MathUtils;
import simple.brainsynder.sound.SoundMaker;

import java.util.*;


/**
 * Ideas:
 * - Use a Map that contains the Team,BlockLocation and check that Map to check how many blocks the Team painted
 * - if TEAM painted over OTHER TEAM it will subtract that from the other teams blocks.
 * - Paint Bombs (Radius: Random [3-5])
 * - 3 min game time (180 seconds)
 */
public class Splatoon extends TeamGameMaker {
    /**
     * String = Team name
     * Map.String = BlockLocation.toDataString()
     * Map.BlockSave = Original block Data.
     */
    private int time = 180, per15 = 0;
    private boolean announced = false;
    private List<Integer> keyTimes = Arrays.asList(1, 2, 3, 4, 5, 10, 20, 30, 60, 120);
    private Map<String, Map<String, BlockSave>> teamBlocks = null;
    private List<String> squids = new ArrayList<>();

    public Splatoon() {
        super();
        teamBlocks = new HashMap<>();
        setGameSettings(new GameSettings(true));
    }

    @Override
    public void onLeave(IGamePlayer player) {
        if (DisguiseHandler.getApi().isDisguised(player.getPlayer()) && squids.contains(player.getPlayer().getUniqueId().toString())) {
            DisguiseHandler.getApi().undisguise(player.getPlayer());
            squids.remove(player.getPlayer().getUniqueId().toString());
        }
        super.onLeave(player);
    }

    @Override
    public void onEnd() {
        players.forEach(player -> {
            if (DisguiseHandler.getApi().isDisguised(player.getPlayer()) && squids.contains(player.getPlayer().getUniqueId().toString())) {
                DisguiseHandler.getApi().undisguise(player.getPlayer());
                squids.remove(player.getPlayer().getUniqueId().toString());
            }
        });

        super.onEnd();
        for (String team : teamBlocks.keySet()) {
            Map<String, BlockSave> map = teamBlocks.getOrDefault(team, new HashMap<>());
            map.values().forEach(BlockSave::placeOriginal);
        }
        teamBlocks.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        players.forEach(player -> player.getPlayer().sendMessage("§cEquipping Paint Cannon..."));
        new BukkitRunnable() {
            @Override
            public void run() {
                players.forEach(player -> {
                    player.getPlayer().sendMessage("§cThe arena is your canvas... COVER IT!!!");
                    equipDefaultPlayer(player.getPlayer());
                    PetHandler.removePet(player.getPlayer());
                });
            }
        }.runTaskLater(plugin, 60);
    }

    @Override
    public String getName() {
        return "Splatoon";
    }

    @Override
    public void equipPlayer(Player player) {}

    @Override
    public void perTick() {
        super.perTick();
        if (per15 == 15) {
            time--;
            per15 = 0;
            announced = false;
        }
        per15++;
        Map<String, BlockSave> saved = teamBlocks.getOrDefault(getRedTeam().getName(), new HashMap<>());
        Map<String, BlockSave> enemySaved = teamBlocks.getOrDefault(getBlueTeam().getName(), new HashMap<>());
        players.forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            BlockLocation location = new BlockLocation(player.getLocation().subtract(0, 1, 0));
            if (squids.contains(player.getUniqueId().toString())) {
                if (saved.containsKey(location.toDataString())) {
                    if (gamePlayer.getTeam().getName().equals("Red")) {
                        if (player.hasPotionEffect(PotionEffectType.SLOW))
                            player.removePotionEffect(PotionEffectType.SLOW);

                        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        }
                    } else {
                        if (player.hasPotionEffect(PotionEffectType.SPEED))
                            player.removePotionEffect(PotionEffectType.SPEED);

                        if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                        }
                    }
                    return;
                }
                if (enemySaved.containsKey(location.toDataString())) {
                    if (gamePlayer.getTeam().getName().equals("Blue")) {
                        if (player.hasPotionEffect(PotionEffectType.SLOW))
                            player.removePotionEffect(PotionEffectType.SLOW);

                        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        }
                    } else {
                        if (player.hasPotionEffect(PotionEffectType.SPEED))
                            player.removePotionEffect(PotionEffectType.SPEED);

                        if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                        }
                    }
                    return;
                }
            }
            if (player.hasPotionEffect(PotionEffectType.SPEED))
                player.removePotionEffect(PotionEffectType.SPEED);
            if (player.hasPotionEffect(PotionEffectType.SLOW))
                player.removePotionEffect(PotionEffectType.SLOW);

        });


        int redSize = saved.size();
        int blueSize = enemySaved.size();
        getRedTeam().setScore(redSize);
        getBlueTeam().setScore(blueSize);
        if (time <= 0) {
            if (redSize > blueSize) {
                onWin(getRedTeam());
            } else {
                onWin(getBlueTeam());
            }
            return;
        }
        if (keyTimes.contains(time) && (!announced)) {
            players.forEach(player -> player.getPlayer().sendMessage("§7Time Left: §b" +
                    ((time == 120) ? "2 Minutes" : ((time == 60) ? "1 Minute" : time + " Second(s)"))
            ));
            announced = true;
        }

        if (message == null) return;
        if (!plugin.getEventMain().eventstarted) return;
        if (!hasStarted()) return;

        players.forEach(player -> message.sendMessage(player.getPlayer(), "§4§lRed Score: §c§l" + (redSize) + " §8§l/ §9§lBlue Score: §b§l" + (blueSize)));
    }

    private ItemStack getRedGun() {
        ItemMaker maker = new ItemMaker(Material.GOLD_BARDING);
        maker.setName("§c§lRed Paint Cannon");
        return maker.create();
    }

    private ItemStack getBlueGun() {
        ItemMaker maker = new ItemMaker(Material.DIAMOND_BARDING);
        maker.setName("§9§lBlue Paint Cannon");
        return maker.create();
    }

    private boolean allowChange(Block block) {
        if (block == null) return true;
        if (block.getType() == Material.AIR) return true;
        return false;
    }

    private void colorBlocks(Location location, IGamePlayer gamePlayer) {
        int i = MathUtils.random(1, 2);
        SoundMaker.BLOCK_SLIME_PLACE.playSound(location, 1.5f, 1.5f);
        ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.BLOCK_CRACK, ((i == 1) ? 75 : 35), 1.0);
        maker.setData((gamePlayer.getTeam().getName().equals("Red")) ? Material.REDSTONE_BLOCK : Material.LAPIS_BLOCK);
        Team enemy = getOppositeTeam(gamePlayer.getTeam());
        Map<String, BlockSave> saveMap = teamBlocks.getOrDefault(gamePlayer.getTeam().getName(), new HashMap<>());
        Map<String, BlockSave> enemySaveMap = teamBlocks.getOrDefault(enemy.getName(), new HashMap<>());
        DyeColorWrapper color = gamePlayer.getTeam().getColor();

        BlockUtils.getBlocksInRadius(location, i, false).forEach(block -> {
            if (block == null) return;
            if (block.getType() == Material.AIR) return;
            if (block.getType() == Material.BARRIER) return;
            if (block.getType() == Material.WOOL) return;
            if (block.getType() == Material.LADDER) return;
            if (block.getType() == Material.CARPET) return;
            if (block.getType() == Material.VINE) return;
            if (block.getType().name().contains("PISTON")) return;
            if (block.getType().name().contains("DOOR")) return;
            if (block.getType().name().contains("BED")) return;
            if (block.getType().name().contains("SIGN")) return;
            if (block.getType().name().contains("POWDER")) return;
            if (block.getType().name().contains("GLAZED")) return;
            if (block.getType().name().contains("LAPIS")) return;
            if (block.getType().name().contains("REDSTONE")) return;
            if ((!allowChange(block.getRelative(BlockFace.DOWN)))
                    && (!allowChange(block.getRelative(BlockFace.UP)))
                    && (!allowChange(block.getRelative(BlockFace.EAST)))
                    && (!allowChange(block.getRelative(BlockFace.SOUTH)))
                    && (!allowChange(block.getRelative(BlockFace.WEST)))
                    && (!allowChange(block.getRelative(BlockFace.NORTH)))) return;

            BlockLocation blockLocation = new BlockLocation(block.getLocation());
            if (saveMap.containsKey(blockLocation.toDataString())) return;

            BlockSave save = new BlockSave(block);
            if (enemySaveMap.containsKey(blockLocation.toDataString())) {
                save = enemySaveMap.remove(blockLocation.toDataString());
            }

            saveMap.put(blockLocation.toDataString(), save);
            block.setType(Material.CONCRETE);
            block.setData(color.getWoolData());
            maker.sendToLocation(block.getLocation());
            teamBlocks.put(enemy.getName(), enemySaveMap);
            teamBlocks.put(gamePlayer.getTeam().getName(), saveMap);
        });
    }

    @Override
    public void equipDefaultPlayer(Player player) {
        player.setHealth(20.0);
        Inventory inventory = player.getInventory();
        IGamePlayer gamePlayer = GameManager.getPlayer(player);
        if (gamePlayer.getTeam() != null) {
            if (gamePlayer.getTeam().getName().equals("Red")) {
                inventory.setItem(0, getRedGun());
            } else {
                inventory.setItem(0, getBlueGun());
            }
        }
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
            if (player.getGame() instanceof Splatoon) {
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
                    } else {
                        if ((p.getHealth() - event.getDamage()) <= 1) {
                            event.setCancelled(true);
                            p.setHealth(p.getMaxHealth());
                            players.forEach(gamePlayer -> gamePlayer.getPlayer().sendMessage(player.getTeam().getChatColor() + p.getName() + " §7was painted by " + hitter.getTeam().getChatColor() + hitter.getPlayer().getName()));
                            p.teleport(getSpawn(player.getTeam()));
                        }
                    }
                }
            }
        }
    }

    private void shootBullet(Player player) {
        Vector direction = RandomRef.calculatePath(player);
        Projectile proj = player.launchProjectile(Snowball.class);
        proj.setMetadata("SPLATOON", new FixedMetadataValue(plugin, "SPLATOON"));
        proj.setShooter(player);
        proj.setVelocity(direction.multiply(4));
        proj.setCustomName("Splatoon - " + player.getPlayer().getUniqueId());
    }

    @EventHandler
    public void fire(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() == Material.AIR) return;

        IGamePlayer player = GameManager.getPlayer(e.getPlayer());
        if (player.isPlaying()) {
            if (player.getGame() instanceof Splatoon) {
                if ((e.getItem().isSimilar(getRedGun()))
                        || (e.getItem().isSimilar(getBlueGun()))) {
                    shootBullet(player.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onSwitch(PlayerItemHeldEvent e) {
        IGamePlayer player = GameManager.getPlayer(e.getPlayer());
        if (player.isPlaying()) {
            if (player.getGame() instanceof Splatoon) {
                if (e.getPreviousSlot() == 0) {
                    MobDisguise disguise = new MobDisguise(DisguiseType.SQUID);
                    disguise.setCustomNameVisible(true);
                    disguise.setCustomName(player.getTeam().getChatColor() + e.getPlayer().getName());
                    DisguiseHandler.getApi().disguise(player.getPlayer(), disguise);
                    squids.add(player.getPlayer().getUniqueId().toString());
                    return;
                }
                if (e.getNewSlot() == 0) {
                    if (squids.contains(player.getPlayer().getUniqueId().toString())) {
                        if (player.getPlayer().hasPotionEffect(PotionEffectType.SPEED))
                            player.getPlayer().removePotionEffect(PotionEffectType.SPEED);
                        if (player.getPlayer().hasPotionEffect(PotionEffectType.SLOW))
                            player.getPlayer().removePotionEffect(PotionEffectType.SLOW);
                        DisguiseHandler.getApi().undisguise(player.getPlayer());
                        squids.remove(player.getPlayer().getUniqueId().toString());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity().hasMetadata("SPLATOON")) {
            IGamePlayer player = GameManager.getPlayer((Player) e.getEntity().getShooter());
            if (e.getHitBlock() == null) {
                if (e.getHitEntity() != null) {
                    if (e.getHitEntity() instanceof Player) {
                        Player p = (Player) e.getHitEntity();
                        p.damage(6, player.getPlayer());
                        return;
                    }
                }
            }
            colorBlocks(e.getHitBlock().getLocation(), player);
        }
    }

    @EventHandler
    public void disableRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        IGamePlayer player = GameManager.getPlayer((Player) e.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof Splatoon) {
                if (plugin.getEventMain().eventstarted)
                    e.setCancelled(true);
            }
        }
    }

    @Override
    public String[] description() {
        return new String[]{
                "§6Splatoon Objective:",
                "§e- Paint the most blocks",
                "§e- You are also able to override the other teams color",
                "§eto make them loose points and your team gains the points",
                "§e- To Morph into a squid Simply switch the active slot (Scroll)",
                "§e- To unMorph Simply scroll back to the Paint Cannon (Slot 1)"
        };
    }
}
