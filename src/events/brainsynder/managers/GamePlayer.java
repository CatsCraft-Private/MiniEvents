package events.brainsynder.managers;

import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.Team;
import events.brainsynder.managers.data.StoredData;
import org.bukkit.entity.Player;

class GamePlayer<T extends Game> implements IGamePlayer<T> {
    private T game = null;
    private Team team = null;
    private Player player;
    private State state = State.NOT_PLAYING;
    private StoredData storedData;
    
    GamePlayer (Player player) {
        this.player = player;
        storedData = new StoredData(player);
    }
    
    @Override public T getGame() {
        return game;
    }
    
    @Override public void setGame(T game) {
        this.game = game;
    }

    @Override
    public Team getTeam() {
        return team;
    }

    @Override
    public void setTeam(Team team) {
        this.team = team;
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
