package qorg.qommons.config.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import qorg.qommons.config.ConfigSpec;

import java.util.List;

public final class ConfigScreen extends Screen {
	private static final int LIST_TOP = 42;
	private static final int FOOTER_HEIGHT = 64;
	private static final int BUTTON_GAP = 4;
	private static final int MAX_BUTTON_WIDTH = 100;
	private static final int BUTTON_HEIGHT = 20;

	private final Screen parent;
	private final ConfigSpec spec;
	private ConfigScreenList optionList;
	private StringWidget status;
	private Button applyButton;
	private Button doneButton;
	private Button resetButton;
	private boolean hasChanges;

	private ConfigScreen(Screen parent, ConfigSpec spec) {
		super(Component.translatable("qommons.config.title_for_mod", spec.displayName()));
		this.parent = parent;
		this.spec = spec;
	}

	public static ConfigScreen create(Screen parent, ConfigSpec spec) {
		return new ConfigScreen(parent, spec);
	}

	@Override
	protected void init() {
		List<ConfigScreenEntry.State> previousEntries = optionList == null ? List.of() : optionList.optionEntries().stream().map(ConfigScreenEntry::state).toList();
		optionList = addRenderableWidget(new ConfigScreenList(minecraft, width, height, LIST_TOP, FOOTER_HEIGHT, spec, previousEntries, this::onEntryChanged));
		int titleWidth = Math.clamp(font.width(title), 1, 300);
		StringWidget titleWidget = new StringWidget((width - titleWidth) / 2, 14, titleWidth, 20, title, font);
		titleWidget.setMaxWidth(titleWidth);
		addRenderableOnly(titleWidget);

		int buttonY = height - 28;
		int buttonWidth = Math.clamp((width - 16 - BUTTON_GAP * 3) / 4, 1, MAX_BUTTON_WIDTH);
		int totalWidth = buttonWidth * 4 + BUTTON_GAP * 3;
		int buttonX = Math.max(8, (width - totalWidth) / 2);
		applyButton = addRenderableWidget(Button.builder(Component.translatable("qommons.config.apply"), _ -> applyChanges()).bounds(buttonX, buttonY, buttonWidth, BUTTON_HEIGHT).build());
		doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> finish()).bounds(buttonX + buttonWidth + BUTTON_GAP, buttonY, buttonWidth, BUTTON_HEIGHT).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, _ -> cancel()).bounds(buttonX + (buttonWidth + BUTTON_GAP) * 2, buttonY, buttonWidth, BUTTON_HEIGHT).build());
		resetButton = addRenderableWidget(Button.builder(Component.translatable("qommons.config.reset_all"), _ -> resetAll()).bounds(buttonX + (buttonWidth + BUTTON_GAP) * 3, buttonY, buttonWidth, BUTTON_HEIGHT).build());
		int statusWidth = Math.max(1, width - 16);
		status = addRenderableWidget(new StringWidget(8, height - 52, statusWidth, 16, Component.empty(), font));
		status.setMaxWidth(statusWidth);
		updateButtons();
	}

	private void onEntryChanged(ConfigScreenEntry ignored) {
		hasChanges = optionList.optionEntries().stream().anyMatch(ConfigScreenEntry::changed);
		updateButtons();
	}

	private boolean allValid() {
		return optionList.optionEntries().stream().allMatch(ConfigScreenEntry::valid);
	}

	private void applyChanges() {
		if (!allValid()) {
			showStatusKey("qommons.config.invalid");
			return;
		}
		optionList.optionEntries().forEach(ConfigScreenEntry::apply);
		spec.save();
		hasChanges = false;
		updateButtons();
		showStatusKey("qommons.config.saved_for_mod", spec.displayName());
	}

	private void finish() {
		if (!hasChanges) {
			closeToParent();
			return;
		}
		if (allValid()) {
			applyChanges();
			closeToParent();
		}
	}

	private void cancel() {
		optionList.optionEntries().forEach(ConfigScreenEntry::discard);
		hasChanges = false;
		closeToParent();
	}

	private void resetAll() {
		optionList.optionEntries().forEach(ConfigScreenEntry::resetToDefault);
		hasChanges = true;
		updateButtons();
	}

	private void closeToParent() {
		minecraft.setScreenAndShow(parent);
	}

	private void updateButtons() {
		if (applyButton == null) return;
		boolean valid = allValid();
		applyButton.active = hasChanges && valid;
		doneButton.active = valid;
		resetButton.active = optionList.optionEntries().stream().anyMatch(entry -> !entry.atDefault());
	}

	private void showStatusKey(String key, Object... arguments) {
		status.setMessage(Component.translatable(key, arguments));
	}

	@Override
	public boolean shouldCloseOnEsc() {
		if (hasChanges) {
			showStatusKey("qommons.config.unsaved");
			return false;
		}
		return true;
	}

	@Override
	public void onClose() {
		if (hasChanges) {
			showStatusKey("qommons.config.unsaved");
		} else {
			closeToParent();
		}
	}

}
