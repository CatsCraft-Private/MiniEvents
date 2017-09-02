package events.brainsynder.events.team;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;

public class TeamPlayerLeaveEvent extends GameEvent<ITeamGame> {
    private Team team;
    private IGamePlayer player;

    public TeamPlayerLeaveEvent(ITeamGame game, Team team, IGamePlayer player) {
        super(game);
        this.team = team;
        this.player = player;
    }

    public IGamePlayer getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }
}
