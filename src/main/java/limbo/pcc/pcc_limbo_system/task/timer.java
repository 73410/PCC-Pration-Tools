package limbo.pcc.pcc_limbo_system.task;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class timer extends BukkitRunnable {
    Plugin plugin = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
    @Override
    public void run() {
        FileConfiguration config = plugin.getConfig();
        if(config.getBoolean("hide_when_no_player") && !Bukkit.getOnlinePlayers().isEmpty()){
            List<String> list = config.getStringList("timerin");
            for(String i :list)
            {
                Bukkit.getServer().broadcastMessage(i);
            }
        }
    }
}
