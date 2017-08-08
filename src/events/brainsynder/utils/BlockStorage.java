package events.brainsynder.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import simple.brainsynder.nbt.StorageTagCompound;
import simple.brainsynder.wrappers.MaterialWrapper;

import java.util.ArrayList;
import java.util.List;

public class BlockStorage {
    private List<StorageTagCompound> compoundList = new ArrayList<>();

    public void addBlock (Block block) {
        StorageTagCompound compound = new StorageTagCompound();
        compound.setString("type", block.getType().name());
        compound.setByte("data", block.getState().getRawData());
        BlockLocation location = new BlockLocation(block.getLocation());
        compound.setString("location", location.toDataString());
        compoundList.add(compound);
    }

    public void reset () {
        for (StorageTagCompound compound : compoundList) {
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
}
