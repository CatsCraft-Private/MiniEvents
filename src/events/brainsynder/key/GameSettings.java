package events.brainsynder.key;

public class GameSettings {
    boolean pvp = false, fallDmg = false, otherDamage = false;

    public GameSettings (boolean pvp, boolean fallDmg, boolean otherDamage) {
        this.pvp = pvp;
        this.fallDmg = fallDmg;
        this.otherDamage = otherDamage;
    }
    public GameSettings (boolean pvp, boolean fallDmg) {
        this.pvp = pvp;
        this.fallDmg = fallDmg;
    }
    public GameSettings (boolean pvp) {
        this.pvp = pvp;
    }
    GameSettings() {}

    public boolean canPvp() {
        return pvp;
    }

    public boolean canTakeOtherDamage() {
        return otherDamage;
    }

    public boolean canTakeFallDmg() {
        return fallDmg;
    }
}
