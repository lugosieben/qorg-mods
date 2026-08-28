package qorg.qommons.config.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class JsonConfigStorage {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private JsonConfigStorage() {
	}

	public static Path pathFor(String modId) {
		return FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
	}

	public static JsonObject load(Path path) {
		if (Files.notExists(path)) return new JsonObject();
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonObject object = GSON.fromJson(reader, JsonObject.class);
			return object == null ? new JsonObject() : object;
		} catch (IOException | JsonParseException exception) {
			throw new IllegalStateException("Could not read " + path, exception);
		}
	}

	public static void save(Path path, JsonObject object) {
		Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
				GSON.toJson(object, writer);
			}
			try {
				Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException atomicMoveUnsupported) {
				Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Could not write " + path, exception);
		}
	}
}
