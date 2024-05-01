package limbo.pcc.pcc_limbo_system.command;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class main implements CommandExecutor, TabExecutor {
    Plugin config = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length==0)
        {
            commandSender.sendMessage(ChatColor.YELLOW + "插件主指令");
            return false;
        }
        if(!commandSender.hasPermission("pcc.command.config"))
        {
            commandSender.sendMessage(ChatColor.RED + "您没有足够的权限来执行此命令");
            return false;
        }
        if (Objects.equals(strings[0], "reload"))
        {
            config.reloadConfig();
            commandSender.sendMessage("[pcc]重载成功");
        }
        else if(Objects.equals(strings[0],"vote")){
            File ccconfig = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(),"vote_result.yml");
            FileConfiguration vote_result = YamlConfiguration.loadConfiguration(ccconfig);
            File configs = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "voteuser.yml");
            FileConfiguration vote_user = YamlConfiguration.loadConfiguration(configs);
            List<String> list = config.getConfig().getStringList("vote");
            if(strings.length==1){
                commandSender.sendMessage(ChatColor.RED+"语法错误");
                return false;
            }
            else if(Objects.equals(strings[1],"on")){
                if (list.size() == 0) {
                    commandSender.sendMessage("[pcc]投票列表为空！");
                }
                else {
                    for (String i : list) {
                        if(vote_result.getString(i)==null) {
                            vote_result.set(i, 0);
                            vote_result.set(i+"百分比",0+"%");
                            commandSender.sendMessage("[pcc]检测到配置文件中不存在"+i+",正在添加");
                        }
                        else{
                            commandSender.sendMessage("[pcc]已存在"+i);
                        }
                    }
                    if(vote_result.getString("总数")==null){
                        vote_result.set("总数",0);
                    }
                    try {
                        vote_result.save(ccconfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    config.getConfig().set("voteopen",true);
                    config.saveConfig();
                    config.reloadConfig();
                    commandSender.sendMessage(ChatColor.GREEN+"启用成功！");
                }
            }
            else if(Objects.equals(strings[1],"off")){
                config.getConfig().set("voteopen",false);
                config.saveConfig();
                config.reloadConfig();
                commandSender.sendMessage(ChatColor.GREEN+"关闭成功！");
            }
            else if(Objects.equals(strings[1],"result")){
                for (String i : list) {
                    commandSender.sendMessage(ChatColor.AQUA+i+"百分比："+ChatColor.WHITE+vote_result.getString(i+"百分比"));
                }
                commandSender.sendMessage(ChatColor.AQUA+"参加人数："+ChatColor.WHITE+vote_result.getInt("总数"));
            }
            else if(Objects.equals(strings[1],"reset")){
                Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).saveResource("voteuser.yml", true);
                Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).saveResource("vote_result.yml", true);
                for (String i : list) {
                    vote_result.set(i, 0);
                    vote_result.set(i+"百分比",0+"%");
                    commandSender.sendMessage("[pcc]正在添加"+i);
                }
                vote_result.set("总数",0);
                try {
                    vote_result.save(ccconfig);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                config.saveConfig();
                config.reloadConfig();
                commandSender.sendMessage(ChatColor.GREEN+"重置数据成功！！");
            }
            else if(Objects.equals(strings[1],"pull")){
                List <String> resourselist =config.getConfig().getStringList("voteresourse");
                String title = config.getConfig().getString("votetitle");
                Bukkit.getServer().broadcastMessage(ChatColor.GREEN+title+"（点击下面选项投票）");
                for(int i=0;i<list.size();i++ ){
                    TextComponent message = new TextComponent(ChatColor.GREEN+list.get(i)+".");
                    TextComponent exa = new TextComponent(resourselist.get(i));
                    message.addExtra(exa);
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote "+list.get(i)));
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA+"投票给"+list.get(i))));
                    Bukkit.getServer().spigot().broadcast(message);
                }
            }
            else{
                commandSender.sendMessage(ChatColor.RED+"语法错误！");
            }
        }
        else
        {
            commandSender.sendMessage(ChatColor.YELLOW + "插件主指令");
        }
    return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!commandSender.hasPermission("pcc.command.config")){
            return null;
        }
        if(strings.length==1){
            List<String> list= new ArrayList<>();
            list.add("reload");
            list.add("vote");
            return list;
        }
        else if(strings.length==2 && Objects.equals(strings[0],"vote")){
            List<String> lists= new ArrayList<>();
            lists.add("reset");
            lists.add("result");
            lists.add("pull");
            if (config.getConfig().getBoolean("voteopen")){
                lists.add("off");
            }
            else if(!config.getConfig().getBoolean("voteopen")){
                lists.add("on");
            }
            return lists;
        }
        return null;
    }
}