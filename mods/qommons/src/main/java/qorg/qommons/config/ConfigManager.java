package qorg.qommons.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConfigManager {
	private static final Map<String, ConfigSpec> SPECS = new LinkedHashMap<>();

	private ConfigManager() {
	}

	public static synchronized void register(ConfigSpec spec) {
		Objects.requireNonNull(spec, "spec");
		if (SPECS.putIfAbsent(spec.modId(), spec) != null) {
			throw new IllegalArgumentException("A config is already registered for " + spec.modId());
		}
		spec.load();
	}

	public static synchronized ConfigSpec get(String modId) {
		return SPECS.get(modId);
	}

	public static synchronized Collection<ConfigSpec> all() {
		return List.copyOf(SPECS.values());
	}
}
