package events.brainsynder.key.teams;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.TeamGameStart;
import events.brainsynder.events.team.TeamPlayerLeaveEvent;
import events.brainsynder.events.team.TeamWinEvent;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.DyeColorWrapper;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.nms.IActionMessage;
import simple.brainsynder.utils.Reflection;

import java.util.*;

public abstract class TeamGameMaker extends ITeamGame {
    protected IActionMessage message = null;
    private Map<String, Location> locationMap = new HashMap<>();
    private boolean started = false;
    private boolean endTask = false;
    private Team red;
    private Team blue;


    public TeamGameMaker(String mapID) {
        super(mapID);
    }

    public TeamGameMaker() {
        super();
        red = new Team("Red", DyeColorWrapper.RED, ChatColor.RED);
        blue = new Team("Blue", DyeColorWrapper.BLUE, ChatColor.BLUE);
    }

    @Override
    public Team getBlueTeam() {
        return blue;
    }

    @Override
    public Team getRedTeam() {
        return red;
    }

    @Override
    public void randomizePlayers() {
        List<String> redMembers = new ArrayList<>();
        List<String> blueMembers = new ArrayList<>();

        for (String name : getPlayers ()) {
            if (blueMembers.size() == redMembers.size()) {
                Random rand = new Random();
                if (rand.nextBoolean()) {
                    redMembers.add(name);
                } else {
                    blueMembers.add(name);
                }
            } else {
                if (redMembers.size() > blueMembers.size()) {
                    blueMembers.add(name);
                } else {
                    redMembers.add(name);
                }
            }
        }

        redMembers.forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            getRedTeam().addMember(player);
            player.setTeam(getRedTeam());
            player.getPlayer().teleport(getSpawn(player.getTeam()));
        });

        blueMembers.forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            getBlueTeam().addMember(player);
            player.setTeam(getBlueTeam());
            player.getPlayer().teleport(getSpawn(player.getTeam()));
        });
    }

    @Override
    public void respawnPlayer(IGamePlayer player) {
        if (player.getTeam() != null) player.getPlayer().teleport(getSpawn(player.getTeam()));
    }


    protected Location getSpawn(Team team) {
        String mapID = getMapID();
        if (locationMap.containsKey(team.getName())) return locationMap.get(team.getName());
        World w = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".team." + team.getName() + ".world"));
        double x = settings.getData().getDouble("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".team." + team.getName() + ".x");
        double y = settings.getData().getDouble("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".team." + team.getName() + ".y");
        double z = settings.getData().getDouble("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".team." + team.getName() + ".z");
        float yaw = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".team." + team.getName() + ".yaw"));
        float pitch = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ((!mapID.equals("none")) ? (".maps." + mapID) : "") + ".team." + team.getName() + ".pitch"));
        locationMap.put(team.getName(), new Location(w, x, y, z, yaw, pitch));

        return locationMap.get(team.getName());
    }

    protected Team getOppositeTeam(Team team) {
        if (team.getName().equals(getRedTeam().getName())) return getBlueTeam();
        return getRedTeam();
    }

    @Override
    public void onEnd() {
        started = false;
        endTask = false;
        getRedTeam().setScore(0);
        getBlueTeam().setScore(0);
        players.forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            player.setTeam(null);
        });
        GameEndEvent<ITeamGame> event = new GameEndEvent<>(this);
        Bukkit.getPluginManager().callEvent(event);
    }


    @Override
    public void onLeave(IGamePlayer player) {
        if (player.getTeam() != null) {
            player.getTeam().removeMember(player);
        }
        TeamPlayerLeaveEvent event = new TeamPlayerLeaveEvent(this, player.getTeam(), player);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void lost(Team player) {
    }

    @Override
    public void onWin(Team gamePlayer) {
        TeamWinEvent event = new TeamWinEvent(this, gamePlayer);
        Bukkit.getPluginManager().callEvent(event);
        onEnd();
    }

    @Override
    public void onStart() {
        message = Reflection.getActionMessage();
        randomizePlayers();
        TeamGameStart event = new TeamGameStart(this);
        Bukkit.getPluginManager().callEvent(event);
        started = true;
        players.forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            player.setState(IGamePlayer.State.IN_GAME);
            player.getPlayer().setGameMode(GameMode.ADVENTURE);
            try {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pet remove " + player.getPlayer().getName());
            } catch (Throwable ignored) {
            }
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getEventMain().eventstarted) {
                    cancel();
                    return;
                }
                if (!started) {
                    cancel();
                    return;
                }

                if (players.size() <= 1) {
                    cancel();
                    return;
                }

                if (endTask) {
                    cancel();
                    return;
                }
                perTick();
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void perTick() {
        if (!players.isEmpty()) {
            players.forEach(name -> {
                IGamePlayer gamePlayer = GameManager.getPlayer(name);
                if (hasStarted()) onScoreboardUpdate(gamePlayer);
                if (gamePlayer.getPlayer().getLocation().getBlockY() <= 10) {
                    respawnPlayer(gamePlayer);
                }
            });
        }
    }

    @Override
    public boolean hasStarted() {
        return started;
    }

    @Override
    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public boolean isSetup() {
        return ((settings.getData().isSet("setup." + getName() + ".team.Red.world")
                && settings.getData().isSet("setup." + getName() + ".team.Blue.world"))
                || (settings.getData().isSet("setup." + getName() + ".maps.0.team.Red.world")
                && settings.getData().isSet("setup." + getName() + ".maps.0.team.Blue.world")));

    }
}
