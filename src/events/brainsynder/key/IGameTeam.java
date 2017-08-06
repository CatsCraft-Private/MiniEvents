package events.brainsynder.key;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is basically a begin to Team Events
 */
public interface IGameTeam {
    List<IGamePlayer> members = new ArrayList<>();
    
    
    default void equip (IGamePlayer player) {
    
    }
}
