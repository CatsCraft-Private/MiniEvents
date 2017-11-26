package events.brainsynder.key.teams;

import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.key.Game;
import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;

public abstract class ITeamGame extends GameMaker {

    public ITeamGame(){}
    public ITeamGame(String mapID) {
        super(mapID);
    }

    public abstract Team getRedTeam();

    public abstract Team getBlueTeam();

    public abstract void randomizePlayers();

    @Override
    public void onLeave(IGamePlayer player) {
        if (player.getTeam() != null) {
            player.getTeam().removeMember(player);
        }
        GamePlayerLeaveEvent<Game> event = new GamePlayerLeaveEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }
}
