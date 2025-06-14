package limbo.pcc.pcc_limbo_system.task;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.regex.*;

public class MarkdownRenderer {

    // 正则表达式模式（移除代码块相关）
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)");
    private static final Pattern HR_PATTERN = Pattern.compile("^[-*_]{3,}\\s*$");
    private static final Pattern LIST_PATTERN = Pattern.compile("^( *)([*-]|\\d+\\.)\\s+(.*)");
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^>\\s*(.*)");
    private static final Pattern INLINE_PATTERN = Pattern.compile(
            "(\\*\\*|\\*|__|~~|\\|\\|)(.*?)\\1" +  // 格式标记
                    "|\\[([^\\]]+)\\]\\(([^\\)]+)\\)" +    // 链接/指令
                    "|!\\[([^\\]]*)\\]\\(([^\\)]+)\\)"    // 图片
    );

    public static BaseComponent[] renderMarkdown(String markdown) {
        TextComponent container = new TextComponent();
        String[] lines = markdown.split("\\r?\\n");

        for (String line : lines) {
            if (HR_PATTERN.matcher(line).matches()) {
                container.addExtra(createHorizontalRule());
            } else if (HEADER_PATTERN.matcher(line).find()) {
                Matcher m = HEADER_PATTERN.matcher(line);
                m.find();
                container.addExtra(createHeader(m.group(2), m.group(1).length()));
            } else if (BLOCKQUOTE_PATTERN.matcher(line).find()) {
                Matcher m = BLOCKQUOTE_PATTERN.matcher(line);
                m.find();
                container.addExtra(createBlockquote(m.group(1)));
            } else if (LIST_PATTERN.matcher(line).find()) {
                Matcher m = LIST_PATTERN.matcher(line);
                m.find();
                container.addExtra(createListItem(
                        m.group(3),
                        m.group(1).length() / 2,
                        m.group(2).matches("\\d+\\.")
                ));
            } else {
                container.addExtra(processInline(line));
            }
            container.addExtra(new TextComponent("\n"));
        }

        return new BaseComponent[]{ container };
    }


    private static BaseComponent createHorizontalRule() {
        TextComponent hr = new TextComponent("--------------------------------");
        hr.setColor(ChatColor.DARK_GRAY);
        hr.setStrikethrough(true);
        return hr;
    }

    private static BaseComponent createHeader(String content, int level) {
        TextComponent header = new TextComponent();
        header.addExtra(processInline(content));

        level = Math.min(6, Math.max(1, level));
        switch (level) {
            case 1:
                header.setColor(ChatColor.RED);
                header.setBold(true);
                header.setUnderlined(true);
                break;
            case 2:
                header.setColor(ChatColor.GOLD);
                header.setBold(true);
                break;
            case 3:
                header.setColor(ChatColor.YELLOW);
                break;
            case 4:
                header.setColor(ChatColor.GREEN);
                break;
            case 5:
                header.setColor(ChatColor.BLUE);
                break;
            case 6:
                header.setColor(ChatColor.DARK_PURPLE);
                header.setItalic(true);
                break;
        }
        return header;
    }

    private static BaseComponent createListItem(String content, int indentLevel, boolean ordered) {
        TextComponent item = new TextComponent();
        for (int i = 0; i < indentLevel; i++) item.addExtra("  ");

        TextComponent bullet = new TextComponent(ordered ? "• " : "◦ ");
        bullet.setColor(ChatColor.GRAY);
        item.addExtra(bullet);

        TextComponent text = processInline(content);
        text.setColor(ChatColor.WHITE);
        item.addExtra(text);
        return item;
    }

    private static BaseComponent createBlockquote(String text) {
        TextComponent quote = new TextComponent("│ ");
        quote.setColor(ChatColor.DARK_GREEN);
        quote.addExtra(processInline(text));
        return quote;
    }

    private static BaseComponent createCodeBlock(String content, String lang, boolean start) {
        ComponentBuilder builder = new ComponentBuilder();
        if (start) {
            builder.append("[代码" + (lang.isEmpty() ? "" : ":" + lang) + "]\n")
                    .color(ChatColor.DARK_GRAY);
        }
        builder.append(content).color(ChatColor.GRAY);
        if (!start) builder.append("\n");
        return new TextComponent(builder.create());
    }

    private static BaseComponent createCodeLine(String line) {
        return new TextComponent(new ComponentBuilder(line + "\n")
                .color(ChatColor.GRAY)
                .create());
    }

    private static TextComponent processInline(String text) {
        TextComponent component = new TextComponent();
        Matcher matcher = INLINE_PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            component.addExtra(new TextComponent(text.substring(lastIndex, matcher.start())));

            if (matcher.group(1) != null) {
                handleFormatting(component, matcher);
            } else if (matcher.group(3) != null) {
                handleLink(component, matcher);
            } else if (matcher.group(5) != null) {
                handleImage(component, matcher);
            }

            lastIndex = matcher.end();
        }
        component.addExtra(new TextComponent(text.substring(lastIndex)));
        return component;
    }

    private static void handleFormatting(TextComponent parent, Matcher matcher) {
        String style = matcher.group(1);
        String content = matcher.group(2);
        TextComponent tc = new TextComponent(content);

        switch (style) {
            case "**":
                tc.setBold(true);
                tc.setColor(ChatColor.RED);
                break;
            case "*":
                tc.setItalic(true);
                tc.setColor(ChatColor.GREEN);
                break;
            case "__":
                tc.setUnderlined(true);
                tc.setColor(ChatColor.BLUE);
                break;
            case "~~":
                tc.setStrikethrough(true);
                tc.setColor(ChatColor.GRAY);
                break;
            case "||":
                tc.setObfuscated(true);
                tc.setColor(ChatColor.BLACK);
                break;
        }
        parent.addExtra(tc);
    }

    private static void handleLink(TextComponent parent, Matcher matcher) {
        String text = matcher.group(3);
        String url = matcher.group(4);

        TextComponent link = new TextComponent(text);
        if (url.startsWith("/")) {
            handleCommandLink(link, url);
        } else {
            handleWebLink(link, url);
        }
        parent.addExtra(link);
    }

    private static void handleCommandLink(TextComponent link, String command) {
        link.setColor(ChatColor.DARK_PURPLE);
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        link.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder("执行命令：\n")
                        .append(command).color(ChatColor.YELLOW)
                        .create()
                )));
    }

    private static void handleWebLink(TextComponent link, String url) {
        link.setColor(ChatColor.YELLOW);
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                url.startsWith("http") ? url : "https://" + url));
        link.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text("打开链接：" + url)
        ));
    }

    private static void handleImage(TextComponent parent, Matcher matcher) {
        String alt = matcher.group(5);
        String url = matcher.group(6);

        TextComponent img = new TextComponent("📷 " + alt);
        img.setColor(ChatColor.AQUA);
        img.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        img.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text("查看图片：" + url)
        ));
        parent.addExtra(img);
    }
}