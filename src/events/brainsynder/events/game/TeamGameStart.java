package events.brainsynder.events.game;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.teams.ITeamGame;

public class TeamGameStart extends GameEvent<ITeamGame> {
    public TeamGameStart(ITeamGame game) {
        super(game);
    }
}
