package limbo.pcc.pcc_limbo_system.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class gong_gao implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length==0)
        {
            commandSender.sendMessage(ChatColor.RED+"语法错误");
        }
        else
        {
            if (commandSender.hasPermission("pcc.command.gonggao"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.RED + "[服务器公告]" + ChatColor.WHITE + args[0]+ChatColor.AQUA+"——来自"+commandSender.getName());
            }
            else
            {
                commandSender.sendMessage(ChatColor.RED + "您没有足够的权限来执行此命令");
            }
        }
        return false;
    }
}
