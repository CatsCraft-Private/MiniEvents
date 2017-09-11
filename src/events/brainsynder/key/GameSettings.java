package events.brainsynder.key;

public class GameSettings {
    boolean pvp = false, fallDmg = false;

    public GameSettings(boolean pvp, boolean fallDmg) {
        this.pvp = pvp;
        this.fallDmg = fallDmg;
    }

    public GameSettings(boolean pvp) {
        this.pvp = pvp;
    }

    GameSettings() {
    }

    public boolean canPvp() {
        return pvp;
    }

    public boolean canTakeFallDmg() {
        return fallDmg;
    }
}
