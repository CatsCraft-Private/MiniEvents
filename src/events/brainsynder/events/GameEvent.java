package events.brainsynder.events;

import events.brainsynder.key.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEvent<T extends Game> extends Event {
    private static final HandlerList handlers = new HandlerList();
    private T game;

    public GameEvent (T game) {
        this.game = game;
    }

    public T getGame() {
        return game;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
