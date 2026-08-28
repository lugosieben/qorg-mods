package qorg.copychat.config;

import qorg.qommons.config.ConfigManager;
import qorg.qommons.config.ConfigSpec;
import qorg.qommons.config.value.ConfigValue;

public final class CopyChatConfig {
	public static final ConfigSpec SPEC = ConfigSpec.create("copychat");
	public static final ConfigSpec.Entry<Boolean> SHOW_COPY_BUTTON = SPEC.define("show_copy_button", ConfigValue.bool(true));
	public static final ConfigSpec.Entry<Boolean> CLICK_ANYWHERE_TO_COPY = SPEC.define("click_anywhere_to_copy", ConfigValue.bool(false));
	public static final ConfigSpec.Entry<Boolean> USE_RIGHT_CLICK_TO_COPY = SPEC.define("use_right_click_to_copy", ConfigValue.bool(false));
	public static final ConfigSpec.Entry<Boolean> REQUIRE_SHIFT_TO_COPY = SPEC.define("require_shift_to_copy", ConfigValue.bool(false));
	public static final ConfigSpec.Entry<Boolean> COPY_AS_JSON = SPEC.define("copy_as_json", ConfigValue.bool(false));
	public static final ConfigSpec.Entry<Boolean> SHOW_COPY_FEEDBACK = SPEC.define("show_copy_feedback", ConfigValue.bool(true));

	private CopyChatConfig() {
	}

	public static void initialize() {
		ConfigManager.register(SPEC);
	}
}
