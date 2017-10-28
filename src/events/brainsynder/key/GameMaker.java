package events.brainsynder.key;

import events.brainsynder.events.game.GameEndEvent;
import events.brainsynder.events.game.GameStartEvent;
import events.brainsynder.events.player.GamePlayerLostEvent;
import events.brainsynder.events.player.GamePlayerWinEvent;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.PetHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameMaker extends Game {
    protected boolean endTask = false;
    private boolean started = false;

    public GameMaker(){}

    public GameMaker(String mapID) {
        super(mapID);
    }

    @Override
    public void onEnd() {
        started = false;
        endTask = false;
        GameEndEvent<Game> event = new GameEndEvent<>(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void respawnPlayer(IGamePlayer gamePlayer) {
        gamePlayer.getPlayer().teleport(getSpawn());
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
        players.forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
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
        if (!players.isEmpty()) players.forEach(name -> {
            IGamePlayer gamePlayer = GameManager.getPlayer(name);

            if (hasStarted()) onScoreboardUpdate(gamePlayer);
            if (gamePlayer.getPlayer().getLocation().getBlockY() <= 10) {
                respawnPlayer(gamePlayer);
            }
        });
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
        return (settings.getData().isSet("setup." + getName() + ".world")) || (settings.getData().isSet("setup." + getName() + ".maps.0.world"));
    }
}
