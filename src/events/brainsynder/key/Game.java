package events.brainsynder.key;

import events.brainsynder.SettingsManager;
import events.brainsynder.commands.api.CommandListener;
import events.brainsynder.events.player.GamePlayerLeaveEvent;
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

public interface Game<T> extends Listener, CommandListener {
    public List<IGamePlayer> players = new ArrayList<>();
    List<IGamePlayer> deadPlayers = new ArrayList<>();
    GamePlugin plugin = GamePlugin.instance;
    SettingsManager settings = plugin.getSettings();
    LinkedList<UUID> waitTP = new LinkedList<>();
    
    default int minPlayers () {
        return 2;
    }

    /**
     * Run on Game end
     */
    void onEnd();

    void lost(T player);

    default void onLeave(IGamePlayer player) {
        GamePlayerLeaveEvent<Game> event = new GamePlayerLeaveEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }

    default int aliveCount() {
        return (players.size() - deadPlayers.size());
    }

    default String getName() {
        return getClass().getSimpleName();
    }

    void onWin(T gamePlayer);

    /**
     * Run on Game Start
     */
    void onStart();

    /**
     * Runs per tick
     */
    void perTick();

    /**
     * Equips the items for the player, can be the custom inv setup.
     */
    void equipPlayer(Player player);

    /**
     * The Default Items for the game.
     */
    void equipDefaultPlayer(Player player);

    /**
     * Has the game started?
     */
    boolean hasStarted();

    /**
     * Set if the game has started
     */
    void setStarted(boolean val);

    /**
     * the Players Currently in the Game
     */
    default List<IGamePlayer> getPlayer() {
        return players;
    }

    default void registerListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    default void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Add a player to the Game
     */
    default void addPlayer(IGamePlayer player) {
        if (!waitTP.contains(player.getPlayer().getUniqueId()))
            waitTP.addLast(player.getPlayer().getUniqueId());
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Remove a player to the Game
     */
    default void removePlayer(IGamePlayer player) {
        if (waitTP.contains(player.getPlayer().getUniqueId()))
            waitTP.remove(player.getPlayer().getUniqueId());
        if (players.contains(player)) {
            players.remove(player);
        }
    }

    default LinkedList<UUID> waitingUUIDs () {
        return waitTP;
    }

    default boolean isSetup() {
        return false;
    }

    default boolean allowsPVP() {
        return false;
    }

    default String[] description() {
        return new String[0];
    }

    default Location getSpawn() {
        World w = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ".world"));
        double x = settings.getData().getDouble("setup." + getName() + ".x");
        double y = settings.getData().getDouble("setup." + getName() + ".y");
        double z = settings.getData().getDouble("setup." + getName() + ".z");
        float yaw = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".yaw"));
        float pitch = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".pitch"));
        return new Location(w, x, y, z, yaw, pitch);
    }
}
