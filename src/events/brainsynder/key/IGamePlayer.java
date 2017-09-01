package events.brainsynder.key;

import events.brainsynder.key.teams.Team;
import events.brainsynder.managers.data.StoredData;
import org.bukkit.entity.Player;

public interface IGamePlayer<T extends Game> {
    T getGame();

    void setGame(T game);

    Team getTeam();

    void setTeam(Team team);
    
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
