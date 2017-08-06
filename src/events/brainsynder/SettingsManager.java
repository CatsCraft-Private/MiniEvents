//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package events.brainsynder;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class SettingsManager {
    static SettingsManager instance = new SettingsManager();
    Plugin p;
    DataFile data;
    File dfile;

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
