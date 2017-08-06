//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package events.brainsynder;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;

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
