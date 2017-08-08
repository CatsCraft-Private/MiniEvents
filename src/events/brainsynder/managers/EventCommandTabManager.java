package events.brainsynder.managers;

import events.brainsynder.key.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventCommandTabManager implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender cs, Command c, String s, String[] args) {
        List<String> names = new ArrayList<>();
        for (Game g : GameManager.getGames()) {
            if (g.isSetup()) {
                names.add(g.getName());
            }
        }
        List<String> f = new ArrayList<>(); // Just because, it still works.
        StringUtil.copyPartialMatches(args[0], names, f);
        Collections.sort(names);
        return names;
    }
}
