package qorg.copychat.chat;

import net.minecraft.client.multiplayer.chat.GuiMessage;

public record HoveredMessage(GuiMessage message, int messageY) {
}
