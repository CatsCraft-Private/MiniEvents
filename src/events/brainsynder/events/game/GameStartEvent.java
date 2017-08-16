package events.brainsynder.events.game;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.Game;

public class GameStartEvent<T extends Game> extends GameEvent<T> {
    public GameStartEvent(T game) {
        super(game);
    }
}
