package qc.mat.minigames.lobby;

import net.minecraft.server.MinecraftServer;
import qc.mat.minigames.MatMinigames;
import qc.mat.minigames.config.ConfigManager;

public class LobbyManager {
	private static MinecraftServer server;
	private static boolean isLobbyMode;

	public static void init(MinecraftServer srv) {
		server = srv;
		isLobbyMode = "LOBBY".equalsIgnoreCase(ConfigManager.getServerConfig().serverMode);
		if (isLobbyMode) {
			MatMinigames.LOGGER.info("Server running in LOBBY mode");
		}
	}

	public static boolean isLobbyMode() {
		return isLobbyMode;
	}

	public static MinecraftServer getServer() {
		return server;
	}
}