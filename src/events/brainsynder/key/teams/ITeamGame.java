package events.brainsynder.key.teams;

import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public interface ITeamGame extends Game<Team> {
    Team red = new Team("Red", Color.RED, ChatColor.RED);
    Team blue = new Team("Blue", Color.BLUE, ChatColor.BLUE);

    void randomizePlayers ();

    @Override
    default void onLeave(IGamePlayer player) {
        if (player.getTeam() != null) {
            player.getTeam().removeMember(player);
        }
        GamePlayerLeaveEvent<Game> event = new GamePlayerLeaveEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }
}
