package events.brainsynder.key;

import events.brainsynder.SettingsManager;
import events.brainsynder.commands.api.CommandListener;
import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.events.team.TeamPlayerLeaveEvent;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import events.brainsynder.utils.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import simple.brainsynder.math.MathUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class Game implements Listener, CommandListener {
    public List<String> players;
    public List<String> deadPlayers;
    public GamePlugin plugin;
    public SettingsManager settings;
    protected GameSettings gameSettings;
    private String mapID = null;

    public Game() {
        gameSettings = new GameSettings();
        players = new ArrayList<>();
        deadPlayers = new ArrayList<>();
        plugin = GamePlugin.instance;
        settings = plugin.getSettings();
    }

    public Game(String mapID) {
        this.mapID = mapID;
        gameSettings = new GameSettings();
        players = new ArrayList<>();
        deadPlayers = new ArrayList<>();
        plugin = GamePlugin.instance;
        settings = plugin.getSettings();
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    protected void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public int minPlayers() {
        return 2;
    }

    /**
     * Run on Game end
     */
    public abstract void onEnd();

    public abstract void respawnPlayer(IGamePlayer gamePlayer);

    public void lost(Team team) {}
    public void lost(IGamePlayer player) {}

    public void onLeave(IGamePlayer player) {
        if (this instanceof ITeamGame) {
            TeamPlayerLeaveEvent event = new TeamPlayerLeaveEvent((ITeamGame) this, player.getTeam(), player);
            Bukkit.getPluginManager().callEvent(event);
            return;
        }

        GamePlayerLeaveEvent<Game> event = new GamePlayerLeaveEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }

    public int aliveCount() {
        return (getPlayers ().size() - deadPlayers.size());
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void onScoreboardLoad(IGamePlayer player) {
        if (player.getScoreHandler() == null) {
            ScoreboardHandler handler = new ScoreboardHandler(player.getPlayer().getUniqueId());
            handler.setTitle(0, "&3" + getName());

            LinkedList<String> listed = new LinkedList<>(getPlayers ());
            int count = 13;
            handler.setLine(0, 14, "&3Players ", "&3In The Event");
            while ((listed.peekFirst() != null) && (count >= 9)) {
                handler.setLine(0, count, "&3- ", "&b" + listed.pollFirst());
                count--;
            }
            if (listed.peekFirst() != null) {
                handler.setLineBlank(0, (count - 1));
                handler.setLine(0, (count - 2), "&3- ", "&bAnd Some More");
            }
            handler.toggleScoreboard();
            player.setScoreHandler(handler);
        }
    }

    public void onScoreboardUpdate(IGamePlayer player) {
        if (player.getScoreHandler() != null) {
            ScoreboardHandler handler = player.getScoreHandler();
            LinkedList<String> listed = new LinkedList<>(players);
            int count = 13;
            handler.setLine(0, 14, "&3Players ", "&3In The Event");
            while ((listed.peekFirst() != null) && (count >= 9)) {
                String gamePlayer = listed.pollFirst();
                if (!deadPlayers.contains(gamePlayer)) {
                    handler.setLine(0, count, "&3- ", "&b" + gamePlayer);
                    count--;
                }
            }
            if (listed.peekFirst() != null) {
                handler.setLineBlank(0, (count - 1));
                handler.setLine(0, (count - 2), "&3- ", "&bAnd Some More");
            }
        }
    }

    public void onWin(Team team) {}
    public void onWin(IGamePlayer player) {}

    /**
     * Run on Game Start
     */
    public abstract void onStart();

    /**
     * Runs per tick
     */
    public abstract void perTick();

    /**
     * Equips the items for the player, can be the custom inv setup.
     */
    public abstract void equipPlayer(Player player);

    /**
     * The Default Items for the game.
     */
    public abstract void equipDefaultPlayer(Player player);

    /**
     * Has the game started?
     */
    public abstract boolean hasStarted();

    /**
     * Set if the game has started
     */
    public abstract void setStarted(boolean val);

    /**
     * the Players Currently in the Game
     */
    public List<String> getPlayers() {
        return players;
    }

    public void registerListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Add a player to the Game
     */
    public void addPlayer(String player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Remove a player to the Game
     */
    public void removePlayer(String player) {
        if (players.contains(player)) {
            players.remove(player);
        }
    }

    public boolean isSetup() {
        return false;
    }

    public boolean allowsPVP() {
        return false;
    }

    public String[] description() {
        return new String[0];
    }

    protected String getMapID() {
        if (mapID != null) return mapID;
        List<String> arenaIDs = GameManager.getMapIDs(this);
        if (!arenaIDs.isEmpty()) {
            if (arenaIDs.size() == 1) {
                mapID = arenaIDs.get(0);
                return mapID;
            }

            int random = MathUtils.random(0, (arenaIDs.size() - 1));
            try {
                mapID = arenaIDs.get(random);
                return mapID;
            } catch (Throwable ignored) {
            }
            if (settings.getData().isSet("setup." + getName() + ".world")) {
                return mapID;
            } else {
                if (settings.getData().isSet("setup." + getName() + ".maps.0.world")) {
                    mapID = "0";
                    return mapID;
                }
            }
        }
        mapID = "none";
        return mapID;
    }

    public Location getSpawn() {
        String mapID = getMapID();
        World w = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".world"));
        double x = settings.getData().getDouble("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".x");
        double y = settings.getData().getDouble("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".y");
        double z = settings.getData().getDouble("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".z");
        float yaw = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".yaw"));
        float pitch = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".pitch"));
        return new Location(w, x, y, z, yaw, pitch);
    }
}
