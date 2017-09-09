package events.brainsynder.utils;

import de.robingrether.idisguise.api.DisguiseAPI;
import org.bukkit.Bukkit;

public class DisguiseHandler {
    private static DisguiseAPI api = null;

    static {
        if (api == null)
            api = Bukkit.getServer().getServicesManager().getRegistration(DisguiseAPI.class).getProvider();
    }

    public static DisguiseAPI getApi() {
        return api;
    }
}
