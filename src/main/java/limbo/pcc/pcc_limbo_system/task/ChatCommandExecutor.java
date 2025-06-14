package limbo.pcc.pcc_limbo_system.task;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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

    private final Map<String, List<JsonObject>> conversationHistories = new HashMap<>();

    public ChatCommandExecutor() {
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(40, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Plugin config = Pcc_limbo_system.getProvidingPlugin(Pcc_limbo_system.class);
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
        String modelChoice = "gpt-4o";

        if (sender instanceof Player) {
            uid = ((Player) sender).getUniqueId().toString();
        } else {
            uid = defaultUid;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            conversationHistories.remove(uid);
            sender.sendMessage(ChatColor.GREEN + "你的会话已被清除。");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "等待后端服务器回应，请稍等");

        String message = String.join(" ", args);
        if (args.length > 1) {
            String lastArg = args[args.length - 1];
            if (lastArg.equalsIgnoreCase("gpt-4o") || lastArg.equalsIgnoreCase("gpt-4o-mini")) {
                modelChoice = lastArg;
                message = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
            }
        }

        List<JsonObject> conversation = conversationHistories.getOrDefault(uid, new ArrayList<>());

        if (conversation.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content",config.getConfig().getString("prompt"));
            conversation.add(systemMessage);
        }

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversation.add(userMessage);

        if (conversation.size() > 20) {
            conversation = conversation.subList(conversation.size() - 20, conversation.size());
        }

        conversationHistories.put(uid, conversation);

        MediaType mediaType = MediaType.parse("application/json");
        JsonObject jsonBody = new JsonObject();
        jsonBody.add("messages", new Gson().toJsonTree(conversation).getAsJsonArray());
        jsonBody.addProperty("modelChoice", modelChoice);

        RequestBody body = RequestBody.create(jsonBody.toString(), mediaType);
        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(body)
                .build();

        List<JsonObject> finalConversation = conversation;
        new Thread(() -> {
            Response response;
            try {
                Call call = httpClient.newCall(request);
                response = call.execute();
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "与聊天机器人通信时发生网络错误！");
                e.printStackTrace();
                return;
            }

            String responseBody;
            try {
                responseBody = Objects.requireNonNull(response.body()).string();
                config.getLogger().info("Response Body: " + responseBody);
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "读取聊天机器人响应时发生错误！");
                e.printStackTrace();
                return;
            }

            JsonObject jsonResponse;
            try {
                jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
            } catch (JsonSyntaxException e) {
                sender.sendMessage(ChatColor.RED + "解析聊天机器人响应时发生错误：无效的JSON格式！");
                e.printStackTrace();
                return;
            }

            if (jsonResponse.has("error")) {
                String errorMessage = jsonResponse.get("error").getAsString();
                sender.sendMessage(ChatColor.RED + "服务器错误：" + errorMessage);
                config.getLogger().warning("Server error: " + errorMessage);
                return;
            }

            String responseText;
            JsonElement replyElement = jsonResponse.get("reply");
            if (replyElement != null && !replyElement.isJsonNull()) {
                responseText = replyElement.getAsString();
            } else {
                sender.sendMessage(ChatColor.RED + "聊天机器人响应中缺少 'reply' 字段！");
                config.getLogger().warning("Missing 'reply' field in response: " + responseBody);
                return;
            }

            JsonObject aiMessage = new JsonObject();
            aiMessage.addProperty("role", "assistant");
            aiMessage.addProperty("content", responseText);
            finalConversation.add(aiMessage);

            conversationHistories.put(uid, finalConversation);

            sender.sendMessage(ChatColor.GREEN + "聊天机器人: ");
            sender.spigot().sendMessage(MarkdownRenderer.renderMarkdown(responseText));
            config.getLogger().info(responseText);

            if (responseText.contains("gpt-4o-mini")) {
                sender.sendMessage(ChatColor.YELLOW + "[注意]: 模型已切换到 gpt-4o-mini 由于 gpt-4o 达到限额");
                config.getLogger().info("[注意]: 模型已切换到 gpt-4o-mini 由于 gpt-4o 达到限额");
            }
        }).start();

        return true;
    }
}
