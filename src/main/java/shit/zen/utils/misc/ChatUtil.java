package shit.zen.utils.misc;

import lombok.Generated;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import shit.zen.ClientBase;

public final class ChatUtil
extends ClientBase {
    public static void addMessage(Component component) {
        ChatComponent chatComponent = mc.gui.getChat();
        chatComponent.addMessage(component);
    }

    public static void print(String string) {
        ChatUtil.print(true, string);
    }

    public static void print(boolean bl, String string) {
        ChatUtil.addMessage(Component.nullToEmpty((bl ? "§7[§b§7] " : "") + string));
    }

    @Generated
    private ChatUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}