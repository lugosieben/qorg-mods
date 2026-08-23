package qorg.copychat.mixin;

import qorg.copychat.CopyChat;
import qorg.copychat.CopyChat.ChatGeometry;
import qorg.copychat.CopyChat.HoverResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	@Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
	private void copychat$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		try {
			if (event.button() != 0) return;
			if (CopyChat.lastPoseInverse == null) return;

			Minecraft minecraft = Minecraft.getInstance();
			ChatComponent chat = minecraft.gui.hud.getChat();
			ChatComponentAccessor chatAccessor = (ChatComponentAccessor) chat;
			List<GuiMessage.Line> trimmedMessages = chatAccessor.getTrimmedMessages();
			int chatScrollbarPos = chatAccessor.getChatScrollbarPos();

			Font font = minecraft.font;
			float scale = minecraft.options.chatScale().get().floatValue();
			int guiHeight = minecraft.getWindow().getGuiScaledHeight();
			float rawLineSpacing = minecraft.options.chatLineSpacing().get().floatValue();
			ChatGeometry geo = CopyChat.computeGeometry(ChatComponent.getWidth(minecraft.options.chatWidth().get()), scale, guiHeight, rawLineSpacing);

			Vector2f localMouse = new Vector2f();
			CopyChat.lastPoseInverse.transformPosition((float) event.x(), (float) event.y(), localMouse);

			HoverResult hover = CopyChat.findHoveredMessage(trimmedMessages, chatScrollbarPos, chat.getLinesPerPage(), geo, localMouse);

			if (hover != null && localMouse.x() >= geo.chatWidth() + 4 - font.width(CopyChat.COPY_SYMBOL) - CopyChat.COPY_BUTTON_PADDING
					&& localMouse.x() <= geo.chatWidth() + 16 + CopyChat.COPY_BUTTON_PADDING) {
				try {
					String text = hover.message().content().getString();
					minecraft.keyboardHandler.setClipboard(text);
					CopyChat.lastFeedbackMessage = hover.message();
					CopyChat.lastFeedbackSuccess = true;
				} catch (Exception e) {
					CopyChat.LOGGER.error("Failed to copy chat message", e);
					CopyChat.lastFeedbackMessage = hover.message();
					CopyChat.lastFeedbackSuccess = false;
				}
				cir.setReturnValue(true);
			}
		} catch (Exception e) {
			CopyChat.LOGGER.error("Failed to handle copy chat click", e);
		}
	}
}
