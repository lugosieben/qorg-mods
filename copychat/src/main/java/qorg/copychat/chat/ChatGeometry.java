package qorg.copychat.chat;

public record ChatGeometry(int chatWidth, int chatBottom, int entryHeight, int entryBottomToMessageY) {
	public static final int CHAT_BACKGROUND_RIGHT_INSET = 4;

	public static ChatGeometry fromRenderState(int rawChatWidth, float scale, int guiHeight, float lineSpacing) {
		return new ChatGeometry(
				(int) Math.ceil(rawChatWidth / scale),
				(int) Math.floor((guiHeight - 40) / scale),
				(int) (9 * (1 + lineSpacing)),
				(int) Math.round(8.0 + 4.0 * lineSpacing)
		);
	}

	public int messageY(int lineIndex) {
		return chatBottom - entryHeight * lineIndex - entryBottomToMessageY;
	}

	public int chatBoxRight() {
		return chatWidth + CHAT_BACKGROUND_RIGHT_INSET;
	}
}
