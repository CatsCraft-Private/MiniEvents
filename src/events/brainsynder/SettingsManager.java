package events.brainsynder;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;

public class SettingsManager {
    private static SettingsManager instance = new SettingsManager();
    File dfile;
    private Plugin p;
    private DataFile data;

    public static SettingsManager getInstance() {
        return instance;
    }

    public void setup(Plugin p) {
        data = new DataFile();
    }

    public DataFile getData() {
        return data;
    }

    public PluginDescriptionFile getDesc() {
        return p.getDescription();
    }
}
