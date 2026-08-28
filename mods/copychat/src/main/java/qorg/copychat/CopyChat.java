package qorg.copychat;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qorg.copychat.config.CopyChatConfig;

public final class CopyChat implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("copychat");

	@Override
	public void onInitialize() {
		CopyChatConfig.initialize();
		LOGGER.info("CopyChat initialized");
	}
}
