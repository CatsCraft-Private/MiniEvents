package events.brainsynder.utils;

import org.bukkit.block.Block;

import java.util.LinkedList;

public class BlockStorage {
    private LinkedList<BlockSave> compoundList = new LinkedList<>();

    public void addBlock(Block block) {
        compoundList.addLast(new BlockSave(block));
    }

    public void reset() {
        if (compoundList.isEmpty()) return;
        for (BlockSave save : compoundList) {
            save.placeOriginal();
        }
    }
}
