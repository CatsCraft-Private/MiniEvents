package events.brainsynder.key;

import events.brainsynder.managers.data.StoredData;
import org.bukkit.entity.Player;

public interface IGamePlayer {
    Game getGame();
    
    void setGame(Game game);
    
    Player getPlayer();
    
    StoredData getPlayerData();
    
    boolean isPlaying ();
}
