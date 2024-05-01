package limbo.pcc.pcc_limbo_system.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class shout implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "缺少参数");
            return false;
        }
        if(commandSender.hasPermission("pcc.command.shout"))
        {
            String who = commandSender.getName();
            if (Objects.equals(args[0], "yellow"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[管理员:"+ChatColor.GOLD+who+ChatColor.DARK_RED+"喊话]" + ChatColor.YELLOW + args[1]);
            }
            else if (Objects.equals(args[0], "green"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[管理员:"+ChatColor.GOLD+who+ChatColor.DARK_RED+"喊话]" + ChatColor.GREEN + args[1]);
            }
            else if (Objects.equals(args[0], "blue"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[管理员:"+ChatColor.GOLD+who+ChatColor.DARK_RED+"喊话]" + ChatColor.BLUE + args[1]);
            }
            else if (Objects.equals(args[0], "red"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[管理员:"+ChatColor.GOLD+who+ChatColor.DARK_RED+"喊话]" + ChatColor.RED + args[1]);
            }
            else if (Objects.equals(args[0], "white"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[管理员:"+ChatColor.GOLD+who+ChatColor.DARK_RED+"喊话]" + ChatColor.WHITE + args[1]);
            }
            else if (Objects.equals(args[0], "aqua"))
            {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[管理员:"+ChatColor.GOLD+who+ChatColor.DARK_RED+"喊话]" + ChatColor.AQUA + args[1]);
            }
            else
            {
                Bukkit.getServer().broadcastMessage(ChatColor.RED + "[管理员喊话]" + ChatColor.WHITE + args[1]);
            }
        }
        else
        {
            commandSender.sendMessage(ChatColor.RED +"您没有足够的权限来执行此命令");
        }
    return false;
}

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1)
        {
            List<String> list= new ArrayList<>();
            list.add("yellow");
            list.add("green");
            list.add("blue");
            list.add("red");
            list.add("white");
            list.add("aqua");
            return list;
        }
        return null;
    }
}
