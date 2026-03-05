package qc.mat.minigames.game;

import net.minecraft.server.MinecraftServer;
import qc.mat.minigames.MatMinigames;
import qc.mat.minigames.arena.Arena;
import qc.mat.minigames.arena.ArenaManager;
import qc.mat.minigames.config.ConfigManager;
import qc.mat.minigames.games.duel.DuelGame;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {
	private static final GameManager INSTANCE = new GameManager();
	private final Map<String, Minigame> activeGames = new LinkedHashMap<>();
	private final AtomicInteger gameCounter = new AtomicInteger(0);
	private MinecraftServer server;

	private GameManager() {}

	public static GameManager getInstance() {
		return INSTANCE;
	}

	public void init(MinecraftServer server) {
		this.server = server;
		activeGames.clear();
		gameCounter.set(0);
	}

	public void shutdown() {
		for (Minigame game : new ArrayList<>(activeGames.values())) {
			game.cleanup();
		}
		activeGames.clear();
	}

	public void tick() {
		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, Minigame> entry : activeGames.entrySet()) {
			Minigame game = entry.getValue();
			game.tick();
			if (game.getState() == GameState.ENDING && game.isEmpty()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String id : toRemove) {
			activeGames.remove(id);
			MatMinigames.LOGGER.info("Game {} cleaned up", id);
		}
	}

	public Minigame createGame(String type, Arena arena) {
		if (activeGames.size() >= ConfigManager.getServerConfig().maxConcurrentGames) {
			return null;
		}
		String gameId = type + "_" + gameCounter.incrementAndGet();
		Minigame game = switch (type) {
			case "duel" -> new DuelGame(gameId, arena, server);
			default -> null;
		};
		if (game == null) return null;
		game.setup();
		activeGames.put(gameId, game);
		MatMinigames.LOGGER.info("Created game {} on arena {}", gameId, arena.id());
		return game;
	}

	public Minigame getGame(String gameId) {
		return activeGames.get(gameId);
	}

	public Minigame findAvailableGame(String type) {
		for (Minigame game : activeGames.values()) {
			if (game.getType().equals(type) && game.getState() == GameState.WAITING && !game.isFull()) {
				return game;
			}
		}
		return null;
	}

	public Minigame findOrCreateGame(String type) {
		Minigame game = findAvailableGame(type);
		if (game != null) return game;
		List<Arena> arenas = ArenaManager.getArenasByType(type);
		for (Arena arena : arenas) {
			if (!isArenaInUse(arena.id())) {
				return createGame(type, arena);
			}
		}
		return null;
	}

	private boolean isArenaInUse(String arenaId) {
		return activeGames.values().stream()
			.anyMatch(g -> g.getArena().id().equals(arenaId) && g.getState() != GameState.ENDING);
	}

	public Collection<Minigame> getActiveGames() {
		return Collections.unmodifiableCollection(activeGames.values());
	}
}