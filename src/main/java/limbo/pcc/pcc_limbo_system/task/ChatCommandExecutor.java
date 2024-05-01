package limbo.pcc.pcc_limbo_system.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import limbo.pcc.pcc_limbo_system.Pcc_limbo_system;
import okhttp3.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ChatCommandExecutor implements CommandExecutor {
    Plugin config = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
    private final OkHttpClient httpClient;
    private final String apiEndpoint = config.getConfig().getString("ip");
    private final String defaultUid = "console";
    Plugin plugin = Pcc_limbo_system.getPlugin(Pcc_limbo_system.class);

    public ChatCommandExecutor() {
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(40, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS) // 写入超时时间
                .build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("pcc.command.chat")){
            sender.sendMessage(ChatColor.RED+"你没有使用该功能的权限");
            return false;
        }
        sender.sendMessage(ChatColor.YELLOW+"等待后端服务器回应，请稍等");
        String uid = defaultUid;
        String username = "console";
        if (sender instanceof Player) {
            uid = ((Player) sender).getUniqueId().toString();
            username = sender.getName();
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "使用方法：/chat [你要说的话]");
            return false;
        }
        String message = String.join(" ", args);

        // 构造 HTTP 请求
        MediaType mediaType = MediaType.parse("application/json");
        String requestBody = "{\"prompt\": \"" + message + "\", \"username\": \"" + username + "\",\"uid\": \"" + uid + "\"}";
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(body)
                .build();

        // 在新线程中发送请求并获取响应
        new Thread(() -> {
            Response response;
            try {
                Call call = httpClient.newCall(request);
                response = call.execute();
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "与聊天机器人通信时发生错误！");
                return;
            }

            // 解析响应
            JsonObject jsonResponse;
            try {
                String responseBody = Objects.requireNonNull(response.body()).string();
                jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "解析聊天机器人响应时发生错误！");
                return;
            } catch (com.google.gson.JsonSyntaxException e) {
                sender.sendMessage(ChatColor.RED + "聊天机器人响应的JSON格式不正确！");
                return;
            }

            // 在主线程中发送响应消息
            String responseText;
            try {
                responseText = jsonResponse.get("message").getAsString();
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "从聊天机器人响应中提取消息时出错！");
                return;
            }
            plugin.getServer().getLogger().info(ChatColor.GREEN + "[聊天机器人]" + ChatColor.WHITE + responseText);
            sender.sendMessage(ChatColor.GREEN +"[聊天机器人]" + ChatColor.WHITE + responseText);
        }).start();

        return true;
    }
}
