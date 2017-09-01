package events.brainsynder.key.teams;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.TeamGameStart;
import events.brainsynder.events.player.GamePlayerLeaveEvent;
import events.brainsynder.events.team.TeamLostEvent;
import events.brainsynder.events.team.TeamWinEvent;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public abstract class TeamGameMaker implements ITeamGame {
    private boolean started = false;
    protected boolean endTask = false;

    @Override
    public void randomizePlayers() {
        for (IGamePlayer p : players) {
            if (p.getTeam() != null) return;
            if (red.size() < blue.size()) {
                red.addMember(p);
            } else if (blue.size() < red.size()) {
                blue.addMember(p);
            } else {
                Random RandomTeam = new Random();
                int TeamID = RandomTeam.nextInt(1);
                if (TeamID == 0) {
                    red.addMember(p);
                } else {
                    blue.addMember(p);
                }
            }
        }
    }

    public Location getSpawn (Team team) {
        World w = Bukkit.getServer().getWorld(settings.getData().getString("setup." + getName() + ".team." + team.getName() + ".world"));
        double x = settings.getData().getDouble("setup." + getName() + ".team." + team.getName() + ".x");
        double y = settings.getData().getDouble("setup." + getName() + ".team." + team.getName() + ".y");
        double z = settings.getData().getDouble("setup." + getName() + ".team." + team.getName() + ".z");
        float yaw = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".team." + team.getName() + ".yaw"));
        float pitch = Float.intBitsToFloat(settings.getData().getInt("setup." + getName() + ".team." + team.getName() + ".pitch"));
        return new Location(w, x, y, z, yaw, pitch);
    }

    public Team getOppositeTeam (Team team) {
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
        
        GamePlayerLeaveEvent<ITeamGame> event = new GamePlayerLeaveEvent<>(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void lost(Team player) {
        TeamLostEvent event = new TeamLostEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
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

    @Override public void perTick() {}

    @Override public boolean hasStarted() {
        return started;
    }

    @Override public void setStarted(boolean started) {
        this.started = started;
    }
}
