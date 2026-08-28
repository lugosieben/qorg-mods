package qorg.qommons.config.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ConfigValue<T> {
	private final T defaultValue;
	private final Function<JsonElement, T> decoder;
	private final Function<T, JsonElement> encoder;
	private final ArgumentType<T> argumentType;
	private final BiFunction<CommandContext<FabricClientCommandSource>, String, T> argumentReader;
	private final SuggestionProvider<FabricClientCommandSource> suggestions;
	private final Function<String, T> textParser;
	private final ConfigEditorType editorType;

	private ConfigValue(T defaultValue, Function<JsonElement, T> decoder, Function<T, JsonElement> encoder, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> argumentReader, SuggestionProvider<FabricClientCommandSource> suggestions, Function<String, T> textParser, ConfigEditorType editorType) {
		this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
		this.decoder = Objects.requireNonNull(decoder, "decoder");
		this.encoder = Objects.requireNonNull(encoder, "encoder");
		this.argumentType = Objects.requireNonNull(argumentType, "argumentType");
		this.argumentReader = Objects.requireNonNull(argumentReader, "argumentReader");
		this.suggestions = Objects.requireNonNull(suggestions, "suggestions");
		this.textParser = Objects.requireNonNull(textParser, "textParser");
		this.editorType = Objects.requireNonNull(editorType, "editorType");
	}

	public static <T> ConfigValue<T> of(T defaultValue, Function<JsonElement, T> decoder, Function<T, JsonElement> encoder, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> argumentReader, SuggestionProvider<FabricClientCommandSource> suggestions, Function<String, T> textParser, ConfigEditorType editorType) {
		return new ConfigValue<>(defaultValue, decoder, encoder, argumentType, argumentReader, suggestions, textParser, editorType);
	}

	public static <T> ConfigValue<T> of(T defaultValue, Function<JsonElement, T> decoder, Function<T, JsonElement> encoder, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> argumentReader) {
		return of(defaultValue, decoder, encoder, argumentType, argumentReader, (_, builder) -> builder.suggest(String.valueOf(defaultValue)).buildFuture());
	}

	public static <T> ConfigValue<T> of(T defaultValue, Function<JsonElement, T> decoder, Function<T, JsonElement> encoder, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> argumentReader, SuggestionProvider<FabricClientCommandSource> suggestions) {
		return of(defaultValue, decoder, encoder, argumentType, argumentReader, suggestions, _ -> {
			throw new IllegalArgumentException("This config value does not define a screen parser");
		}, ConfigEditorType.TEXT);
	}

	public static <T> ConfigValue<T> of(T defaultValue, Function<JsonElement, T> decoder, Function<T, JsonElement> encoder, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> argumentReader, Function<String, T> textParser, ConfigEditorType editorType) {
		return of(defaultValue, decoder, encoder, argumentType, argumentReader, (_, builder) -> builder.suggest(String.valueOf(defaultValue)).buildFuture(), textParser, editorType);
	}

	public static ConfigValue<Boolean> bool(boolean defaultValue) {
		return new ConfigValue<>(defaultValue, JsonElement::getAsBoolean, JsonPrimitive::new, BoolArgumentType.bool(), BoolArgumentType::getBool, (_, builder) -> builder.suggest("true").suggest("false").buildFuture(), ConfigValue::parseBoolean, ConfigEditorType.BOOLEAN);
	}

	public static ConfigValue<Integer> integer(int defaultValue) {
		return new ConfigValue<>(defaultValue, JsonElement::getAsInt, JsonPrimitive::new, IntegerArgumentType.integer(), IntegerArgumentType::getInteger, suggest(defaultValue), Integer::parseInt, ConfigEditorType.INTEGER);
	}

	public static ConfigValue<Integer> integer(int defaultValue, int minimum, int maximum) {
		if (minimum > maximum) {
			throw new IllegalArgumentException("minimum must not be greater than maximum");
		}
		validateIntegerRange(defaultValue, minimum, maximum);
		return new ConfigValue<>(
				defaultValue,
				element -> validateIntegerRange(element.getAsInt(), minimum, maximum),
				JsonPrimitive::new,
				IntegerArgumentType.integer(minimum, maximum),
				IntegerArgumentType::getInteger,
				suggest(defaultValue),
				rawValue -> validateIntegerRange(Integer.parseInt(rawValue), minimum, maximum),
				ConfigEditorType.INTEGER
		);
	}

	public static ConfigValue<Long> longValue(long defaultValue) {
		return new ConfigValue<>(defaultValue, JsonElement::getAsLong, JsonPrimitive::new, LongArgumentType.longArg(), LongArgumentType::getLong, suggest(defaultValue), Long::parseLong, ConfigEditorType.LONG);
	}

	public static ConfigValue<Double> decimal(double defaultValue) {
		return new ConfigValue<>(defaultValue, JsonElement::getAsDouble, JsonPrimitive::new, DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble, suggest(defaultValue), Double::parseDouble, ConfigEditorType.DECIMAL);
	}

	public static ConfigValue<String> string(String defaultValue) {
		return new ConfigValue<>(defaultValue, JsonElement::getAsString, JsonPrimitive::new, StringArgumentType.greedyString(), StringArgumentType::getString, suggest(defaultValue), Function.identity(), ConfigEditorType.TEXT);
	}

	public T defaultValue() {
		return defaultValue;
	}

	public T decode(JsonElement element) {
		return decoder.apply(element);
	}

	public JsonElement encode(T value) {
		return encoder.apply(value);
	}

	public ArgumentType<T> argumentType() {
		return argumentType;
	}

	public T readArgument(CommandContext<FabricClientCommandSource> context, String name) {
		return argumentReader.apply(context, name);
	}

	public SuggestionProvider<FabricClientCommandSource> suggestions() {
		return suggestions;
	}

	public T parseText(String value) {
		return textParser.apply(value);
	}

	public ConfigEditorType editorType() {
		return editorType;
	}

	private static <T> SuggestionProvider<FabricClientCommandSource> suggest(T value) {
		return (_, builder) -> builder.suggest(String.valueOf(value)).buildFuture();
	}

	private static Boolean parseBoolean(String value) {
		if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
			throw new IllegalArgumentException("Expected true or false");
		}
		return Boolean.parseBoolean(value);
	}

	private static int validateIntegerRange(int value, int minimum, int maximum) {
		if (value < minimum || value > maximum) {
			throw new IllegalArgumentException("Expected a value between " + minimum + " and " + maximum);
		}
		return value;
	}
}
