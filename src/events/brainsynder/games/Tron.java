package events.brainsynder.games;

import events.brainsynder.key.GameMaker;
import events.brainsynder.key.IGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

//TODO: Make this an Actual Event
public class Tron extends GameMaker {
    @Override
    public String getName() {
        return "Tron";
    }

    @Override
    public void perTick() {
        super.perTick();
        //TODO: Handle the movement of the "Cycles" (Cats)
    }

    @Override
    public void onStart() {
        for (IGamePlayer gamePlayer : players) {
            Player player = gamePlayer.getPlayer();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have 2 seconds to spread out..."));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (players.size() != 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.AQUA + "Selecting Tagger..."));
                }
            }, 120L);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Tron.super.onStart();
            }
        }.runTaskLater(plugin, 130);
    }

    @Override // There are no Kits for Tron
    public void equipPlayer(Player player) {}

    @Override // There are no Kits for Tron
    public void equipDefaultPlayer(Player player) {}

    @Override
    public String[] description() {
        return new String[]{
                "§eTron§7 is an event made",
                "§7to mimic the 1980s Movie 'Tron'" ,
                "§7Where you try and eliminate the Enemies",
                "§7by using a 'Light Cycle' make them cross into your line",
                "§7or force them into a corner."
        };
    }

    @Override
    public void onEnd() {
        super.onEnd();
    }
}

