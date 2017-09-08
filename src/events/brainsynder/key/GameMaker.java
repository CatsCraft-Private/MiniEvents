package events.brainsynder.key;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.GameStartEvent;
import events.brainsynder.events.player.GamePlayerLostEvent;
import events.brainsynder.events.player.GamePlayerWinEvent;
import events.brainsynder.utils.PetHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameMaker extends Game<IGamePlayer> {
    private boolean started = false;
    protected boolean endTask = false;

    @Override
    public void onEnd() {
        started = false;
        endTask = false;
        GameEndEvent<Game> event = new GameEndEvent<>(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onWin(IGamePlayer gamePlayer) {
        GamePlayerWinEvent<Game> event = new GamePlayerWinEvent<>(this, gamePlayer);
        Bukkit.getPluginManager().callEvent(event);
        onEnd();
    }

    @Override
    public void lost(IGamePlayer player) {
        GamePlayerLostEvent<Game> event = new GamePlayerLostEvent<>(this, player);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onStart() {
        GameStartEvent<Game> event = new GameStartEvent<>(this);
        Bukkit.getPluginManager().callEvent(event);
        started = true;
        plugin.getEventMain().eventstarting = false;
        plugin.getEventMain().eventstarted = true;
        plugin.getEventMain().waiting = null;
        players.forEach(player -> {
            player.setState(IGamePlayer.State.IN_GAME);
            player.getPlayer().setGameMode(GameMode.ADVENTURE);
            PetHandler.removePet(player.getPlayer());
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
        return (settings.getData().isSet("setup." + getName() + ".world"));
    }
}
