package limbo.pcc.pcc_limbo_system.command;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class vote implements CommandExecutor, TabExecutor {
    Plugin configs = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> list = configs.getConfig().getStringList("vote");
        if(!configs.getConfig().getBoolean("voteopen")){
            commandSender.sendMessage(ChatColor.RED+"该功能未启用或已停止投票！");
            return false;
        }
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "语法错误！");
            return false;
        }
        String player = commandSender.getName();
        File config = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "voteuser.yml");
        FileConfiguration vote_user = YamlConfiguration.loadConfiguration(config);
        File ccconfig = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "vote_result.yml");
        FileConfiguration vote_result = YamlConfiguration.loadConfiguration(ccconfig);

        if (vote_user.getString(player) == null) {
            if (!list.contains(strings[0])) {
                commandSender.sendMessage(ChatColor.RED + "选项不存在！");
                return false;
            }
            vote_user.set(player, strings[0]);
            int result = vote_result.getInt(strings[0]);
            result = result + 1;
            vote_result.set(strings[0], result);

            int num = vote_result.getInt("总数");
            num+=1;
            vote_result.set("总数",num);
            DecimalFormat df = new DecimalFormat("#.##");
            for (String i : list) {
                double percentage = (double) vote_result.getInt(i) / vote_result.getInt("总数") * 100;
                vote_result.set(i + "百分比", df.format(percentage) + "%");
            }
            try {
                vote_user.save(config);
                vote_result.save(ccconfig);
                commandSender.sendMessage(ChatColor.GREEN + "投票成功！");
            } catch (IOException e) {
                e.printStackTrace();
                commandSender.sendMessage(ChatColor.RED+"投票失败！请联系管理员");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "你已经参加投票了！");
            return false;
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list = configs.getConfig().getStringList("vote");
        if (args.length == 1) {
            return list;
        }
        return null;
    }
}