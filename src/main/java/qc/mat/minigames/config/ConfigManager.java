package qc.mat.minigames.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import qc.mat.minigames.MatMinigames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("matminigames");

	private static ServerConfig serverConfig;
	private static MessagesConfig messagesConfig;
	private static DuelConfig duelConfig;
	private static ArenasConfig arenasConfig;

	public static <T> T load(String filename, Class<T> type, T defaultValue) {
		Path file = CONFIG_DIR.resolve(filename + ".json");
		try {
			if (!Files.exists(CONFIG_DIR)) {
				Files.createDirectories(CONFIG_DIR);
			}
			if (!Files.exists(file)) {
				String json = GSON.toJson(defaultValue);
				Files.writeString(file, json);
				return defaultValue;
			}
			String json = Files.readString(file);
			return GSON.fromJson(json, type);
		} catch (IOException e) {
			MatMinigames.LOGGER.error("Failed to load config {}: {}", filename, e.getMessage());
			return defaultValue;
		}
	}

	public static void loadAll() {
		serverConfig = load("server", ServerConfig.class, new ServerConfig());
		messagesConfig = load("messages", MessagesConfig.class, new MessagesConfig());
		duelConfig = load("duel", DuelConfig.class, new DuelConfig());
		arenasConfig = load("arenas", ArenasConfig.class, new ArenasConfig());
		MatMinigames.LOGGER.info("Configs loaded from {}", CONFIG_DIR);
	}

	public static ServerConfig getServerConfig() {
		return serverConfig;
	}

	public static MessagesConfig getMessages() {
		return messagesConfig;
	}

	public static DuelConfig getDuelConfig() {
		return duelConfig;
	}

	public static ArenasConfig getArenasConfig() {
		return arenasConfig;
	}
}