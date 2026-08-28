package qorg.qommons.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import qorg.qommons.config.ConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class ConfigScreenList extends ContainerObjectSelectionList<ConfigScreenEntry> {
	private final List<ConfigScreenEntry> optionEntries = new ArrayList<>();

	ConfigScreenList(Minecraft minecraft, int width, int screenHeight, int top, int footerHeight, ConfigSpec spec, List<ConfigScreenEntry.State> previousEntries, Consumer<ConfigScreenEntry> changed) {
		super(minecraft, width, screenHeight - top - footerHeight, top, 24);
		int index = 0;
		for (ConfigSpec.Entry<?> entry : spec.entries().values()) {
			ConfigScreenEntry.State previousEntry = index < previousEntries.size() ? previousEntries.get(index) : null;
			ConfigScreenEntry optionEntry = new ConfigScreenEntry(minecraft, entry, previousEntry, changed);
			optionEntries.add(optionEntry);
			addEntry(optionEntry);
			index++;
		}
	}

	List<ConfigScreenEntry> optionEntries() {
		return List.copyOf(optionEntries);
	}

	@Override
	public int getRowWidth() {
		return Math.min(420, width - 20);
	}
}
