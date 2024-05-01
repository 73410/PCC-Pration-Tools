package limbo.pcc.pcc_limbo_system.task;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class stop implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Plugin config = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
        if (commandSender.hasPermission("pcc.command.stop"))
        {
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED+"[服务器公告]"+ChatColor.AQUA+"服务器将在"+ChatColor.YELLOW+config.getConfig().getInt("stop")+ChatColor.AQUA+"秒后重启，请做好准备");
            new BukkitRunnable()
            {
                int i =config.getConfig().getInt("stop") ;
                public void run()
                {
                    if(i==0)
                    {
                        Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED+"[服务器公告]"+ChatColor.AQUA+"即将重启...");
                        Bukkit.getServer().spigot().restart();
                        this.cancel();
                    }
                    else
                    {
                        Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED+"[重启倒计时]"+ChatColor.YELLOW+i);
                        i--;
                    }
                }
            }.runTaskTimerAsynchronously(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class),0,20);
        }
        else
        {
            commandSender.sendMessage(ChatColor.RED +"[stop倒计时]您没有足够的权限来执行此命令");
        }
        return false;
    }
}
