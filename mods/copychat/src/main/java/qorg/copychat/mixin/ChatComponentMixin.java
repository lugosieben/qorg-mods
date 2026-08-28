package qorg.copychat.mixin;

import qorg.copychat.chat.ChatCopyState;
import qorg.copychat.chat.ChatGeometry;
import qorg.copychat.chat.ChatHoverResolver;
import qorg.copychat.chat.CopyButtonHitbox;
import qorg.copychat.chat.HoveredMessage;
import qorg.copychat.config.CopyChatConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
	@Shadow @Final private net.minecraft.client.Minecraft minecraft;
	@Shadow @Final private List<GuiMessage.Line> trimmedMessages;
	@Shadow private int chatScrollbarPos;

	@Shadow
	private int getWidth() {
		return 0;
	}

	@Shadow
	private double getScale() {
		return 0;
	}

	@Shadow
	public abstract int getLinesPerPage();

	@Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;", shift = At.Shift.BEFORE))
	private void copychat$renderCopyButton(GuiGraphicsExtractor graphics, Font font, int ticks, int mouseX, int mouseY, ChatComponent.DisplayMode displayMode, boolean changeCursorOnInsertions, CallbackInfo ci) {
		if (!displayMode.foreground || !CopyChatConfig.SHOW_COPY_BUTTON.get()) return;

		float scale = (float) getScale();
		float rawLineSpacing = minecraft.options.chatLineSpacing().get().floatValue();
		ChatGeometry geo = ChatGeometry.fromRenderState(getWidth(), scale, graphics.guiHeight(), rawLineSpacing);

		Matrix3x2f inverse = new Matrix3x2f();
		graphics.pose().invert(inverse);
		Vector2f localMouse = new Vector2f();
		inverse.transformPosition((float) mouseX, (float) mouseY, localMouse);

		HoveredMessage hover = ChatHoverResolver.find(trimmedMessages, chatScrollbarPos, getLinesPerPage(), geo, localMouse);
		if (hover == null) {
			HoveredMessage messageAtCursor = ChatHoverResolver.findAtY(trimmedMessages, chatScrollbarPos, getLinesPerPage(), geo, localMouse.y());
			if (messageAtCursor != null) {
				CopyButtonHitbox hitbox = CopyButtonHitbox.forMessage(geo, messageAtCursor, font.width(ChatCopyState.COPY_SYMBOL), font.lineHeight);
				if (hitbox.contains(localMouse.x(), localMouse.y())) hover = messageAtCursor;
			}
		}

		if (hover != null) {
			CopyButtonHitbox hitbox = CopyButtonHitbox.forMessage(geo, hover, font.width(ChatCopyState.COPY_SYMBOL), font.lineHeight);
			ChatCopyState.capture(inverse, hitbox, geo.chatBoxRight());
			String symbol = ChatCopyState.symbolFor(hover.message(), CopyChatConfig.SHOW_COPY_FEEDBACK.get());
			graphics.text(font, symbol, hitbox.textX(font.width(symbol)), hitbox.top(), 0xFFFFFFFF);
		} else {
			ChatCopyState.capture(inverse, null, geo.chatBoxRight());
			ChatCopyState.clearFeedback();
		}
	}
}
