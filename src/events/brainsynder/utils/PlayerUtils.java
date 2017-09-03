package events.brainsynder.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import simple.brainsynder.api.ParticleMaker;
import simple.brainsynder.sound.SoundMaker;

public class PlayerUtils {
    public static void sendBlood (Player p) {
        ParticleMaker maker = new ParticleMaker (ParticleMaker.Particle.ITEM_CRACK, 5, 1.0);
        maker.setData(Material.REDSTONE_BLOCK);
        maker.sendToLocation(p.getEyeLocation());
        SoundMaker.BLOCK_STONE_BREAK.playSound(p.getLocation(), 0.3f, 0.3f);
    }
}
