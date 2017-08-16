package events.brainsynder.events.player;

import events.brainsynder.events.GamePlayerEvent;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;

/**
 * Calls when a Player joins a Game
 */
public class GamePlayerWinEvent<T extends Game> extends GamePlayerEvent<T> {
    public GamePlayerWinEvent(T game, IGamePlayer player) {
        super(game, player);
    }
}
