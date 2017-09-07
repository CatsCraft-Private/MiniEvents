package events.brainsynder;

import events.brainsynder.commands.api.Command;
import events.brainsynder.commands.api.CommandListener;
import events.brainsynder.commands.api.CommandManager;
import events.brainsynder.key.IGamePlayer;
import events.brainsynder.managers.GameManager;
import events.brainsynder.managers.GamePlugin;
import events.brainsynder.utils.BlockLocation;
import events.brainsynder.utils.Cuboid;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import simple.brainsynder.nbt.JsonToNBT;
import simple.brainsynder.nbt.NBTException;
import simple.brainsynder.nbt.StorageTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubeHandler implements Listener,CommandListener {
    private List<Cuboid> cuboids = new ArrayList<>();
    private Map<String, BlockLocation> locationMap = new HashMap<>();

    public void load(GamePlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        CommandManager.register(this);
        DataFile file = plugin.getSettings().getData();
        file.getStringList("Cubes").forEach(line -> {
            try {
                StorageTagCompound compound = JsonToNBT.getTagFromJson(line);
                BlockLocation corner1 = BlockLocation.fromString(compound.getString("corner1"));
                BlockLocation corner2 = BlockLocation.fromString(compound.getString("corner2"));

                cuboids.add(new Cuboid(corner1, corner2));
            } catch (NBTException ignored) {}
        });
    }

    public void unload (GamePlugin plugin) {
        List<String> data = new ArrayList<>();
        cuboids.forEach(cuboid -> {
            StorageTagCompound compound = new StorageTagCompound();
            compound.setString("corner1", cuboid.getCorner1().toDataString());
            compound.setString("corner2", cuboid.getCorner2().toDataString());
            data.add(compound.toString());
        });
        DataFile file = plugin.getSettings().getData();
        file.set("Cubes", data);
    }

    @Command (name = "cubewand")
    public void run (Player player) {
        if (!player.hasPermission("Events.getWand")) return;
        player.getInventory().addItem(GamePlugin.instance.getCubeWand());
    }

    @EventHandler
    public void onBreak (BlockBreakEvent e) {
        if (e.getPlayer().getEquipment().getItemInMainHand().isSimilar(GamePlugin.instance.getCubeWand())) {
            e.setCancelled(true);
            if (locationMap.containsKey(e.getPlayer().getName())) {
                BlockLocation corner1 = locationMap.get(e.getPlayer().getName());
                BlockLocation corner2 = new BlockLocation(e.getBlock().getLocation());
                cuboids.add(new Cuboid(corner1, corner2));
                locationMap.remove(e.getPlayer().getName());
                e.getPlayer().sendMessage("§aCuboid has been set.");
            }else{
                BlockLocation corner1 = new BlockLocation(e.getBlock().getLocation());
                locationMap.put(e.getPlayer().getName(), corner1);
                e.getPlayer().sendMessage("§cCorner One has been set.");
            }
        }
    }

    @EventHandler
    public void onMove (PlayerMoveEvent e) {
        if (cuboids.isEmpty()) return;
        cuboids.forEach(cuboid -> {
            if (cuboid.contains(new BlockLocation(e.getTo()))) {
                //if (e.getPlayer().hasPermission("EventsEntry.bypass")) return;
                IGamePlayer player = GameManager.getPlayer(e.getPlayer());
                if (player.isPlaying()) return;
                if (e.getPlayer().getGameMode() != GameMode.SPECTATOR)
                e.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
        });
    }

    @EventHandler
    public void onChange (PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode() != GameMode.SPECTATOR) {
            if (cuboids.isEmpty()) return;
            cuboids.forEach(cuboid -> {
                if (cuboid.contains(new BlockLocation(e.getPlayer().getLocation()))) {
                    //if (e.getPlayer().hasPermission("EventsEntry.bypass")) return;
                    IGamePlayer player = GameManager.getPlayer(e.getPlayer());
                    if (player.isPlaying()) return;
                    e.setCancelled(true);
                }
            });
        }
    }
}
