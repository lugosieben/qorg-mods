package qorg.copychat;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CopyChat implements ModInitializer {
	public static final String MOD_ID = "copychat";
	public static final String COPY_SYMBOL = "⧉";
	public static final String SUCCESS_SYMBOL = "✔";
	public static final String ERROR_SYMBOL = "✗";
	public static final int COPY_BUTTON_PADDING = 4;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Matrix3x2f lastPoseInverse;
	public static GuiMessage lastFeedbackMessage;
	public static boolean lastFeedbackSuccess;

	@Override
	public void onInitialize() {
		LOGGER.info("CopyChat initialized!");
	}

	public record ChatGeometry(int chatWidth, int chatBottom, int entryHeight, int entryBottomToMessageY) {
		public int messageY(int lineIndex) {
			return chatBottom - entryHeight * lineIndex - entryBottomToMessageY;
		}
	}

	public static ChatGeometry computeGeometry(int rawChatWidth, float scale, int guiHeight, float rawLineSpacing) {
		int chatWidth = (int) Math.ceil(rawChatWidth / scale);
		int chatBottom = (int) Math.floor((guiHeight - 40) / scale);
		int entryHeight = (int) (9 * (1 + rawLineSpacing));
		int entryBottomToMessageY = (int) Math.round(8.0 + 4.0 * rawLineSpacing);
		return new ChatGeometry(chatWidth, chatBottom, entryHeight, entryBottomToMessageY);
	}

	public record HoverResult(GuiMessage message, int messageY) {}

	public static HoverResult findHoveredMessage(List<GuiMessage.Line> trimmedMessages, int chatScrollbarPos, int linesPerPage, ChatGeometry geo, Vector2f localMouse) {
		int visibleCount = Math.min(trimmedMessages.size() - chatScrollbarPos, linesPerPage);
		boolean hovered = false;

		for (int i = visibleCount - 1; i >= 0; i--) {
			int displayIndex = chatScrollbarPos + i;
			GuiMessage.Line line = trimmedMessages.get(displayIndex);
			int messageY = geo.messageY(i);

			if (localMouse.y() >= messageY && localMouse.y() <= messageY + 8 && localMouse.x() >= 0 && localMouse.x() <= geo.chatWidth()) {
				hovered = true;
			}

			if (line.endOfEntry()) {
				if (hovered) return new HoverResult(line.parent(), messageY);
            }
		}

		return null;
	}
}
