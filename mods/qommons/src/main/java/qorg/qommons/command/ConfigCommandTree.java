package qorg.qommons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import qorg.qommons.config.ConfigManager;
import qorg.qommons.config.ConfigSpec;

public final class ConfigCommandTree {
	private ConfigCommandTree() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		LiteralArgumentBuilder<FabricClientCommandSource> config = ClientCommands.literal("config")
				.executes(context -> ConfigCommandHandler.listConfigs(context.getSource()));

		for (ConfigSpec spec : ConfigManager.all()) {
			LiteralArgumentBuilder<FabricClientCommandSource> mod = ClientCommands.literal(spec.modId())
					.executes(context -> ConfigCommandHandler.listKeys(context.getSource(), spec));
			for (ConfigSpec.Entry<?> entry : spec.entries().values()) {
				mod.then(createEntryBranch(spec, entry));
			}
			config.then(mod);
		}

		dispatcher.register(ClientCommands.literal("qommons")
				.requires(FabricClientCommandSource::attended)
				.then(config));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> createEntryBranch(ConfigSpec spec, ConfigSpec.Entry<?> entry) {
		LiteralArgumentBuilder<FabricClientCommandSource> key = ClientCommands.literal(entry.key())
				.executes(context -> ConfigCommandHandler.getValue(context.getSource(), spec.modId(), entry));

		key.then(ClientCommands.literal("set")
				.then(ClientCommands.argument("value", entry.argumentType())
						.suggests(entry.suggestions())
						.executes(context -> ConfigCommandHandler.setValue(context.getSource(), spec, entry, context, "value"))));
		key.then(ClientCommands.literal("reset")
				.executes(context -> ConfigCommandHandler.resetValue(context.getSource(), spec, entry)));
		return key;
	}
}
