package limbo.pcc.pcc_limbo_system.sign_in;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class sign_in_week implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("pcc.signin_week")){
            sender.sendMessage(ChatColor.RED+"你没有足够权限使用此功能");
            return false;
        }
        File week = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "week.yml");
        FileConfiguration week_set = YamlConfiguration.loadConfiguration(week);

        File object = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "sign_objects.yml");
        FileConfiguration configs = YamlConfiguration.loadConfiguration(object);

        String name = sender.getName();
        List<String> list = week_set.getStringList(name);
        if (list.size() != 7) {
            sender.sendMessage(ChatColor.RED + "你未连续签到一星期签到(必须要从周一连续到周日)");
            sender.sendMessage(ChatColor.YELLOW + "如果想知道哪天没签到，可以联系管理");
            return false;
        }

        List<String> emptyList = new ArrayList<>();
        week_set.set(name, emptyList);
        try {
            week_set.save(week);
            sender.sendMessage(ChatColor.GREEN + "记录成功");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "记录时遇到问题，联系管理员处理");
            return false; // 如果保存失败，立即返回
        }

        //发送奖励
        sender.sendMessage(ChatColor.YELLOW + "正在发送奖励...");
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> objects_list = configs.getStringList("Sunday");

            if (!objects_list.isEmpty() && objects_list.size() >= 2) {
                // 列表不为空，可以安全地访问元素
                String itemMaterial = objects_list.get(0);
                int count;
                try {
                    count = Integer.parseInt(objects_list.get(1));
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "配置文件格式错误，联系管理员处理");
                    return false;
                }

                Material material = Material.getMaterial(itemMaterial);
                if (material != null) {
                    ItemStack item = new ItemStack(material, count);
                    player.getInventory().addItem(item);
                    player.sendMessage(ChatColor.GREEN + "给予物品成功！" + itemMaterial);
                } else {
                    player.sendMessage(ChatColor.RED + "无法给予物品，因为 " + itemMaterial + " 不是一个有效的物品类型。");
                }
                return true;
            } else {
                // 列表为空，处理这种情况

                sender.sendMessage(ChatColor.RED + "配置文件中未指定要给予的物品！正在随机创建");

                Material[] allMaterials = Material.values();
                List<Material> items = new ArrayList<>();
                for (Material material : allMaterials) {
                    if (material.isItem()) { // 仅选取可以作为物品的 Material
                        items.add(material);
                    }
                }
                Random random = new Random();
                Material randomItem = items.get(random.nextInt(items.size()));
                int randomNumber = random.nextInt(64) + 1;

                List<Object> newlist = new ArrayList<>();
                newlist.add(String.valueOf(randomItem));
                newlist.add(randomNumber);
                configs.set("Sunday", newlist);

                try {
                    configs.save(object);
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "操作失败");
                    return true;
                }

                String itemMaterial = String.valueOf(randomItem);
                Material material = Material.getMaterial(itemMaterial);
                if (material != null) {
                    ItemStack item = new ItemStack(material, randomNumber);
                    player.getInventory().addItem(item);
                    player.sendMessage(ChatColor.GREEN + "给予物品成功！" + itemMaterial);
                } else {
                    player.sendMessage(ChatColor.RED + "无法给予物品，因为 " + itemMaterial + " 不是一个有效的物品类型。");
                }
                }
            }
        return false;
    }
}
