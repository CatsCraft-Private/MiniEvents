package events.brainsynder.key.teams;

import events.brainsynder.key.IGamePlayer;
import events.brainsynder.utils.DyeColorWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import simple.brainsynder.api.LeatherArmorMaker;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private List<String> members = new ArrayList<>();
    private DyeColorWrapper color = null;
    private double score = 0;
    private ChatColor chatColor = null;

    public Team(String name, DyeColorWrapper color, ChatColor chatColor) {
        this.name = name;
        this.color = color;
        this.chatColor = chatColor;
    }

    public int size() {
        return members.size();
    }

    public void addMember(IGamePlayer player) {
        addMember(player, true);
    }

    public void addMember(IGamePlayer player, boolean armor) {
        if (armor) {
            Player p = player.getPlayer();
            p.getInventory().setHelmet(new LeatherArmorMaker(Material.LEATHER_HELMET).setColor(color.getVanilla()).setName(chatColor + name + " Team Armor").create());
            p.getInventory().setChestplate(new LeatherArmorMaker(Material.LEATHER_CHESTPLATE).setColor(color.getVanilla()).setName(chatColor + name + " Team Armor").create());
            p.getInventory().setLeggings(new LeatherArmorMaker(Material.LEATHER_LEGGINGS).setColor(color.getVanilla()).setName(chatColor + name + " Team Armor").create());
            p.getInventory().setBoots(new LeatherArmorMaker(Material.LEATHER_BOOTS).setColor(color.getVanilla()).setName(chatColor + name + " Team Armor").create());
        }
        if (!members.contains(player.getPlayer().getName())) members.add(player.getPlayer().getName());
    }

    public void removeMember(IGamePlayer player) {
        if (members.contains(player.getPlayer().getName())) members.remove(player.getPlayer().getName());
    }

    public DyeColorWrapper getColor() {
        return color;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getName() {
        return name;
    }

    public List<String> getMembers() {
        return members;
    }
}
