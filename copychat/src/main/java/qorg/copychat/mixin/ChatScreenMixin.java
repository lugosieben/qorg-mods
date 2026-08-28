package qorg.copychat.mixin;

import org.spongepowered.asm.mixin.Unique;
import qorg.copychat.chat.ChatCopyState;
import qorg.copychat.chat.CopyButtonHitbox;
import qorg.copychat.chat.ChatMessageFormatter;
import qorg.copychat.config.CopyChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	@Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("copychat");
	@Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
	private void copychat$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		try {
			if (!CopyChatConfig.SHOW_COPY_BUTTON.get()) return;
			int copyMouseButton = CopyChatConfig.USE_RIGHT_CLICK_TO_COPY.get() ? 1 : 0;
			if (event.button() != copyMouseButton) return;
			Minecraft minecraft = Minecraft.getInstance();
			if (CopyChatConfig.REQUIRE_SHIFT_TO_COPY.get() && !minecraft.hasShiftDown()) return;
			ChatCopyState.RenderSnapshot snapshot = ChatCopyState.renderSnapshot();
			if (snapshot == null) return;
			CopyButtonHitbox hitbox = snapshot.copyButton();

			Vector2f localMouse = new Vector2f();
			snapshot.inversePose().transformPosition((float) event.x(), (float) event.y(), localMouse);

			if (hitbox != null && hitbox.contains(localMouse.x(), localMouse.y())) {
				try {
					String text = ChatMessageFormatter.forClipboard(hitbox.message());
					minecraft.keyboardHandler.setClipboard(text);
					ChatCopyState.setCopyResult(hitbox.message(), true);
				} catch (Exception e) {
					LOGGER.error("Failed to copy chat message", e);
					ChatCopyState.setCopyResult(hitbox.message(), false);
				}
				cir.setReturnValue(true);
			}
			else if (CopyChatConfig.CLICK_ANYWHERE_TO_COPY.get()) {
				// The render snapshot only has a hitbox for the currently hovered message.
				// Reuse that hitbox's row bounds while allowing clicks anywhere in the row.
				if (hitbox != null && localMouse.y() >= hitbox.top() && localMouse.y() < hitbox.bottom()
						&& localMouse.x() >= 0 && localMouse.x() < snapshot.chatBoxRight()) {
					try {
						String text = ChatMessageFormatter.forClipboard(hitbox.message());
						minecraft.keyboardHandler.setClipboard(text);
						ChatCopyState.setCopyResult(hitbox.message(), true);
					} catch (Exception e) {
						LOGGER.error("Failed to copy chat message", e);
						ChatCopyState.setCopyResult(hitbox.message(), false);
					}
					cir.setReturnValue(true);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to handle copy chat click", e);
		}
	}
}
