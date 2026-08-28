package qorg.copychat.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import qorg.copychat.config.CopyChatConfig;
import qorg.qommons.config.screen.ConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> ConfigScreen.create(parent, CopyChatConfig.SPEC);
	}
}
