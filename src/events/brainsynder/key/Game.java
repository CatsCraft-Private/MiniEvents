package events.brainsynder.key;

import events.brainsynder.SettingsManager;
import events.brainsynder.commands.api.CommandListener;
import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.events.team.TeamPlayerLeaveEvent;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.managers.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class Game<T> implements Listener, CommandListener {
    public List<IGamePlayer> players;
    public List<IGamePlayer> deadPlayers;
    public GamePlugin plugin;
    public SettingsManager settings;
    private LinkedList<UUID> waitTP;
    private GameSettings gameSettings;

    public Game () {
        gameSettings = new GameSettings ();
        players = new ArrayList<>();
        deadPlayers = new ArrayList<>();
        plugin = GamePlugin.instance;
        settings = plugin.getSettings();
        waitTP = new LinkedList<>();
    }

    protected void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public int minPlayers () {
        return 2;
    }

    /**
     * Run on Game end
     */
    public abstract void onEnd();

    public abstract void lost(T player);

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
        return (players.size() - deadPlayers.size());
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public abstract void onWin(T gamePlayer);

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
    public List<IGamePlayer> getPlayers() {
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
    public void addPlayer(IGamePlayer player) {
        if (!waitTP.contains(player.getPlayer().getUniqueId()))
            waitTP.addLast(player.getPlayer().getUniqueId());
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Remove a player to the Game
     */
    public void removePlayer(IGamePlayer player) {
        if (waitTP.contains(player.getPlayer().getUniqueId()))
            waitTP.remove(player.getPlayer().getUniqueId());
        if (players.contains(player)) {
            players.remove(player);
        }
    }

    public LinkedList<UUID> waitingUUIDs () {
        return waitTP;
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

    public Location getSpawn() {
        World w = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ".world"));
        double x = settings.getData().getDouble("setup." + getName() + ".x");
        double y = settings.getData().getDouble("setup." + getName() + ".y");
        double z = settings.getData().getDouble("setup." + getName() + ".z");
        float yaw = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".yaw"));
        float pitch = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".pitch"));
        return new Location(w, x, y, z, yaw, pitch);
    }
}
