package limbo.pcc.pcc_limbo_system.task.question;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class question implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pcc.command.config")) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "您没有足够的权限来执行此命令");
            return false;
        }
        File config = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "question.yml");
        FileConfiguration question = YamlConfiguration.loadConfiguration(config);
        File user = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "user.yml");
        FileConfiguration users = YamlConfiguration.loadConfiguration(user);
        Plugin configs = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
        if (args.length >= 1) {
            if (Objects.equals(args[0], "off")) {
                configs.getConfig().set("answer", false);
                sender.sendMessage(ChatColor.GREEN + "修改成功");
                configs.saveConfig();
                configs.reloadConfig();
                return false;
            } else if (Objects.equals(args[0], "reset")) {
                if (user.exists()) {
                    boolean deleted = user.delete();
                    if (deleted) {
                        sender.sendMessage(ChatColor.GREEN + "配置文件删除成功！");
                    } else {
                        sender.sendMessage(ChatColor.RED + "无法删除配置文件！");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "配置文件不存在！");
                }
                return false;
            }else {
                sender.sendMessage(ChatColor.RED+"该指令不存在");
                return false;
            }
        }
        List<String> list = question.getStringList("things");
        configs.getConfig().set("answer", true);
        configs.saveConfig();
        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + question.getString("question"));
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                TextComponent message = new TextComponent(ChatColor.GREEN + list.get(i) + ".");
                TextComponent exa = new TextComponent(list.get(i + 1));
                message.addExtra(exa);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/answer " + list.get(i)));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "选择答案为" + list.get(i))));
                Bukkit.getServer().spigot().broadcast(message);
            }
        }
        Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "答对奖励：" + question.getString("give") + "*" + question.getInt("count"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> list= new ArrayList<>();
        if(strings.length==1){
            list.add("reset");
            list.add("off");
            return list;
        }
        return null;
    }
}
