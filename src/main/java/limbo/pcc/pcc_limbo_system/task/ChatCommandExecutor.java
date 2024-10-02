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
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatCommandExecutor implements CommandExecutor {
    Plugin config = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
    private final OkHttpClient httpClient;
    private final String apiEndpoint = config.getConfig().getString("ip");
    private final String defaultUid = "console";

    // 为每个玩家维护对话历史
    private final Map<String, List<JsonObject>> conversationHistories = new HashMap<>();

    public ChatCommandExecutor() {
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(40, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS) // 写入超时时间
                .build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("pcc.command.chat")) {
            sender.sendMessage(ChatColor.RED + "你没有使用该功能的权限");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "使用方法：/chat [你要说的话] [模型选择 (可选: gpt-4o/gpt-4o-mini)]");
            sender.sendMessage(ChatColor.RED + "或者使用 /chat clear 来清除会话");
            return false;
        }

        String uid;
        String modelChoice = "gpt-4o"; // 默认选择gpt-4o

        if (sender instanceof Player) {
            uid = ((Player) sender).getUniqueId().toString();
        } else {
            uid = defaultUid;
        }

        // 检查是否是清除会话的命令
        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            // 清除玩家的对话历史
            conversationHistories.remove(uid);
            sender.sendMessage(ChatColor.GREEN + "你的会话已被清除。");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "等待后端服务器回应，请稍等");

        // 处理用户的消息
        String message = String.join(" ", args);
        if (args.length > 1) {
            String lastArg = args[args.length - 1];
            if (lastArg.equalsIgnoreCase("gpt-4o") || lastArg.equalsIgnoreCase("gpt-4o-mini")) {
                modelChoice = lastArg; // 如果用户提供了模型选择，则使用用户的选择
                message = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
            }
        }

        // 获取玩家的对话历史，没有则创建新的
        List<JsonObject> conversation = conversationHistories.getOrDefault(uid, new ArrayList<>());

        // 添加系统消息（如果是新会话）
        if (conversation.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "你是我的世界pcc服务器的助理机器人，你需要表明你是什么模型。接下来会给你一些数据以便于你解答玩家问题。数据：1.我们服务器的服主是chen_xigua,管理员团队有：brockh090、Vex、Kehuan1、XINGKONGLZC。2.我们服务器插件列表：CatSeedLogin, CMILib, CoreProtect, CustomCrops, dynmap, DynMap_Residence, EClean, Econoblocks, Essentials, GSit,HeadDatabase, ItemsAdder, LoneLibs, Multiverse-Core, Newkit, NoMoreCooked, Pcc_limbo_system, Permission, PlayerTitle, PlugManX,ProtocolLib, qsaddon-dynmap, QuickShop-Hikari, Residence, shop, Vault, ViaBackwards, ViaVersion。3.我们服务器十一活动正在进行，详细见公告");
            conversation.add(systemMessage);
        }

        // 添加用户消息到对话历史
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversation.add(userMessage);

        // 控制对话历史长度，保留最近的 20 条消息
        if (conversation.size() > 20) {
            conversation = conversation.subList(conversation.size() - 20, conversation.size());
        }

        // 将更新后的对话历史保存回 Map
        conversationHistories.put(uid, conversation);

        // 构造HTTP请求的JSON内容
        MediaType mediaType = MediaType.parse("application/json");
        JsonObject jsonBody = new JsonObject();
        jsonBody.add("messages", new Gson().toJsonTree(conversation).getAsJsonArray());
        jsonBody.addProperty("modelChoice", modelChoice);

        RequestBody body = RequestBody.create(jsonBody.toString(), mediaType);
        Request request = new Request.Builder()
                .url(apiEndpoint)  // 后端 API 的 URL
                .post(body)
                .build();

        // 在新线程中发送请求并获取响应
        List<JsonObject> finalConversation = conversation;
        new Thread(() -> {
            Response response;
            try {
                Call call = httpClient.newCall(request);
                response = call.execute();
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "与聊天机器人通信时发生错误！");
                e.printStackTrace();
                return;
            }

            // 解析响应
            JsonObject jsonResponse;
            try {
                String responseBody = Objects.requireNonNull(response.body()).string();
                jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "解析聊天机器人响应时发生错误！");
                e.printStackTrace();
                return;
            }

            String responseText;
            try {
                responseText = jsonResponse.get("reply").getAsString();
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "从聊天机器人响应中提取消息时出错！");
                e.printStackTrace();
                return;
            }

            // 添加 AI 回复到对话历史
            JsonObject aiMessage = new JsonObject();
            aiMessage.addProperty("role", "assistant");
            aiMessage.addProperty("content", responseText);
            finalConversation.add(aiMessage);

            // 更新对话历史 Map
            conversationHistories.put(uid, finalConversation);

            // 发送AI回复到玩家
            sender.sendMessage(ChatColor.GREEN + "[聊天机器人] " + ChatColor.WHITE + responseText);
            config.getLogger().info(responseText);
            // 检查是否有模型切换提示
            if (responseText.contains("gpt-4o-mini")) {
                sender.sendMessage(ChatColor.YELLOW + "[注意]: 模型已切换到 gpt-4o-mini 由于 gpt-4o 达到限额");
                config.getLogger().info("[注意]: 模型已切换到 gpt-4o-mini 由于 gpt-4o 达到限额");
            }
        }).start();

        return true;
    }
}
