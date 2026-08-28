package qorg.copychat.chat;

import com.mojang.serialization.JsonOps;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.ComponentSerialization;
import qorg.copychat.config.CopyChatConfig;

public final class ChatMessageFormatter {
	private ChatMessageFormatter() {
	}

	public static String forClipboard(GuiMessage message) {
		if (!CopyChatConfig.COPY_AS_JSON.get()) {
			return message.content().getString();
		}
		return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, message.content()).getOrThrow().toString();
	}
}
