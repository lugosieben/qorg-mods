package qorg.qommons;

import net.fabricmc.api.ClientModInitializer;
import qorg.qommons.command.ConfigCommand;

public final class Qommons implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigCommand.register();
	}
}
