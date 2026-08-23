package qorg.copychat.mixin;

import qorg.copychat.CopyChat;
import qorg.copychat.CopyChat.ChatGeometry;
import qorg.copychat.CopyChat.HoverResult;
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
		if (!displayMode.foreground) return;

		float scale = (float) getScale();
		float rawLineSpacing = minecraft.options.chatLineSpacing().get().floatValue();
		ChatGeometry geo = CopyChat.computeGeometry(getWidth(), scale, graphics.guiHeight(), rawLineSpacing);

		Matrix3x2f inverse = new Matrix3x2f();
		graphics.pose().invert(inverse);
		CopyChat.lastPoseInverse = new Matrix3x2f(inverse);
		Vector2f localMouse = new Vector2f();
		inverse.transformPosition((float) mouseX, (float) mouseY, localMouse);

		HoverResult hover = CopyChat.findHoveredMessage(trimmedMessages, chatScrollbarPos, getLinesPerPage(), geo, localMouse);

		if (hover != null) {
			String symbol;
			if (hover.message() == CopyChat.lastFeedbackMessage) {
				symbol = CopyChat.lastFeedbackSuccess ? CopyChat.SUCCESS_SYMBOL : CopyChat.ERROR_SYMBOL;
			} else {
				symbol = CopyChat.COPY_SYMBOL;
				CopyChat.lastFeedbackMessage = null;
			}
			int symbolX = geo.chatWidth() + 4 - font.width(symbol);
			graphics.text(font, symbol, symbolX, hover.messageY(), 0xFFFFFFFF);
		} else {
			CopyChat.lastFeedbackMessage = null;
		}
	}
}
