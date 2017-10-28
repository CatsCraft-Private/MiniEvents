package events.brainsynder.utils;

import events.brainsynder.managers.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomRef {
    private static Random r = new Random();
    public static List<Location> getStraightLine(LivingEntity player, int length) {
        List<Location> list = new ArrayList<>();
        for (int amount = length; amount > 0; amount--)
            list.add(player.getTargetBlock(null, amount).getLocation());
        return list;
    }

    public static Location getEyeLocation(Entity player) {
        return player.getLocation().add(0.0D, 1.1D, 0.0D);
    }

    public static Location getTargetLocation(Entity player) {
        return player.getLocation().clone().add(player.getLocation().getDirection().multiply(100.0));
    }

    public static String formatHHMMSS(long secondsCount) {
        int seconds = (int) (secondsCount % 60);
        secondsCount -= seconds;
        long minutesCount = secondsCount / 60;
        long minutes = minutesCount % 60;
        StringBuilder builder = new StringBuilder();
        if (minutes > 0)
            builder.append(minutes).append(":");
        builder.append((seconds < 10) ? "0" + seconds : seconds);
        return builder.toString();
    }

    public static Vector calculatePath(Player player) {
        return calculatePath(player, false);
    }

    public static Vector calculatePath(Player player, boolean spreadProj) {
        double yaw = Math.toRadians((double) (-player.getLocation().getYaw() - 90.0F));
        double pitch = Math.toRadians((double) (-player.getLocation().getPitch()));
        double[] spread = new double[]{1.0D, 1.0D, 1.0D};

        for(int x = 0; x < 3; ++x) {
            spread[x] = (r.nextDouble() - r.nextDouble()) * 0.1D;
        }

        double x = Math.cos(pitch) * Math.cos(yaw) + (spreadProj ? spread[0] : 0.0D);
        double y = Math.sin(pitch) + (spreadProj ? spread[1] : 0.0D);
        double z = -Math.sin(yaw) * Math.cos(pitch) + (spreadProj ? spread[2] : 0.0D);
        return new Vector(x, y, z);
    }

    public static void noArc(final Projectile proj, final org.bukkit.util.Vector direction) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(GamePlugin.instance, () -> {
            if (!proj.isDead()) {
                proj.setVelocity(direction);
                RandomRef.noArc(proj, direction);
            }
        }, 1L);
    }

    public static Entity[] getNearbyEntities(Location l, int radius) {
        int chunk = radius < 16 ? 1 : (radius - radius % 16) / 16;
        ArrayList<Entity> radiusEntities = new ArrayList<>();

        for (int ent = 0 - chunk; ent <= chunk; ++ent) {
            for (int chZ = 0 - chunk; chZ <= chunk; ++chZ) {
                int x = (int) l.getX();
                int y = (int) l.getY();
                int z = (int) l.getZ();
                Location loc = new Location(l.getWorld(), (double) (x + ent * 16), (double) y, (double) (z + chZ * 16));

                for (Entity e : loc.getChunk().getEntities()) {
                    if (Objects.equals(e.getLocation().getWorld().getName(), l.getWorld().getName()) && e.getLocation().distance(l) <= (double) radius && e.getLocation().getBlock() != l.getBlock()) {
                        radiusEntities.add(e);
                    }
                }
            }
        }

        return radiusEntities.toArray(new Entity[radiusEntities.size()]);
    }
}