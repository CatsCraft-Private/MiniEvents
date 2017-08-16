package events.brainsynder.events;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;

public class GamePlayerEvent<T extends Game> extends GameEvent<T> {
    private IGamePlayer player;

    public GamePlayerEvent(T game, IGamePlayer player) {
        super(game);
        this.player = player;
    }

    public IGamePlayer getPlayer() {
        return player;
    }
}
