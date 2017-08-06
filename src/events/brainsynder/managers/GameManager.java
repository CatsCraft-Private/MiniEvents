package events.brainsynder.managers;

import events.brainsynder.commands.api.CommandManager;
import events.brainsynder.games.*;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import simple.brainsynder.storage.ExpireHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GameManager {
    private static List<Game> games = new ArrayList<>();
    private static ExpireHashMap<String, IGamePlayer> gamePlayerMap = new ExpireHashMap<> ();
    
    public static IGamePlayer getPlayer (Player player) {
        if (gamePlayerMap.containsKey(player.getName())) return gamePlayerMap.get(player.getName());
        IGamePlayer gamePlayer = new GamePlayer(player);
        gamePlayerMap.put(player.getName(), gamePlayer, 5, TimeUnit.HOURS);
        return gamePlayerMap.get(player.getName());
    }
    
    
    /**
     * ==================================================================
     *                    Handle the Game classes
     * ==================================================================
     */
    public static void register (Game game, Plugin plugin) {
        if (!games.contains(game)) {
            games.add(game);
            Bukkit.getServer().getPluginManager().registerEvents(game, plugin);
            CommandManager.register(game);
        }
    }
    
    static void initiate () {
        GamePlugin plugin = GamePlugin.instance;
        register(new BowBattle(), plugin);
        register(new TntRun(), plugin);
        register(new Spleef(), plugin);
        register(new BlitzTag(), plugin);
        register(new LMS(), plugin);
        register(new KOTH(), plugin);
        register(new KO(), plugin);
        register(new Paintball(), plugin);
        register(new Parkour(), plugin);
    }
    
    public static <T extends Game> T getGame(Class<T> clazz) {
        if (games == null) initiate();
        if (games.isEmpty()) initiate();
        for (Game game : games) {
            if (clazz.isAssignableFrom(game.getClass())) {
                return (T) game;
            }
        }
        throw new NullPointerException(clazz.getSimpleName() + " is not a registered Game.");
    }
    
    public static Game getGame(String name) {
        if (games == null) initiate();
        if (games.isEmpty()) initiate();
        for (Game game : games) {
            if (game.getName().equals(name)) {
                return game;
            }
        }
        return null;
    }
    
    public static List<Game> getGames() {
        return GameManager.games;
    }
}
