package qorg.copychat.chat;

import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.joml.Matrix3x2f;

public final class ChatCopyState {
	public static final String COPY_SYMBOL = "⧉";
	public static final String SUCCESS_SYMBOL = "✔";
	public static final String ERROR_SYMBOL = "✗";

	private static RenderSnapshot renderSnapshot;
	private static GuiMessage feedbackMessage;
	private static boolean feedbackSucceeded;

	private ChatCopyState() {
	}

	public static void capture(Matrix3x2f inversePose, CopyButtonHitbox copyButton, int chatBoxRight) {
		renderSnapshot = new RenderSnapshot(new Matrix3x2f(inversePose), copyButton, chatBoxRight);
	}

	public static RenderSnapshot renderSnapshot() {
		return renderSnapshot;
	}

	public static String symbolFor(GuiMessage message, boolean showFeedback) {
		if (!showFeedback) {
			clearFeedback();
			return COPY_SYMBOL;
		}
		if (message != feedbackMessage) {
			feedbackMessage = null;
			return COPY_SYMBOL;
		}
		return feedbackSucceeded ? SUCCESS_SYMBOL : ERROR_SYMBOL;
	}

	public static void clearFeedback() {
		feedbackMessage = null;
	}

	public static void setCopyResult(GuiMessage message, boolean succeeded) {
		feedbackMessage = message;
		feedbackSucceeded = succeeded;
	}

	public record RenderSnapshot(Matrix3x2f inversePose, CopyButtonHitbox copyButton, int chatBoxRight) {
	}
}
