package qorg.qommons.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import qorg.qommons.config.ConfigSpec;
import qorg.qommons.config.value.ConfigEditorType;

import java.util.List;
import java.util.function.Consumer;

final class ConfigScreenEntry extends ContainerObjectSelectionList.Entry<ConfigScreenEntry> {
	private static final int HEIGHT = 24;
	private static final int RESET_WIDTH = 20;
	private static final int VALUE_WIDTH = 120;
	private static final int VALUE_GAP = 8;
	private static final Component RESET_LABEL = Component.translatable("qommons.config.reset_button");

	private final ConfigSpec.Entry<?> entry;
	private Object committedValue;
	private final Consumer<ConfigScreenEntry> changed;
	private final AbstractWidget valueWidget;
	private final Button resetButton;
	private Object draftValue;
	private boolean valid;
	private Component errorMessage;

	record State(Object committedValue, Object draftValue, boolean valid, Component errorMessage, String text) {
	}

	ConfigScreenEntry(Minecraft minecraft, ConfigSpec.Entry<?> entry, State previousState, Consumer<ConfigScreenEntry> changed) {
		this.entry = entry;
		this.committedValue = previousState == null ? entry.get() : previousState.committedValue();
		this.draftValue = previousState == null ? committedValue : previousState.draftValue();
		this.valid = previousState == null || previousState.valid();
		this.errorMessage = previousState == null ? null : previousState.errorMessage();
		this.changed = changed;
		this.valueWidget = createValueWidget(minecraft);
		if (previousState != null && valueWidget instanceof EditBox editBox) {
			editBox.setValue(previousState.text());
			this.committedValue = previousState.committedValue();
			this.draftValue = previousState.draftValue();
			this.valid = previousState.valid();
			this.errorMessage = previousState.errorMessage();
			if (!valid) editBox.setTextColor(0xFFFF5555);
		}
		this.resetButton = Button.builder(RESET_LABEL, _ -> resetToDefault())
				.bounds(0, 0, RESET_WIDTH, 20)
				.tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("qommons.config.reset")))
				.build();
		updateResetButton();
	}

	State state() {
		String text = valueWidget instanceof EditBox editBox ? editBox.getValue() : String.valueOf(draftValue);
		return new State(committedValue, draftValue, valid, errorMessage, text);
	}

	private AbstractWidget createValueWidget(Minecraft minecraft) {
		if (entry.editorType() == ConfigEditorType.BOOLEAN) {
			return CycleButton.booleanBuilder(
					Component.translatable("qommons.config.on"),
					Component.translatable("qommons.config.off"),
					(Boolean) draftValue)
					.displayOnlyValue()
					.withTooltip(_ -> net.minecraft.client.gui.components.Tooltip.create(entry.description()))
					.create(0, 0, 120, 20, Component.empty(), (_, value) -> updateDraft(value));
		}

		EditBox editBox = new EditBox(minecraft.font, 0, 0, 120, 20, entry.name());
		editBox.setValue(String.valueOf(draftValue));
		editBox.setResponder(this::parseText);
		editBox.setTooltip(net.minecraft.client.gui.components.Tooltip.create(entry.description()));
		return editBox;
	}

	private void parseText(String rawValue) {
		try {
			updateDraft(entry.parseText(rawValue));
			valid = true;
			errorMessage = null;
			((EditBox) valueWidget).setTextColor(EditBox.DEFAULT_TEXT_COLOR);
		} catch (RuntimeException exception) {
			valid = false;
			errorMessage = Component.translatable("qommons.config.invalid_value");
			((EditBox) valueWidget).setTextColor(0xFFFF5555);
			changed.accept(this);
		}
	}

	private void updateDraft(Object value) {
		draftValue = value;
		valid = true;
		errorMessage = null;
		updateResetButton();
		changed.accept(this);
	}

	void resetToDefault() {
		draftValue = entry.defaultValue();
		valid = true;
		errorMessage = null;
		if (valueWidget instanceof CycleButton<?> cycleButton) {
			setCycleValue(cycleButton, draftValue);
		} else if (valueWidget instanceof EditBox editBox) {
			editBox.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
			editBox.setValue(String.valueOf(draftValue));
		}
		updateResetButton();
		changed.accept(this);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void setCycleValue(CycleButton<?> button, Object value) {
		((CycleButton) button).setValue(value);
	}

	private void updateResetButton() {
		resetButton.active = !draftValue.equals(entry.defaultValue());
	}

	boolean valid() {
		return valid;
	}

	boolean changed() {
		return valid && !draftValue.equals(committedValue);
	}

	boolean atDefault() {
		return draftValue.equals(entry.defaultValue());
	}

	void apply() {
		if (!valid) return;
		applyValue(entry, draftValue);
		committedValue = draftValue;
	}

	@SuppressWarnings("unchecked")
	private static void applyValue(ConfigSpec.Entry<?> entry, Object value) {
		ConfigSpec.Entry<Object> typedEntry = (ConfigSpec.Entry<Object>) entry;
		typedEntry.setFromScreen(value);
	}

	void discard() {
		draftValue = committedValue;
		if (valueWidget instanceof CycleButton<?> cycleButton) {
			setCycleValue(cycleButton, draftValue);
		} else if (valueWidget instanceof EditBox editBox) {
			editBox.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
			editBox.setValue(String.valueOf(draftValue));
		}
		valid = true;
		errorMessage = null;
		updateResetButton();
	}


	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
		int rowX = getX() + 2;
		int rowY = getY() + 2;
		int rowWidth = getWidth() - 4;
		int rowHeight = getHeight() - 4;
		int valueWidth = Math.clamp(rowWidth - RESET_WIDTH - VALUE_GAP, 1, VALUE_WIDTH);
		int valueX = rowX + rowWidth - RESET_WIDTH - VALUE_GAP - valueWidth;
		int labelWidth = Math.max(1, valueX - rowX - VALUE_GAP);
		graphics.text(Minecraft.getInstance().font, fitLabel(entry.name(), labelWidth), rowX, rowY + 7, 0xFFFFFFFF);
		int controlY = rowY + Math.max(0, (rowHeight - 20) / 2);
		valueWidget.setRectangle(valueWidth, 20, valueX, controlY);
		resetButton.setRectangle(RESET_WIDTH, 20, rowX + rowWidth - RESET_WIDTH, controlY);
		valueWidget.extractRenderState(graphics, mouseX, mouseY, delta);
		resetButton.extractRenderState(graphics, mouseX, mouseY, delta);
		if (errorMessage != null) {
			graphics.text(Minecraft.getInstance().font, errorMessage, rowX, rowY + 22, 0xFFFF5555);
		}
	}

	private Component fitLabel(Component label, int maxWidth) {
		if (Minecraft.getInstance().font.width(label) <= maxWidth) return label;
		int ellipsisWidth = Minecraft.getInstance().font.width(Component.translatable("qommons.config.ellipsis"));
		if (maxWidth <= ellipsisWidth) return Minecraft.getInstance().font.substrByWidth(label, maxWidth).getString().isEmpty() ? Component.empty() : Minecraft.getInstance().font.substrByWidth(label, maxWidth).getString().equals(label.getString()) ? label : Component.translatable("qommons.config.ellipsis");
		return Minecraft.getInstance().font.substrByWidth(label, maxWidth - ellipsisWidth).getString().isEmpty() ? Component.translatable("qommons.config.ellipsis") : Component.translatable("qommons.config.clipped_label", Minecraft.getInstance().font.substrByWidth(label, maxWidth - ellipsisWidth));
	}

	@Override
	public @NonNull List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
		return List.of(valueWidget, resetButton);
	}

	@Override
	public @NonNull List<? extends NarratableEntry> narratables() {
		return List.of(valueWidget, resetButton);
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}
}
