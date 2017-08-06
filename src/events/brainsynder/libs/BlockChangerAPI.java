package events.brainsynder.libs;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockChangerAPI {
    private Material material;
    private byte data;
    private Material oldMaterial;
    private byte oldData;
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
    
    public Material getMaterial() {
        return this.material;
    }
    
    public byte getData() {
        return this.data;
    }
    
    public Material getOldMaterial() {
        return this.oldMaterial;
    }
    
    public byte getOldData() {
        return this.oldData;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public void setData(byte data) {
        this.data = data;
    }
}
