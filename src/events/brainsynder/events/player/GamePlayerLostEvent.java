package events.brainsynder.events.player;

import events.brainsynder.events.GamePlayerEvent;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;

/**
 * Calls when a Player leaves an event that has already started.
 */
public class GamePlayerLostEvent<T extends Game> extends GamePlayerEvent<T> {
    public GamePlayerLostEvent(T game, IGamePlayer player) {
        super(game, player);
    }
}
