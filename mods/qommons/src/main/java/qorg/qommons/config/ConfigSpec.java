package qorg.qommons.config;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import qorg.qommons.config.storage.JsonConfigStorage;
import qorg.qommons.config.value.ConfigEditorType;
import qorg.qommons.config.value.ConfigValue;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ConfigSpec {
	private final String modId;
	private final Path path;
	private final Map<String, Entry<?>> entries = new LinkedHashMap<>();

	private ConfigSpec(String modId) {
		if (!modId.matches("[a-z0-9_-]+")) {
			throw new IllegalArgumentException("modId must contain only lowercase letters, numbers, '_' or '-'");
		}
		this.modId = modId;
		this.path = JsonConfigStorage.pathFor(modId);
	}

	public static ConfigSpec create(String modId) {
		return new ConfigSpec(modId);
	}

	public String modId() {
		return modId;
	}

	public String nameTranslationKey(String key) {
		return modId + ".config." + key;
	}

	public String descriptionTranslationKey(String key) {
		return nameTranslationKey(key) + ".description";
	}

	public Component displayName() {
		return Component.translatableWithFallback(modId + ".name", readableName(modId));
	}

	public <T> Entry<T> define(String key, ConfigValue<T> value) {
		String readableKey = readableName(key);
		return define(
				key,
				Component.translatableWithFallback(nameTranslationKey(key), readableKey),
				Component.translatableWithFallback(descriptionTranslationKey(key), "Configure " + readableKey + "."),
				value
		);
	}

	public <T> Entry<T> define(String key, Component name, Component description, ConfigValue<T> value) {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(description, "description");
		Objects.requireNonNull(value, "value");
		if (!key.matches("[a-z0-9_-]+")) {
			throw new IllegalArgumentException("key must contain only lowercase letters, numbers, '_' or '-'");
		}
		if (entries.containsKey(key)) {
			throw new IllegalArgumentException("Duplicate config key: " + key);
		}
		Entry<T> entry = new Entry<>(key, name, description, value);
		entries.put(key, entry);
		return entry;
	}

	public Map<String, Entry<?>> entries() {
		return Collections.unmodifiableMap(entries);
	}

	public void load() {
		JsonObject object = JsonConfigStorage.load(path);
		boolean changed = false;
		for (Entry<?> entry : entries.values()) {
			if (object.has(entry.key())) {
				changed |= entry.load(object.get(entry.key()));
			} else {
				entry.resetToDefault();
				changed = true;
			}
		}
		if (changed || object.isEmpty()) save();
	}

	public void save() {
		JsonObject object = new JsonObject();
		entries.values().forEach(entry -> entry.write(object));
		JsonConfigStorage.save(path, object);
	}

	private static String readableName(String value) {
		StringBuilder result = new StringBuilder(value.length());
		boolean capitalizeNext = true;
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			if (character == '_' || character == '-') {
				result.append(' ');
				capitalizeNext = true;
			} else {
				result.append(capitalizeNext ? Character.toUpperCase(character) : character);
				capitalizeNext = false;
			}
		}
		return result.toString();
	}

	public static final class Entry<T> {
		private final String key;
		private final Component name;
		private final Component description;
		private final ConfigValue<T> value;
		private T currentValue;

		private Entry(String key, Component name, Component description, ConfigValue<T> value) {
			this.key = key;
			this.name = name;
			this.description = description;
			this.value = value;
			this.currentValue = value.defaultValue();
		}

		public String key() {
			return key;
		}

		public Component name() {
			return name;
		}

		public Component description() {
			return description;
		}

		public T get() {
			return currentValue;
		}

		public void set(T newValue) {
			currentValue = Objects.requireNonNull(newValue, "newValue");
		}

		public T defaultValue() {
			return value.defaultValue();
		}

		public void resetToDefault() {
			currentValue = value.defaultValue();
		}

		public ArgumentType<T> argumentType() {
			return value.argumentType();
		}

		public T readArgument(CommandContext<FabricClientCommandSource> context, String name) {
			return value.readArgument(context, name);
		}

		public void setFromArgument(CommandContext<FabricClientCommandSource> context, String name) {
			set(readArgument(context, name));
		}

		public void setFromScreen(T value) {
			set(value);
		}

		public T parseText(String rawValue) {
			return value.parseText(rawValue);
		}

		public ConfigEditorType editorType() {
			return value.editorType();
		}

		public SuggestionProvider<FabricClientCommandSource> suggestions() {
			return (context, builder) -> {
				builder.suggest(String.valueOf(currentValue));
				return value.suggestions().getSuggestions(context, builder);
			};
		}

		private boolean load(com.google.gson.JsonElement element) {
			try {
				set(value.decode(element));
				return false;
			} catch (RuntimeException exception) {
				resetToDefault();
				return true;
			}
		}

		private void write(JsonObject object) {
			object.add(key, value.encode(currentValue));
		}
	}
}
