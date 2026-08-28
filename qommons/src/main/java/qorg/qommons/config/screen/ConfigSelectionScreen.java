package qorg.qommons.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import qorg.qommons.config.ConfigManager;
import qorg.qommons.config.ConfigSpec;

import java.util.List;

public final class ConfigSelectionScreen extends Screen {
	private final Screen parent;

	private ConfigSelectionScreen(Screen parent) {
		super(Component.translatable("qommons.config.title"));
		this.parent = parent;
	}

	public static ConfigSelectionScreen create(Screen parent) {
		return new ConfigSelectionScreen(parent);
	}

	@Override
	protected void init() {
		addRenderableWidget(new ConfigList(minecraft, width, height));
		int titleWidth = Math.clamp(font.width(title), 1, 300);
		StringWidget titleWidget = new StringWidget((width - titleWidth) / 2, 14, titleWidth, 20, title, font);
		titleWidget.setMaxWidth(titleWidth);
		addRenderableOnly(titleWidget);
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> closeToParent()).bounds(width / 2 - 50, height - 28, 100, 20).build());
	}

	private void closeToParent() {
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void onClose() {
		closeToParent();
	}

	private final class ConfigList extends ContainerObjectSelectionList<ConfigListEntry> {
		private ConfigList(Minecraft minecraft, int width, int height) {
			super(minecraft, width, height - 42 - 28, 42, 24);
			for (ConfigSpec spec : ConfigManager.all()) addEntry(new ConfigListEntry(spec));
		}

		@Override
		public int getRowWidth() {
			return Math.min(420, width - 20);
		}
	}

	private final class ConfigListEntry extends ContainerObjectSelectionList.Entry<ConfigListEntry> {
		private final ConfigSpec spec;
		private final Button openButton;

		private ConfigListEntry(ConfigSpec spec) {
			this.spec = spec;
				this.openButton = Button.builder(Component.translatable("qommons.config.configure"), _ -> minecraft.setScreenAndShow(ConfigScreen.create(ConfigSelectionScreen.this, spec)))
					.bounds(0, 0, 100, 20)
					.build();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
			int x = getX() + 2;
			int y = getY() + 2;
			int width = getWidth() - 4;
			graphics.text(font, spec.displayName(), x, y + 7, 0xFFFFFFFF);
			openButton.setRectangle(100, 20, x + width - 100, y + 2);
			openButton.extractRenderState(graphics, mouseX, mouseY, delta);
		}

		@Override
		public @NonNull List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
			return List.of(openButton);
		}

		@Override
		public @NonNull List<? extends NarratableEntry> narratables() {
			return List.of(openButton);
		}

		@Override
		public int getHeight() {
			return 24;
		}
	}
}
