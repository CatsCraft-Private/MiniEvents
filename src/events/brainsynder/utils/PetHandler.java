package events.brainsynder.utils;

import org.bukkit.entity.Player;
import simplepets.brainsynder.player.PetOwner;

public class PetHandler {
    public static void removePet(Player player) {
        try {
            PetOwner owner = PetOwner.getPetOwner(player);
            if (owner.hasPet()) owner.removePet();
        } catch (Throwable ignored) {
        }
    }
}
