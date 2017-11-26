package events.brainsynder.games.team;

import de.robingrether.idisguise.disguise.DisguiseType;
import de.robingrether.idisguise.disguise.MobDisguise;
import events.brainsynder.key.GameSettings;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.key.teams.TeamGameMaker;
import events.brainsynder.managers.GameManager;
import events.brainsynder.utils.DisguiseHandler;
import events.brainsynder.utils.RandomRef;
import events.brainsynder.utils.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import simple.brainsynder.api.ParticleMaker;
import simple.brainsynder.nms.ITitleMessage;
import simple.brainsynder.utils.Reflection;

import java.util.*;

public class SnowPack extends TeamGameMaker {
    private int time = 240, per18 = 0;
    private boolean announced = false;
    private List<Integer> keyTimes = Arrays.asList(1, 2, 3, 4, 5, 10, 20, 30, 60, 120, 180);
    private List<String> snowmen = new ArrayList<>();

    public SnowPack(String mapID) {
        super(mapID);
        randomTeams = false;
    }

    public SnowPack() {
        super();
        randomTeams = false;
    }

    @Override
    public String getName() {
        return "SnowPack";
    }
    
    @Override public void onScoreboardLoad(IGamePlayer player) {
        if (player.getScoreHandler() == null) {
            ScoreboardHandler handler = new ScoreboardHandler(player.getPlayer().getUniqueId());
            handler.setTitle(0, "❅ &bSnowPack &f❅");
            handler.setLine(0, 14, "&3Time Left: ", "&b" + RandomRef.formatHHMMSS(time));
            LinkedList<String> listed = new LinkedList<>(getPlayers ());
            int count = 12;
            handler.setLine(0, 13, "&3Players ", "&3In The Event");
            while ((listed.peekFirst() != null) && (count >= 8)) {
                handler.setLine(0, count, "&3❅ ", "&b" + listed.pollFirst());
                count--;
            }
            if (listed.peekFirst() != null) {
                handler.setLineBlank(0, (count - 1));
                handler.setLine(0, (count - 2), "&3❅ ", "&bAnd Some More");
            }
            handler.toggleScoreboard();
            player.setScoreHandler(handler);
        }
    }
    @Override public void onScoreboardUpdate(IGamePlayer player) {
        if (player.getScoreHandler() != null) {
            ScoreboardHandler handler = player.getScoreHandler();
            handler.setLine(0, 14, "&3Time Left: ", "&b" + RandomRef.formatHHMMSS(time));
            LinkedList<String> listed = new LinkedList<>(getPlayers ());
            int count = 12;
            handler.setLine(0, 13, "&3Players ", "&3In The Event");
            while ((listed.peekFirst() != null) && (count >= 8)) {
                handler.setLine(0, count, "&3❅ ", "&b" + listed.pollFirst());
                count--;
            }
            if (listed.peekFirst() != null) {
                handler.setLineBlank(0, (count - 1));
                handler.setLine(0, (count - 2), "&3❅ ", "&bAnd Some More");
            }
        }
    }
    
    @Override
    public void onLeave(IGamePlayer player) {
        super.onLeave(player);
        if (snowmen.contains(player.getPlayer().getName())) {
            if (DisguiseHandler.getApi().isDisguised(player.getPlayer())) {
                DisguiseHandler.getApi().undisguise(player.getPlayer());
            }
            snowmen.remove(player.getPlayer().getName());
            if (snowmen.isEmpty()) {
                tellPlayers (player.getPlayer().getName() + " has left the game, Selecting a new Snowman...");
                randomTagged();
            }
        }
    }

    @Override
    public void perTick() {
        super.perTick();
        if (per18 == 18) {
            time--;
            per18 = 0;
            announced = false;
        }
        per18++;
        if (time <= 0) {
            if (getRedTeam().size() == 0) {
                onWin(getBlueTeam());
            }else{
                onWin(getRedTeam());
            }
            return;
        }
        if (keyTimes.contains(time) && (!announced)) {
            String timeLeft = time + " Second(s)";
            switch (time) {
                case 180:
                    timeLeft = "3 Minutes";
                    break;
                case 120:
                    timeLeft = "2 Minutes";
                    break;
                case 60:
                    timeLeft = "1 Minute";
                    break;

            }

            tellPlayers("§7Time Left: §b" + timeLeft);
            announced = true;
        }
    }

    private void randomTagged() {
        Random r = new Random();
        List<String> alive = new ArrayList<>();
        getPlayers ().stream().filter(player -> (
                (!deadPlayers.contains(GameManager.getPlayer(player).getPlayer().getName()))
                        && (!snowmen.contains(GameManager.getPlayer(player).getPlayer().getName())))).forEach(alive::add);
        if (alive.size() <= 1) {
            onWin(GameManager.getPlayer(alive.get(0)));
            onEnd();
            plugin.getEventMain().end();
            return;
        }
        int a = r.nextInt(alive.size());
        IGamePlayer player = GameManager.getPlayer(alive.get(a));
        setSnowman(player);
    }

    @Override
    public void onStart() {
        getPlayers().forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            player.setTeam(getRedTeam());
            getRedTeam().addMember(player, false);
            player.getPlayer().teleport(getSpawn(player.getTeam()));
        });
        super.onStart();
        gameSettings = new GameSettings(true);
        for (String name : getPlayers ()) {
            Player player = GameManager.getPlayer(name).getPlayer();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 2 seconds to spread out..."));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (getPlayers ().size() != 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.AQUA + "Selecting Snowman..."));
                }
            }, 120L);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                SnowPack.super.onStart();
                randomTagged();
                ITitleMessage title = Reflection.getTitleMessage();
                players.forEach(name -> {
                    IGamePlayer player = GameManager.getPlayer(name);
                    if (!snowmen.contains(name)) {
                        title.sendMessage(player.getPlayer(), 0, 2, 0, "§3Objective:", "§bRun from the Snowmen");
                    }
                });
            }
        }.runTaskLater(plugin, 130);
    }

    @Override
    public void equipPlayer(Player player) {
        equipDefaultPlayer(player);
    }

    @Override
    public void equipDefaultPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    @Override
    public boolean allowsPVP() {
        return true;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        IGamePlayer player = GameManager.getPlayer((Player) event.getEntity());
        if (player.isPlaying()) {
            if (player.getGame() instanceof SnowPack) {
                if (!plugin.getEventMain().eventstarted) {
                    event.setCancelled(true);
                    return;
                }

                if (snowmen.contains(event.getDamager().getName()) && (!snowmen.contains(player.getPlayer().getName()))) {
                    if (getRedTeam().size() == 1) {
                        onWin(getBlueTeam());
                        event.setCancelled(true);
                        return;
                    }

                    setSnowman(player);
                }
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onEnd() {
        getPlayers ().forEach(name -> {
            IGamePlayer player = GameManager.getPlayer(name);
            if (DisguiseHandler.getApi().isDisguised(player.getPlayer()) && snowmen.contains(player.getPlayer().getName())) {
                DisguiseHandler.getApi().undisguise(player.getPlayer());
                snowmen.remove(player.getPlayer().getName());
            }
        });
        super.onEnd();
    }

    private void setSnowman (IGamePlayer player) {
        player.setTeam(getBlueTeam());
        getBlueTeam().addMember(player, false);
        getRedTeam().removeMember(player);
        snowmen.add(player.getPlayer().getName());
        MobDisguise disguise = new MobDisguise(DisguiseType.SNOWMAN);
        disguise.setCustomNameVisible(true);
        disguise.setCustomName(player.getTeam().getChatColor() + player.getPlayer().getName());
        DisguiseHandler.getApi().disguise(player.getPlayer(), disguise);

        actionPlayers("§3§l" + player.getPlayer().getName() + " §8§lhas been turned into a Snowman!");
        ParticleMaker maker = new ParticleMaker(ParticleMaker.Particle.BLOCK_CRACK, 15, 0.5);
        maker.setData(Material.PACKED_ICE);
        maker.sendToLocation(player.getPlayer().getEyeLocation());

        ITitleMessage title = Reflection.getTitleMessage();
        title.sendMessage(player.getPlayer(), 0, 2, 0, "§4Objective:", "§c§lTag all the Runners");
    }

    @Override
    public String[] description() {
        return new String[]{
                "§f❅ §bSnowPack §f❅",
                "§7Don't let the cold get to you...",
                "§7or you might turn into one of them..."
        };
    }
}

