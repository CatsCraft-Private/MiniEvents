package events.brainsynder.events.game;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.teams.ITeamGame;

public class TeamGameEnd extends GameEvent<ITeamGame> {
    public TeamGameEnd(ITeamGame game) {
        super(game);
    }
}
