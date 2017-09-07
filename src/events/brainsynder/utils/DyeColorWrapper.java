package events.brainsynder.utils;

import org.bukkit.Color;

public enum DyeColorWrapper {
    WHITE(0, 15, Color.WHITE),
    ORANGE(1, 14, Color.ORANGE),
    MAGENTA(2, 13, Color.fromRGB(255,0,255)),
    LIGHT_BLUE(3, 12, Color.fromRGB(0,223,255)),
    YELLOW(4, 11, Color.YELLOW),
    LIME(5, 10, Color.LIME),
    PINK(6, 9, Color.fromRGB(255,151,151)),
    GRAY(7, 8, Color.GRAY),
    SILVER(8, 7, Color.SILVER),
    CYAN(9, 6, Color.fromRGB(27,165,219)),
    PURPLE(10, 5, Color.PURPLE),
    BLUE(11, 4, Color.BLUE),
    BROWN(12, 3, Color.fromRGB(165,42,42)),
    GREEN(13, 2, Color.GREEN),
    RED(14, 1, Color.RED),
    BLACK(15, 0, Color.BLACK);

    private byte woolData;
    private byte dyeData;
    private Color color;

    DyeColorWrapper(int woolData, int dyeData, Color color) {
        this.woolData = (byte) woolData;
        this.dyeData = (byte) dyeData;
        this.color = color;
    }

    public byte getDyeData() {
        return dyeData;
    }

    public byte getWoolData() {
        return woolData;
    }

    public Color getVanilla () {
        return color;
    }

    public static DyeColorWrapper getPrevious(DyeColorWrapper current) {
        int original = current.ordinal();
        if (original == 0) {
            return BLACK;
        }
        return values()[(original - 1)];
    }

    public static DyeColorWrapper getNext(DyeColorWrapper current) {
        int original = current.ordinal();
        if (original == 15) {
            return WHITE;
        }
        return values()[(original + 1)];
    }

    public static DyeColorWrapper getByWoolData(byte data) {
        for (DyeColorWrapper wrapper : values()) {
            if (wrapper.woolData == data)
                return wrapper;
        }
        return null;
    }

    public static DyeColorWrapper getByDyeData(byte data) {
        for (DyeColorWrapper wrapper : values()) {
            if (wrapper.dyeData == data)
                return wrapper;
        }
        return null;
    }
}