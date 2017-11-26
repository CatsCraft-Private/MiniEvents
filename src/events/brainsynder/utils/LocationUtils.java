package events.brainsynder.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class LocationUtils {
    private static final float DEGTORAD = 0.017453293F;
    private static final float RADTODEG = 57.29577951F;

    public float getLookAtYaw(Entity loc, Entity lookat) {
        return getLookAtYaw(loc.getLocation(), lookat.getLocation());
    }
    public float getLookAtYaw(Block loc, Block lookat) {
        return getLookAtYaw(loc.getLocation(), lookat.getLocation());
    }
    public float getLookAtYaw(Location loc, Location lookat) {
        // Values of change in distance (make it relative)
        return getLookAtYaw(lookat.getX() - loc.getX(), lookat.getZ() - loc.getZ());
    }
    public float getLookAtYaw(Vector motion) {
        return getLookAtYaw(motion.getX(), motion.getZ());
    }

    public static Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();
        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dz = lookat.getZ() - loc.getZ();
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        // Set pitch
        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        return loc;
    }

    public float getLookAtYaw(double dx, double dz) {
        float yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 270;
            } else {
                yaw = 90;
            }
            yaw -= atan(dz / dx);
        } else if (dz < 0) {
            yaw = 180;
        }
        return -yaw - 90;
    }
    public float getLookAtPitch(double motX, double motY, double motZ) {
        return getLookAtPitch(motY, length(motX, motZ));
    }
    public float getLookAtPitch(double motY, double motXZ) {
        return -atan(motY / motXZ);
    }
    private float atan(double value) {
        return RADTODEG * (float) Math.atan(value);
    }

    public Location move(Location loc, Vector offset) {
        return move(loc, offset.getX(), offset.getY(), offset.getZ());
    }
    public Location move(Location loc, double dx, double dy, double dz) {
        Vector off = rotate(loc.getYaw(), loc.getPitch(), dx, dy, dz);
        double x = loc.getX() + off.getX();
        double y = loc.getY() + off.getY();
        double z = loc.getZ() + off.getZ();
        return new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
    }
    public Vector rotate(float yaw, float pitch, Vector value) {
        return rotate(yaw, pitch, value.getX(), value.getY(), value.getZ());
    }
    public Vector rotate(float yaw, float pitch, double x, double y, double z) {
        //Conversions found by (a lot of) testing
        float angle;
        angle = yaw * DEGTORAD;
        double sinyaw = Math.sin(angle);
        double cosyaw = Math.cos(angle);

        angle = pitch * DEGTORAD;
        double sinpitch = Math.sin(angle);
        double cospitch = Math.cos(angle);

        double newx = 0.0;
        double newy = 0.0;
        double newz = 0.0;
        newz -= x * cosyaw;
        newz -= y * sinyaw * sinpitch;
        newz -= z * sinyaw * cospitch;
        newx += x * sinyaw;
        newx -= y * cosyaw * sinpitch;
        newx -= z * cosyaw * cospitch;
        newy += y * cospitch;
        newy -= z * sinpitch;

        return new Vector(newx, newy, newz);
    }

    private double lengthSquared(double... values) {
        double rval = 0;
        for (double value : values) {
            rval += value * value;
        }
        return rval;
    }

    private double length(double... values) {
        return Math.sqrt(lengthSquared(values));
    }
}
