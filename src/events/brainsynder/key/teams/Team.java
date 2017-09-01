package events.brainsynder.key.teams;

import events.brainsynder.key.IGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import simple.brainsynder.api.LeatherArmorMaker;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private List<IGamePlayer> members = new ArrayList<> ();
    private Color color = null;
    private double score = 0;
    private ChatColor chatColor = null;
    private Scoreboard board = null;

    public Team (String name, Color color, ChatColor chatColor) {
        this.name = name;
        this.color = color;
        this.chatColor = chatColor;
    }

    public int size () {
        return members.size();
    }

    public void addMember (IGamePlayer player) {
        Player p = player.getPlayer();
        p.getInventory().setHelmet(new LeatherArmorMaker(Material.LEATHER_HELMET).setColor(color).setName(chatColor + name + " Team Armor").create());
        p.getInventory().setChestplate(new LeatherArmorMaker(Material.LEATHER_CHESTPLATE).setColor(color).setName(chatColor + name + " Team Armor").create());
        p.getInventory().setLeggings(new LeatherArmorMaker(Material.LEATHER_LEGGINGS).setColor(color).setName(chatColor + name + " Team Armor").create());
        p.getInventory().setBoots(new LeatherArmorMaker(Material.LEATHER_BOOTS).setColor(color).setName(chatColor + name + " Team Armor").create());
        if (!members.contains(player)) members.add(player);
    }

    public void removeMember (IGamePlayer player) {
        if (members.contains(player)) members.remove(player);
    }

    public Color getColor() {
        return color;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getName() {
        return name;
    }
}
