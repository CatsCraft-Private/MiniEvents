package events.brainsynder.events.team;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;

public class TeamWinEvent extends GameEvent<ITeamGame> {
    private Team team;

    public TeamWinEvent(ITeamGame game, Team team) {
        super(game);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}
