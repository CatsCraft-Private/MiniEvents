package events.brainsynder.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import simple.brainsynder.nbt.StorageTagCompound;
import simple.brainsynder.wrappers.MaterialWrapper;

public class BlockSave {
    private StorageTagCompound compound = null;

    public BlockSave(Block block) {
        compound = new StorageTagCompound();
        compound.setString("type", block.getType().name());
        compound.setByte("data", block.getState().getRawData());
        BlockLocation location = new BlockLocation(block.getLocation());
        compound.setString("location", location.toDataString());
    }

    public void placeOriginal() {
        MaterialWrapper wrapper = MaterialWrapper.fromName(compound.getString("type"));
        byte data = compound.getByte("data");
        BlockLocation location = BlockLocation.fromString(compound.getString("location"));

        if (location != null) {
            Location loc = location.toLocation();
            loc.getBlock().setType(wrapper.toMaterial());
            loc.getBlock().setData(data);
        }
    }
}
