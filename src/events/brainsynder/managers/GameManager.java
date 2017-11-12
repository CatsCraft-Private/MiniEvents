package events.brainsynder.managers;

import events.brainsynder.SettingsManager;
import events.brainsynder.commands.api.CommandManager;
import events.brainsynder.games.*;
import events.brainsynder.games.team.Splatoon;
import events.brainsynder.games.team.TDM;
import events.brainsynder.key.Game;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    public static Map<String, IGamePlayer> gamePlayerMap = new HashMap<>();
    private static Map<String, List<String>> idMap = new HashMap<>();
    private static List<Game> games = new ArrayList<>();

    public static IGamePlayer getPlayer(Player player) {
        return gamePlayerMap.putIfAbsent(player.getName(), new GamePlayer(player));
    }

    public static IGamePlayer getPlayer(String name) {
        if (gamePlayerMap.containsKey(name)) return gamePlayerMap.get(name);
        return gamePlayerMap.putIfAbsent(name, new GamePlayer(Bukkit.getPlayerExact(name)));
    }



    /**
     * ==================================================================
     * Handle the Game classes
     * ==================================================================
     */
    public static void resetGames() {
        games.clear();
        idMap.clear();
        initiate();
    }

    public static List<String> getMapIDs (Game game) {
        if (!idMap.containsKey(game.getName())) {
            List<String> list = new ArrayList();
            list.addAll(SettingsManager.getInstance().getData().getSection("setup." + game.getName() + ".maps").getKeys(false));
            idMap.put(game.getName(), list);
            return idMap.get(game.getName());
        }
        return idMap.get(game.getName());
    }

    private static void register(Game game, GamePlugin plugin) {
        if (!games.contains(game)) {
            games.add(game);
            CommandManager.register(game);
        }
    }

    static void initiate() {
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
        register(new TDM(), plugin);
        register(new Splatoon(), plugin);
    }

    public static <T extends Game> T getGame(Class<T> clazz) {
        if (games == null) initiate();
        if (games.isEmpty()) initiate();
        if (clazz.isAssignableFrom(BlitzTag.class)) return (T) new BlitzTag();
        if (clazz.isAssignableFrom(BowBattle.class)) return (T) new BowBattle();
        if (clazz.isAssignableFrom(KO.class)) return (T) new KO();
        if (clazz.isAssignableFrom(KOTH.class)) return (T) new KOTH();
        if (clazz.isAssignableFrom(LMS.class)) return (T) new LMS();
        if (clazz.isAssignableFrom(Paintball.class)) return (T) new Paintball();
        if (clazz.isAssignableFrom(Parkour.class)) return (T) new Parkour();
        if (clazz.isAssignableFrom(Spleef.class)) return (T) new Spleef();
        if (clazz.isAssignableFrom(TntRun.class)) return (T) new TntRun();
        if (clazz.isAssignableFrom(TDM.class)) return (T) new TDM();
        if (clazz.isAssignableFrom(Splatoon.class)) return (T) new Splatoon();
        throw new NullPointerException(clazz.getSimpleName() + " is not a registered Game.");
    }

    public static <T extends Game> T getGame(Class<T> clazz, String mapID) {
        if (games == null) initiate();
        if (games.isEmpty()) initiate();
        if (clazz.isAssignableFrom(BlitzTag.class)) return (T) new BlitzTag(mapID);
        if (clazz.isAssignableFrom(BowBattle.class)) return (T) new BowBattle(mapID);
        if (clazz.isAssignableFrom(KO.class)) return (T) new KO(mapID);
        if (clazz.isAssignableFrom(KOTH.class)) return (T) new KOTH(mapID);
        if (clazz.isAssignableFrom(LMS.class)) return (T) new LMS(mapID);
        if (clazz.isAssignableFrom(Paintball.class)) return (T) new Paintball(mapID);
        if (clazz.isAssignableFrom(Parkour.class)) return (T) new Parkour(mapID);
        if (clazz.isAssignableFrom(Spleef.class)) return (T) new Spleef(mapID);
        if (clazz.isAssignableFrom(TntRun.class)) return (T) new TntRun(mapID);
        if (clazz.isAssignableFrom(TDM.class)) return (T) new TDM(mapID);
        if (clazz.isAssignableFrom(Splatoon.class)) return (T) new Splatoon(mapID);
        throw new NullPointerException(clazz.getSimpleName() + " is not a registered Game.");
    }

    public static Game getGame(String name) {
        if (games == null) initiate();
        if (games.isEmpty()) initiate();
        for (Game game : games) {
            if (game.getName().equalsIgnoreCase(name)) {
                return getGame(game.getClass());
            }
        }
        return null;
    }

    public static Game getGame(String name, String mapID) {
        if (games == null) initiate();
        if (games.isEmpty()) initiate();
        for (Game game : games) {
            if (game.getName().equalsIgnoreCase(name)) {
                return getGame(game.getClass(), mapID);
            }
        }
        return null;
    }

    public static List<Game> getGames() {
        return games;
    }
}
