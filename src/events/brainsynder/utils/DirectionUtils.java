package events.brainsynder.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class DirectionUtils {
    public enum Direction {
        NORTH, EAST, WEST, SOUTH
    }


    private static String direction(float yaw) {
        yaw /= 90.0F;
        int i = Math.round(yaw);
        switch (i) {
            case -4:
            case 0:
            case 4:
                return "SOUTH";
            case -3:
            case 1:
                return "WEST";
            case -2:
            case 2:
                return "NORTH";
            case -1:
            case 3:
                return "EAST";
            default:
                return "";
        }
    }

    public static BlockFace getDirectionAsFace (Player player) {
        return getDirectionAsFace(player.getLocation());
    }

    public static BlockFace getDirectionAsFace (Location location) {
        switch (getCardinalDirection(location)) {
            case NORTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.EAST;
            case SOUTH:
                return BlockFace.SOUTH;
            default:
                return BlockFace.WEST;
        }
    }

    public static String getPlayerDirection(Player playerSelf) {
        return direction(playerSelf.getLocation().getYaw());
    }

    public static Direction getCardinalDirection(Player player) {
        return getCardinalDirection(player.getLocation());
    }

    private static Direction getCardinalDirection(Location location) {
        double rotation = (location.getYaw() - 90.0F) % 360.0F;
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if ((0.0D <= rotation) && (rotation < 67.5D)) {
            return Direction.NORTH;
        } else if ((67.5D <= rotation) && (rotation < 112.5D)) {
            return Direction.EAST;
        } else if ((157.5D <= rotation) && (rotation < 247.5D)) {
            return Direction.SOUTH;
        } else {
            return Direction.WEST;
        }
    }
}