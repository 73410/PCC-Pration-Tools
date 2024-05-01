package limbo.pcc.pcc_limbo_system.task.question;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class answer implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        File config = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "user.yml");
        FileConfiguration answer = YamlConfiguration.loadConfiguration(config);
        File configs = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "question.yml");
        FileConfiguration question = YamlConfiguration.loadConfiguration(configs);
        Plugin configgg = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
        if(!configgg.getConfig().getBoolean("answer")){
            commandSender.sendMessage(ChatColor.RED+"还没开启答题");
            return false;
        }
        if(answer.getString(commandSender.getName())!=null){
            commandSender.sendMessage(ChatColor.RED+"你已经答过题了！");
            return false;
        }
        if(Objects.equals(strings[0],question.getString("key"))){
            commandSender.sendMessage(ChatColor.GREEN+"恭喜你答对了！");
            commandSender.sendMessage(ChatColor.YELLOW+"正在发送奖励...");
            Player player = Bukkit.getPlayer(commandSender.getName());
            String itemMaterial = question.getString("give");
            if (itemMaterial != null) {
                ItemStack item = new ItemStack(Material.getMaterial(itemMaterial),question.getInt("count"));
                player.getInventory().addItem(item);
                player.sendMessage(ChatColor.GREEN+"给予物品成功！");
            }else {
                player.sendMessage(ChatColor.RED+"配置文件中未指定要给予的物品！");
            }
        }else {
            commandSender.sendMessage(ChatColor.RED+"很遗憾，回答错误！");
        }
        answer.set(commandSender.getName(),strings[0]);
        try {
            answer.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
