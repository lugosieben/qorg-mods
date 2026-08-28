package qorg.copychat.chat;

import net.minecraft.client.multiplayer.chat.GuiMessage;

public record CopyButtonHitbox(GuiMessage message, int left, int top, int right, int bottom) {
	private static final int HORIZONTAL_PADDING = 4;

	public static CopyButtonHitbox forMessage(ChatGeometry geometry, HoveredMessage message, int glyphWidth, int glyphHeight) {
		int glyphRight = geometry.chatBoxRight();
		return new CopyButtonHitbox(
				message.message(),
				glyphRight - glyphWidth - HORIZONTAL_PADDING,
				message.messageY(),
				glyphRight + HORIZONTAL_PADDING,
				message.messageY() + glyphHeight
		);
	}

	public boolean contains(float x, float y) {
		return x >= left && x < right && y >= top && y < bottom;
	}

	public int textX(int glyphWidth) {
		return right - HORIZONTAL_PADDING - glyphWidth;
	}
}
