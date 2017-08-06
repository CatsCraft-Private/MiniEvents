package events.brainsynder.key;

import java.util.ArrayList;
import java.util.List;

public interface IGameTeam {
    List<IGamePlayer> members = new ArrayList<>();
    
    
    default void equip (IGamePlayer player) {
    
    }
}
