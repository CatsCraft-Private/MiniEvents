package events.brainsynder.events.team;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;

public class TeamLostEvent extends GameEvent<ITeamGame> {
    private Team team;

    public TeamLostEvent(ITeamGame game, Team team) {
        super(game);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}
