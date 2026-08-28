package qorg.qommons.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import qorg.qommons.config.ConfigManager;
import qorg.qommons.config.ConfigSpec;
import qorg.qommons.config.screen.ConfigScreen;
import qorg.qommons.config.screen.ConfigSelectionScreen;

public final class ConfigCommandHandler {
	private ConfigCommandHandler() {
	}

	public static int listConfigs(FabricClientCommandSource source) {
		if (ConfigManager.all().isEmpty()) {
			source.sendError(Component.translatable("qommons.config.no_configs"));
			return 0;
		}
		source.getClient().schedule(() -> source.getClient().setScreenAndShow(ConfigSelectionScreen.create(null)));
		return 1;
	}

	public static int listKeys(FabricClientCommandSource source, ConfigSpec spec) {
		source.getClient().schedule(() -> source.getClient().setScreenAndShow(ConfigScreen.create(null, spec)));
		return 1;
	}

	public static int getValue(FabricClientCommandSource source, String modId, ConfigSpec.Entry<?> entry) {
		source.sendFeedback(Component.translatable("qommons.config.value", modId, entry.key(), entry.get()));
		return 1;
	}

	public static <T> int setValue(FabricClientCommandSource source, ConfigSpec spec, ConfigSpec.Entry<T> entry, CommandContext<FabricClientCommandSource> context, String argumentName) {
		entry.setFromArgument(context, argumentName);
		spec.save();
		source.sendFeedback(Component.translatable("qommons.config.value_set", spec.modId(), entry.key(), entry.get()));
		return 1;
	}

	public static int resetValue(FabricClientCommandSource source, ConfigSpec spec, ConfigSpec.Entry<?> entry) {
		entry.resetToDefault();
		spec.save();
		source.sendFeedback(Component.translatable("qommons.config.value_reset", spec.modId(), entry.key(), entry.get()));
		return 1;
	}

}
