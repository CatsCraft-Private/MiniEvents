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

    default Team getRedTeam (){
        return red;
    }
    default Team getBlueTeam (){
        return blue;
    }

    void randomizePlayers ();

    @Override
    default void onLeave(IGamePlayer player) {
        if (player.getTeam() != null) {
            player.getTeam().removeMember(player);
        }
        GamePlayerLeaveEvent<Game> event = new GamePlayerLeaveEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    default boolean isSetup() {
        return settings.getData().isSet("setup." + getName() + ".team." + red.getName() + ".world")
                && settings.getData().isSet("setup." + getName() + ".team." + blue.getName() + ".world");

    }
}
