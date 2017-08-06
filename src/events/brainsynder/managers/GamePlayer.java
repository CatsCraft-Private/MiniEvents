package events.brainsynder.managers;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.data.StoredData;
import org.bukkit.entity.Player;

class GamePlayer implements IGamePlayer {
    private Game game;
    private Player player;
    private StoredData storedData;
    
    GamePlayer (Player player) {
        this.player = player;
        storedData = new StoredData(player.getUniqueId());
    }
    
    @Override public Game getGame() {
        return game;
    }
    
    @Override public void setGame(Game game) {
        this.game = game;
    }
    
    @Override public Player getPlayer() {
        return player;
    }
    
    @Override public StoredData getPlayerData() {
        return storedData;
    }
    
    @Override public boolean isPlaying() {
        return (game != null);
    }
}
