package events.brainsynder.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import simple.brainsynder.nbt.StorageTagCompound;
import simple.brainsynder.utils.Valid;

public class EntityLocation {
    private World world;
    private double x, y, z;
    private float yaw = 0, pitch = 0;

    public EntityLocation(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EntityLocation(World world, double x, double y, double z, float yaw, float pitch) {
        this(world, x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public EntityLocation(Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public static EntityLocation fromString(String str) {
        str = str.replace("EntityLocation:[world=", "");
        str = str.replace("x=", "");
        str = str.replace("y=", "");
        str = str.replace("z=", "");
        str = str.replace("yaw=", "");
        str = str.replace("pitch=", "");
        str = str.replace("]", "");
        String[] args = str.split(",");
        try {
            World world = Bukkit.getWorld(args[0]);
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);
            float yaw = Float.parseFloat(args[4]);
            float pitch = Float.parseFloat(args[5]);
            return new EntityLocation(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    public static EntityLocation fromCompound(StorageTagCompound compound) {
        World world = Bukkit.getWorld(compound.getString("world"));
        double x = compound.getDouble("x");
        double y = compound.getDouble("y");
        double z = compound.getDouble("z");
        float yaw = compound.getFloat("yaw");
        float pitch = compound.getFloat("pitch");
        return new EntityLocation(world, x, y, z, yaw, pitch);
    }

    public String toDataString() {
        return "BlockLocation:[world=" + world.getName() + ",x=" + x + ",y=" + y + ",z=" + z + ",yaw=" + yaw + ",pitch=" + pitch + ']';
    }

    public StorageTagCompound toCompound () {
        StorageTagCompound compound = new StorageTagCompound();
        compound.setString("world", world.getName());
        compound.setDouble("x", x);
        compound.setDouble("y", y);
        compound.setDouble("z", z);
        compound.setDouble("yaw", yaw);
        compound.setDouble("pitch", pitch);
        return compound;
    }

    public Location toLocation() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean atLocation(EntityLocation location) {
        Valid.notNull(location);
        return ((location.world.getName().equals(world.getName()))
                && (((int) location.x) == ((int) x))
                && (((int) location.y) == ((int) y))
                && (((int) location.z) == ((int) z))
        );
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
