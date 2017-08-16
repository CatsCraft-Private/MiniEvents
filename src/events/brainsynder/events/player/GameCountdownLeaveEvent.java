package events.brainsynder.events.player;

import events.brainsynder.events.GamePlayerEvent;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;

/**
 * Calls when a Player leave an event before it starts (During the Countdown Phase)
 */
public class GameCountdownLeaveEvent<T extends Game> extends GamePlayerEvent<T> {
    public GameCountdownLeaveEvent(T game, IGamePlayer player) {
        super(game, player);
    }
}
