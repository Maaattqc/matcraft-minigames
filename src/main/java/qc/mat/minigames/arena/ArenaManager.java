package qc.mat.minigames.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import qc.mat.minigames.MatMinigames;
import qc.mat.minigames.config.ArenasConfig;
import qc.mat.minigames.config.ConfigManager;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaManager {
	private static final Map<String, Arena> arenas = new LinkedHashMap<>();

	public static void init() {
		arenas.clear();
		ArenasConfig config = ConfigManager.getArenasConfig();
		for (ArenasConfig.ArenaEntry entry : config.arenas) {
			List<BlockPos> spawns = entry.spawnPoints.stream()
				.map(sp -> new BlockPos((int) sp.x, (int) sp.y, (int) sp.z))
				.collect(Collectors.toList());
			Arena arena = new Arena(
				entry.id,
				entry.name,
				entry.gameType,
				Identifier.parse(entry.world),
				spawns
			);
			arenas.put(arena.id(), arena);
		}
		MatMinigames.LOGGER.info("Loaded {} arenas", arenas.size());
	}

	public static Arena getArena(String id) {
		return arenas.get(id);
	}

	public static List<Arena> getArenasByType(String gameType) {
		return arenas.values().stream()
			.filter(a -> a.gameType().equals(gameType))
			.collect(Collectors.toList());
	}

	public static Collection<Arena> getAllArenas() {
		return Collections.unmodifiableCollection(arenas.values());
	}
}