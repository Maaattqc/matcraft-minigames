package qc.mat.minigames.game;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
	private static final Map<UUID, String> playerToGame = new ConcurrentHashMap<>();

	public static void assignPlayer(UUID uuid, String gameId) {
		playerToGame.put(uuid, gameId);
	}

	public static void removePlayer(UUID uuid) {
		playerToGame.remove(uuid);
	}

	public static String getPlayerGame(UUID uuid) {
		return playerToGame.get(uuid);
	}

	public static boolean isInGame(UUID uuid) {
		return playerToGame.containsKey(uuid);
	}

	public static void handleDisconnect(ServerPlayer player) {
		UUID uuid = player.getUUID();
		String gameId = playerToGame.get(uuid);
		if (gameId == null) return;
		Minigame game = GameManager.getInstance().getGame(gameId);
		if (game != null) {
			game.removePlayer(player);
		}
		playerToGame.remove(uuid);
	}
}