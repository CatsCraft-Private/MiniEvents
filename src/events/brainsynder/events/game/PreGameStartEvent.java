package events.brainsynder.events.game;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.Game;

public class PreGameStartEvent<T extends Game> extends GameEvent<T> {
    public PreGameStartEvent(T game) {
        super(game);
    }
}
