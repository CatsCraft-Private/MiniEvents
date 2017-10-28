package events.brainsynder.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class ScoreboardHandler {
    private Scoreboard scoreboard;
    private int lastPage;
    private final UUID uuid;

    private Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getLastPage() {
        return lastPage;
    }

    private Objective getPage(int page) {
        if (page > 15)
            throw new IllegalArgumentException("Page number must be between 0 and 15");

        Objective obj = scoreboard.getObjective("page" + page);
        if (obj == null) {
            obj = scoreboard.registerNewObjective("page" + page, "dummy");
            for (int i = 0; i < 15; i++)
                scoreboard.registerNewTeam(ChatColor.getByChar(Integer.toHexString(page))
                        + ChatColor.getByChar(Integer.toHexString(i)).toString());
        }
        return obj;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public ScoreboardHandler(UUID uuid) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.uuid = uuid;
        Objective obj = getPage(0);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void setTitle(int page, String title) {
        if (title == null)
            title = "";
        if (title.length() > 32)
            title = title.substring(0, 32);
        getPage(page).setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
    }


    public void toggleScoreboard() {
        if (getPlayer() != null && !getPlayer().getScoreboard().equals(scoreboard)) {
            getPlayer().setScoreboard(scoreboard);
        } else if (scoreboard.getObjective(DisplaySlot.SIDEBAR) == null) {
            getPage(lastPage).setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    public void changePage(int page) {
        lastPage = page;
        if (scoreboard.getObjective(DisplaySlot.SIDEBAR) != null)
            getPage(page).setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void setLineBlank(int page, int index) {
        setLine(page, index, "", "", false);
    }

    public void setLine(int page, int index, String string) {
        setLine(page, index, ChatColor.translateAlternateColorCodes('&', string), true);
    }

    public void setLine(int page, int index, String prefix, String suffix) {
        setLine(page, index, prefix, suffix, true);
    }

    public void setLine(int page, int index, String string, boolean copyPreviousColors) {
        if (string.length() > 16)
            setLine(page, index, string.substring(0, 16), string.substring(16), copyPreviousColors);
        else
            setLine(page, index, string, "", copyPreviousColors);
    }

    public void setLine(int page, int index, String prefix, String suffix, boolean copyPreviousColors) {
        if (prefix.length() > 16)
            prefix = prefix.substring(0, 16);
        if (suffix.length() > 16)
            suffix = suffix.substring(0, 16);
        if (index < 0 || index > 14)
            throw new IllegalArgumentException("You can only get a line from 0 - 14");
        Objective obj = getPage(page);
        String name = ChatColor.getByChar(Integer.toHexString(page))
                + ChatColor.getByChar(Integer.toHexString(index)).toString();
        Score score = obj.getScore(name + ChatColor.RESET);
        Team team = scoreboard.getTeam(name);
        if (!score.isScoreSet()) {
            score.setScore(index);
            team.addEntry(score.getEntry());
        }

        team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));

        if (copyPreviousColors) {
            suffix = ChatColor.getLastColors(prefix) + suffix;
            if (suffix.length() > 16)
                suffix = suffix.substring(0, 16);
        }

        team.setSuffix(ChatColor.translateAlternateColorCodes('&', suffix));
    }
}