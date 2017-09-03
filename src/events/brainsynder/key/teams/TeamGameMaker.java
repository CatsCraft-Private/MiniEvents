package events.brainsynder.key.teams;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.TeamGameStart;
import events.brainsynder.events.team.TeamPlayerLeaveEvent;
import events.brainsynder.events.team.TeamWinEvent;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.nms.IActionMessage;
import simple.brainsynder.utils.Reflection;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class TeamGameMaker implements ITeamGame {
    private Map<String, Location> locationMap = new HashMap<>();
    private boolean started = false;
    protected boolean endTask = false;
    private IActionMessage message = null;

    @Override
    public void randomizePlayers() {
        for (IGamePlayer p : players) {
            if (blue.size() == red.size()) {
                Random rand = new Random();
                if (rand.nextBoolean()) {
                    red.addMember(p);
                    p.setTeam(red);
                } else {
                    blue.addMember(p);
                    p.setTeam(blue);
                }
            } else {
                if (red.size() > blue.size()) {
                    blue.addMember(p);
                    p.setTeam(blue);
                } else {
                    red.addMember(p);
                    p.setTeam(red);
                }
            }
            p.getPlayer().teleport(getSpawn(p.getTeam()));
        }
    }

    protected Location getSpawn(Team team) {
        if (locationMap.containsKey(team.getName())) return locationMap.get(team.getName());
        World w = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ".team." + team.getName() + ".world"));
        double x = settings.getData().getDouble("setup." + getName() + ".team." + team.getName() + ".x");
        double y = settings.getData().getDouble("setup." + getName() + ".team." + team.getName() + ".y");
        double z = settings.getData().getDouble("setup." + getName() + ".team." + team.getName() + ".z");
        float yaw = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".team." + team.getName() + ".yaw"));
        float pitch = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".team." + team.getName() + ".pitch"));
        locationMap.put(team.getName(), new Location(w, x, y, z, yaw, pitch));

        return locationMap.get(team.getName());
    }

    public Team getOppositeTeam(Team team) {
        if (team.getName().equals(red.getName())) return blue;
        return red;
    }

    @Override
    public void onEnd() {
        started = false;
        endTask = false;
        players.forEach(player -> player.setTeam(null));
        GameEndEvent<ITeamGame> event = new GameEndEvent<>(this);
        Bukkit.getPluginManager().callEvent(event);
    }


    @Override
    public void onLeave(IGamePlayer player) {
        if (player.getTeam() != null) {
            player.getTeam().removeMember(player);
            player.setTeam(null);
        }
        TeamPlayerLeaveEvent event = new TeamPlayerLeaveEvent(this, player.getTeam(), player);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void lost(Team player) {
    }

    @Override
    public void onWin(Team gamePlayer) {
        red.setScore(0);
        blue.setScore(0);

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
        plugin.getEventMain().eventstarting = false;
        plugin.getEventMain().eventstarted = true;
        plugin.getEventMain().waiting = null;
        players.forEach(player -> {
            player.setState(IGamePlayer.State.IN_GAME);
            player.getPlayer().setGameMode(GameMode.ADVENTURE);
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
        if (message == null) return;
        if (!plugin.getEventMain().eventstarted) return;
        if (!started) return;
        players.forEach(player -> message.sendMessage(player.getPlayer(), "§4§lRed Score: §c§l" + ((int) red.getScore()) + " §8§l/ §9§lBlue Score: §b§l" + ((int) blue.getScore())));

    }

    @Override
    public boolean hasStarted() {
        return started;
    }

    @Override
    public void setStarted(boolean started) {
        this.started = started;
    }
}
