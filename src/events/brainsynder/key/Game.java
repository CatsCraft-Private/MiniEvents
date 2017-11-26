package events.brainsynder.key;

import events.brainsynder.SettingsManager;
import events.brainsynder.commands.api.CommandListener;
import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.events.team.TeamPlayerLeaveEvent;
import events.brainsynder.key.teams.ITeamGame;
import events.brainsynder.key.teams.Team;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import events.brainsynder.utils.BlockLocation;
import events.brainsynder.utils.Cuboid;
import events.brainsynder.utils.EntityLocation;
import events.brainsynder.utils.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import simple.brainsynder.math.MathUtils;
import simple.brainsynder.nbt.CompressedStreamTools;
import simple.brainsynder.nbt.StorageTagCompound;
import simple.brainsynder.nms.IActionMessage;
import simple.brainsynder.utils.Reflection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class Game implements Listener, CommandListener {
    public List<String> players;
    public List<String> deadPlayers;
    public GamePlugin plugin;
    public SettingsManager settings;
    protected GameSettings gameSettings;
    protected String mapID = null;
    protected StorageTagCompound compound;
    private Cuboid cuboid = null;

    public Game() {
        compound = new StorageTagCompound();
        gameSettings = new GameSettings();
        players = new ArrayList<>();
        deadPlayers = new ArrayList<>();
        plugin = GamePlugin.instance;
        settings = plugin.getSettings();
    }

    public Game(String mapID) {
        if (hasMapID(mapID)) {
            deserialize(getMapFile(mapID));
        }

        this.mapID = mapID;
        compound = new StorageTagCompound();
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

    protected String randomizeMap() {
        if (mapID != null) {
            return deserialize(getMapFile(mapID));
        }

        File folder = getFolder();
        List<File> files = Arrays.asList(folder.listFiles());
        if (!files.isEmpty()) {
            if (files.size() == 1) {
                return deserialize(files.get(0));
            }
            int size = files.size();
            int random = MathUtils.random(0, (size-1));
            return deserialize(files.get(random));
        }
        return null;
    }

    public Location getSpawn() {
        this.mapID = randomizeMap();
        if (compound.hasKey("spawn")) {
            EntityLocation loc = EntityLocation.fromCompound(compound.getCompoundTag("spawn"));
            return loc.toLocation();
        }
        return null;
    }

    public Cuboid getCuboid() {
        return getCuboid(mapID);
    }

    public Cuboid getCuboid(String mapID) {
        if (hasRegion(mapID)) {
            StorageTagCompound cube = compound.getCompoundTag("cube");
            return new Cuboid(BlockLocation.fromCompound(cube.getCompoundTag("corner1")), BlockLocation.fromCompound(cube.getCompoundTag("corner2")));
        }
        return null;
    }

    public boolean hasRegion (String mapID) {
        StorageTagCompound compound = getCompound(mapID);
        return compound.hasKey("cube");
    }

    public void setCuboid(String mapID, BlockLocation corner1, BlockLocation corner2) {
        this.cuboid = new Cuboid(corner1, corner2);
        StorageTagCompound cube = new StorageTagCompound();
        cube.setTag("corner1", corner1.toCompound());
        cube.setTag("corner2", corner2.toCompound());
        compound.setTag("cube", cube);
        save(compound, mapID);
    }

    protected void tellPlayers (String message) {
        players.stream().filter(name -> !deadPlayers.contains(name)).forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        });
    }
    protected void actionPlayers (String message) {
        IActionMessage action = Reflection.getActionMessage();
        players.stream().filter(name -> !deadPlayers.contains(name)).forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            action.sendMessage(player.getPlayer(), ChatColor.translateAlternateColorCodes('&', message));
        });
    }

    public String deserialize (File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            compound = CompressedStreamTools.readCompressed(stream);

            if (compound.hasKey("cube")) {
                StorageTagCompound cube = compound.getCompoundTag("cube");
                cuboid = new Cuboid(BlockLocation.fromCompound(cube.getCompoundTag("corner1")), BlockLocation.fromCompound(cube.getCompoundTag(cube.getString("corner2"))));
            }
            return file.getName().replace(".st", "");
        }catch (Exception ignored) {}
        return file.getName().replace(".st", "");
    }

    public void save (StorageTagCompound compound, String mapID) {
        File file = new File (getFolder(), mapID + ".st");
        try {
            if (!file.exists()) file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(compound, stream);
        }catch (Exception ignored) {}
    }

    public File getMapFile (String mapID) {
        if (hasMapID(mapID)) {
            List<File> files = Arrays.asList(getFolder().listFiles());
            for (File file : files) {
                if (file.getName().startsWith(mapID)) {
                    return file;
                }
            }
        }
        return null;
    }

    public boolean hasMapID (String mapID) {
        List<File> files = Arrays.asList(getFolder().listFiles());
        if (files.isEmpty()) return false;

        for (File file : files) {
            if (file.getName().startsWith(mapID)) {
                return true;
            }
        }
        return false;
    }

    public File getFolder () {
        File folder = new File(GamePlugin.instance.getDataFolder().toString() + File.separator + getName());
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public StorageTagCompound getCompound() {
        return compound;
    }
    public StorageTagCompound getCompound (String mapID) {
        StorageTagCompound compound = new StorageTagCompound();
        if (hasMapID(mapID)) {
            File file = getMapFile(mapID);
            try {
                FileInputStream stream = new FileInputStream(file);
                compound = CompressedStreamTools.readCompressed(stream);
            }catch (Exception ignored) {}
        }
        return compound;
    }
}
