package qorg.copychat.chat;

import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.joml.Vector2f;

import java.util.List;

public final class ChatHoverResolver {
	private static final int MESSAGE_HEIGHT = 9;

	private ChatHoverResolver() {
	}

	public static HoveredMessage find(List<GuiMessage.Line> lines, int scrollbarPosition, int linesPerPage, ChatGeometry geometry, Vector2f mouse) {
		if (mouse.x() < 0 || mouse.x() >= geometry.chatBoxRight()) return null;
		return findAtY(lines, scrollbarPosition, linesPerPage, geometry, mouse.y());
	}

	public static HoveredMessage findAtY(List<GuiMessage.Line> lines, int scrollbarPosition, int linesPerPage, ChatGeometry geometry, float mouseY) {
		int firstLine = Math.max(0, scrollbarPosition);
		if (firstLine >= lines.size()) return null;
		int visibleCount = Math.clamp(linesPerPage, 0, lines.size() - firstLine);
		boolean entryHovered = false;

		for (int index = visibleCount - 1; index >= 0; index--) {
			GuiMessage.Line line = lines.get(firstLine + index);
			int messageY = geometry.messageY(index);
			if (mouseY >= messageY && mouseY < messageY + MESSAGE_HEIGHT) entryHovered = true;
			if (line.endOfEntry() && entryHovered) return new HoveredMessage(line.parent(), messageY);
		}
		return null;
	}
}
