package qorg.qommons.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public final class ConfigCommand {
	private ConfigCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> ConfigCommandTree.register(dispatcher));
	}
}
