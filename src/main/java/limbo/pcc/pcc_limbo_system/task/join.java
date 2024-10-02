package limbo.pcc.pcc_limbo_system.task;

import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class join implements Listener {
    Plugin config = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent join) {
        if (config.getConfig().getString("joinmode").equals("info")) {
            List<String> list = config.getConfig().getStringList("join");
            for (String i : list) {
                Bukkit.getServer().broadcastMessage(i);
            }
        } else if (config.getConfig().getString("joinmode").equals("vote")) {
            List<String> resourselist = config.getConfig().getStringList("voteresourse");
            List<String> list = config.getConfig().getStringList("vote");
            String title = config.getConfig().getString("votetitle");
            Bukkit.getServer().broadcastMessage(ChatColor.GREEN+title+"（点击下面选项投票,基岩版/vote <选项>）");
            for (int i = 0; i < list.size(); i++) {
                TextComponent message = new TextComponent(ChatColor.GREEN + list.get(i) + ".");
                TextComponent exa = new TextComponent(resourselist.get(i));
                message.addExtra(exa);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + list.get(i)));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "投票给" + list.get(i))));
                Bukkit.getServer().spigot().broadcast(message);
            }
        }else{
            config.getLogger().warning("config中的joinmode填写错误！");
        }
    }

}