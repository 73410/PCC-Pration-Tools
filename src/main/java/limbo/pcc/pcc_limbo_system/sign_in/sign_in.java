package limbo.pcc.pcc_limbo_system.sign_in;

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
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class sign_in implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("pcc.signin")){
            sender.sendMessage(ChatColor.RED+"你没有足够权限使用此功能");
            return false;
        }
        File config_file = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "sign.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(config_file);

        File object = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "sign_objects.yml");
        FileConfiguration configs = YamlConfiguration.loadConfiguration(object);

        File week = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "week.yml");
        FileConfiguration week_set = YamlConfiguration.loadConfiguration(week);

        File lastSignFile = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "last_sign.yml");
        FileConfiguration lastSignConfig = YamlConfiguration.loadConfiguration(lastSignFile);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String Date = sdf.format(new Date());
        List<String> list = config.getStringList(Date);

        if (list.contains(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "你已经签过到了");
            return true;
        } else {
            list.add(sender.getName());
            config.set(Date, list);

            String name = sender.getName();
            List<String> continue_ = week_set.getStringList(name);
            String a = "";
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch(dayOfWeek) {
                case Calendar.SUNDAY:
                    a = "星期日";
                    break;
                case Calendar.MONDAY:
                    a = "星期一";
                    break;
                case Calendar.TUESDAY:
                    a = "星期二";
                    break;
                case Calendar.WEDNESDAY:
                    a = "星期三";
                    break;
                case Calendar.THURSDAY:
                    a = "星期四";
                    break;
                case Calendar.FRIDAY:
                    a = "星期五";
                    break;
                case Calendar.SATURDAY:
                    a = "星期六";
                    break;
            }

            String lastSignDateStr = lastSignConfig.getString(name);
            if (lastSignDateStr != null) {
                try {
                    Date lastSignDate = sdf.parse(lastSignDateStr);
                    Calendar lastSignCalendar = Calendar.getInstance();
                    lastSignCalendar.setTime(lastSignDate);
                    // Check if last sign-in and current sign-in are in the same week
                    if (!(lastSignCalendar.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR)
                            && lastSignCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))) {
                        continue_.clear();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            lastSignConfig.set(name, Date);
            try {
                lastSignConfig.save(lastSignFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            continue_.add(a);
            week_set.set(name,continue_);

            if(continue_.size()==7){
                sender.sendMessage(ChatColor.AQUA+"您已经连续七天签到，去领取连续签到到奖励吧");
                sender.sendMessage(ChatColor.YELLOW+"使用/signin_week领取");
            }
            try {
                config.save(config_file);
                week_set.save(week);
                sender.sendMessage(ChatColor.GREEN + "签到成功");
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "签到失败，请联系管理员处理");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "正在发送奖励...");
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<String> objects_list = configs.getStringList(Date);

                if (!objects_list.isEmpty()) {
                    // 列表不为空，可以安全地访问元素
                    String itemMaterial = objects_list.get(0);
                    int count = Integer.parseInt(objects_list.get(1));

                    Material material = Material.getMaterial(itemMaterial);
                    if (material != null) {
                        ItemStack item = new ItemStack(material, count);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(ChatColor.YELLOW +Date+ChatColor.GOLD+"签到奖励");
                        item.setItemMeta(meta);
                        player.getInventory().addItem(item);
                        player.sendMessage(ChatColor.GREEN + "给予物品成功！" + itemMaterial);
                    } else {
                        player.sendMessage(ChatColor.RED + "无法给予物品，因为 " + itemMaterial + " 不是一个有效的物品类型。");
                    }

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
                    configs.set(Date, newlist);

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
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(ChatColor.YELLOW +Date+ChatColor.GOLD+"签到奖励");
                        item.setItemMeta(meta);
                        player.getInventory().addItem(item);
                        player.sendMessage(ChatColor.GREEN + "给予物品成功！" + itemMaterial);
                    } else {
                        player.sendMessage(ChatColor.RED + "无法给予物品，因为 " + itemMaterial + " 不是一个有效的物品类型。");
                    }
                }

                int num = list.size();
                Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "今日已有" + ChatColor.YELLOW + num + ChatColor.GREEN + "人签到，没签到的赶紧签到吧！");
                Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "提示：使用/signin来签到");
            } else {
                sender.sendMessage(ChatColor.RED + "只有玩家可以执行此命令。");
            }
        }
        return true;
    }
}
