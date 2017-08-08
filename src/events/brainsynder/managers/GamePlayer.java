package events.brainsynder.managers;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.data.StoredData;
import org.bukkit.entity.Player;

class GamePlayer implements IGamePlayer {
    private Game game;
    private Player player;
    private State state;
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

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override public Player getPlayer() {
        return player;
    }
    
    @Override public StoredData getPlayerData() {
        return storedData;
    }
    
    @Override public boolean isPlaying() {
        return ((game != null) && ((state == State.IN_GAME_ARENA) || (state == State.IN_GAME)));
    }
}
