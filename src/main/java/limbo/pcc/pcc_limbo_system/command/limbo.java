package limbo.pcc.pcc_limbo_system.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class limbo implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!commandSender.hasPermission("pcc.command.limbo")&& !commandSender.hasPermission("pcc.command.limbo.cancel")){
            commandSender.sendMessage(ChatColor.RED+"你没有权限使用该指令");
            return false;
        }
        if(strings.length==0 || strings.length==1){
            commandSender.sendMessage(ChatColor.RED + "缺少参数");
            return false;
        }
        Player player = Bukkit.getPlayer(strings[0]); // 获取玩家对象
        if (player == null) {
            commandSender.sendMessage(ChatColor.RED + "该玩家不在线");
            return true;
        }

        Scoreboard scoreboard = player.getScoreboard(); // 获取玩家计分板对象
        Objective objective = scoreboard.getObjective("limbo"); // 获取名为 "limbo" 的计分板目标

        if (objective == null) {
            commandSender.sendMessage(ChatColor.RED + "计分板目标不存在");
            return true;
        }
        int scores = 0;
        switch (strings[1]){
            case "1":
                scores = 1;
                break;
            case "2":
                scores = 2;
                break;
            case "cancel":
                break;
            default:
                commandSender.sendMessage(ChatColor.RED+"小黑屋选项内容错误");
                return false;
        }
        Score score = objective.getScore(player.getName()); // 获取玩家分数对象

        if(scores==0){
            score.setScore(scores); // 设置玩家分数
            player.setHealth(0);
            commandSender.sendMessage(ChatColor.GREEN+"设置成功！");
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "[小黑屋公告]" + ChatColor.AQUA+"玩家"+ChatColor.DARK_RED+strings[0]+ChatColor.AQUA+"被管理员"+ChatColor.GOLD+commandSender.getName()+ChatColor.AQUA+"从小黑屋里放了出来");
            return true;
        }else if(commandSender.hasPermission("pcc.command.limbo")) {
            score.setScore(scores); // 设置玩家分数
            commandSender.sendMessage(ChatColor.GREEN + "设置成功！");
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "[小黑屋公告]" + ChatColor.AQUA + "玩家" + ChatColor.DARK_RED + strings[0] + ChatColor.AQUA + "被管理员" + ChatColor.GOLD + commandSender.getName() + ChatColor.AQUA + "关进了" + scores + "号小黑屋");
            return true;
        }else{
            commandSender.sendMessage(ChatColor.RED+"你没有权限将玩家关入小黑屋");
            return false;
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length==2)
        {
            List<String> list= new ArrayList<>();
            if(commandSender.hasPermission("pcc.command.limbo")){
                list.add("1");
                list.add("2");
            }
            list.add("cancel");
            return list;
        }
        return null;
    }
}
