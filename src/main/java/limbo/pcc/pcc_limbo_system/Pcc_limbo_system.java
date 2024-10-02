package limbo.pcc.pcc_limbo_system;

import limbo.pcc.pcc_limbo_system.command.*;
import limbo.pcc.pcc_limbo_system.sign_in.sign_in;
import limbo.pcc.pcc_limbo_system.sign_in.sign_in_week;
import limbo.pcc.pcc_limbo_system.task.question.answer;
import limbo.pcc.pcc_limbo_system.task.question.question;
import limbo.pcc.pcc_limbo_system.task.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.List;


public final class Pcc_limbo_system extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        int pluginId = 17758; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("[pcc插件]加载完毕");
        getCommand("pcc").setExecutor(new main());
        getCommand("gonggao").setExecutor(new gong_gao());
        getCommand("shout").setExecutor(new shout());
        getCommand("shout").setTabCompleter(new shout());
        getCommand("stopp").setExecutor(new stop());
        getCommand("limbo").setExecutor(new limbo());
        getCommand("limbo").setTabCompleter(new limbo());
        getCommand("vote").setExecutor(new vote());
        getCommand("vote").setTabCompleter(new vote());
        getCommand("question").setExecutor(new question());
        getCommand("answer").setExecutor(new answer());
        getCommand("chat").setExecutor(new ChatCommandExecutor());
        saveDefaultConfig();
        this.saveResource("question.yml", false);
        FileConfiguration config = getConfig();
        //检查是否存在hide_when_no_player配置
        if(config.get("hide_when_no_player")==null){
            config.set("hide_when_no_player",true);
            saveConfig();
            getLogger().warning("未检测到hide_when_no_player配置选项，已自动添加");
        }
        boolean join = true;
        boolean times = true;
        if (config.getBoolean("joinopen")) {
            getServer().getPluginManager().registerEvents(new join(), this);
            getLogger().info("已启用公告模式中的join模式");
            join = false;
        }
        if (config.getBoolean("timeropen")) {
            int time = config.getInt("timer");
            time = time * 1200;
            BukkitTask close = new timer().runTaskTimer(this, 0, time);
            getLogger().info("已启用公告模式中的timer模式");
            times = false;
        }
        if(join && times){
            metrics.addCustomChart(new Metrics.SimplePie("announce", () -> "off"));
        }else if(!join && !times){
            metrics.addCustomChart(new Metrics.SimplePie("announce", () -> "both"));
        }else if(join){
            metrics.addCustomChart(new Metrics.SimplePie("announce", () -> "timer"));
        }else{
            metrics.addCustomChart(new Metrics.SimplePie("announce", () -> "join"));
        }
        if (config.getBoolean("voteopen")) {
            getLogger().info("正在启用投票");
            this.saveResource("voteuser.yml", false);
            this.saveResource("vote_result.yml", false);

            File ccconfig = new File(Pcc_limbo_system.getPlugin(Pcc_limbo_system.class).getDataFolder(), "vote_result.yml");
            FileConfiguration vote_result = YamlConfiguration.loadConfiguration(ccconfig);

            List<String> list = config.getStringList("vote");
            if (list.size() == 0) {
                System.out.println("[pcc]投票列表为空！");
            } else {
                for (String i : list) {
                    if (vote_result.getString(i) == null) {
                        vote_result.set(i, 0);
                        vote_result.set(i + "百分比", 0 + "%");
                        getLogger().info("检测到配置文件中不存在" + i + ",正在添加");
                    } else {
                        getLogger().info("已存在" + i);
                    }
                }
                if (vote_result.getString("总数") == null) {
                    vote_result.set("总数", 0);
                }
                try {
                    vote_result.save(ccconfig);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            metrics.addCustomChart(new Metrics.SimplePie("vote", () -> "true"));
        } else {
            metrics.addCustomChart(new Metrics.SimplePie("vote", () -> "false"));
        }
        if (config.getBoolean("signin")) {
            this.saveResource("sign.yml", false);
            this.saveResource("sign_objects.yml", false);
            this.saveResource("week.yml", false);
            getCommand("signin").setExecutor(new sign_in());
            getCommand("signin_week").setExecutor(new sign_in_week());
            metrics.addCustomChart(new Metrics.SimplePie("signin", () -> "true"));
        } else {
            getLogger().warning("未开启签到功能");
            metrics.addCustomChart(new Metrics.SimplePie("signin", () -> "false"));
        }
        getLogger().info("加载完成");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("卸载完毕");
    }
}