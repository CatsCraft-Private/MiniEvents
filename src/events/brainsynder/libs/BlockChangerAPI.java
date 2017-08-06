package events.brainsynder.libs;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockChangerAPI {
    @Getter @Setter private Material material;
    @Getter @Setter private byte data;
    @Getter private Material oldMaterial;
    @Getter private byte oldData;
    private Block block;

    public BlockChangerAPI(Block block) {
        this.block = block;
        this.material = block.getType ();
        this.data = (byte) block.getState().getData().toItemStack().getDurability();
        this.oldMaterial = block.getType ();
        this.oldData = (byte) block.getState().getData().toItemStack().getDurability();
    }

    public void placeNewBlock () {
        this.block.setType(material);
        this.block.getState().getData().toItemStack().setDurability(data);
    }

    public void placeOldBlock () {
        this.block.getState().getData().toItemStack().setDurability(oldData);
        this.block.setType(oldMaterial);

    }
}
