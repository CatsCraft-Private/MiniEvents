package events.brainsynder.key;

import events.brainsynder.managers.data.StoredData;
import org.bukkit.entity.Player;

public interface IGamePlayer {
    Game getGame();
    
    void setGame(Game game);
    
    Player getPlayer();
    
    StoredData getPlayerData();
    
    boolean isPlaying ();

    State getState ();

    void setState (State state);

    enum State {
        NOT_PLAYING,
        IN_GAME,
        IN_GAME_ARENA,
        WAITING
    }
}
