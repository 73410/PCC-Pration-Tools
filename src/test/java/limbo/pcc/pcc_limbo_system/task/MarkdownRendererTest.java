package limbo.pcc.pcc_limbo_system.task;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.Test;
import static org.junit.Assert.*;

public class MarkdownRendererTest {
    @Test
    public void testBoldRendering() {
        BaseComponent[] result = MarkdownRenderer.renderMarkdown("**text**");
        assertEquals("Expected one root component", 1, result.length);
        assertTrue("Root component should be TextComponent", result[0] instanceof TextComponent);

        TextComponent container = (TextComponent) result[0];
        assertNotNull("Container extras should not be null", container.getExtra());
        assertFalse("Container should have at least one child", container.getExtra().isEmpty());

        // The first child is the processed line, which itself contains the formatted text
        assertTrue(container.getExtra().get(0) instanceof TextComponent);
        TextComponent line = (TextComponent) container.getExtra().get(0);
        assertNotNull(line.getExtra());
        assertTrue(line.getExtra().size() > 1);

        BaseComponent bold = line.getExtra().get(1);
        assertTrue("Child component should be bold", bold.isBold());
        assertEquals("text", bold.toPlainText());
    }
}
