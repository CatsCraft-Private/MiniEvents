package events.brainsynder;

import events.brainsynder.key.Game;
import events.brainsynder.managers.GamePlugin;

public class EventsMain {
    public boolean eventstarting = false;
    public boolean eventstarted = false;
    public boolean cancelled = false;
    public Game waiting = null;
    public GamePlugin plugin;
    SettingsManager settings = SettingsManager.getInstance();

    public EventsMain(GamePlugin plugin) {
        plugin = plugin;
    }

    public void end() {
        waiting = null;
        eventstarted = false;
        eventstarting = false;
        cancelled = true;
    }
}
