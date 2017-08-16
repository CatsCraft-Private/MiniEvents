package events.brainsynder.events.game;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.Game;

public class GameEndEvent<T extends Game> extends GameEvent<T> {
    public GameEndEvent(T game) {
        super(game);
    }
}
