package events.brainsynder.events.game;

import events.brainsynder.events.GameEvent;
import events.brainsynder.key.teams.ITeamGame;

public class PreTeamGameStart extends GameEvent<ITeamGame> {
    public PreTeamGameStart(ITeamGame game) {
        super(game);
    }
}
