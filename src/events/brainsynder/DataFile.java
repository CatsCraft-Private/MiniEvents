package events.brainsynder;

import events.brainsynder.managers.GamePlugin;
import simple.brainsynder.files.FileMaker;

public class DataFile extends FileMaker {
    public DataFile() {
        super(GamePlugin.instance, "data.yml");
    }
}
