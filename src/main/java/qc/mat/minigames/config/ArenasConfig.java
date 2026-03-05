package qc.mat.minigames.config;

import java.util.ArrayList;
import java.util.List;

public class ArenasConfig {
	public List<ArenaEntry> arenas = new ArrayList<>(List.of(
		new ArenaEntry()
	));

	public static class ArenaEntry {
		public String id = "duel_arena_1";
		public String name = "Arena 1";
		public String gameType = "duel";
		public String world = "minecraft:overworld";
		public List<SpawnPoint> spawnPoints = List.of(
			new SpawnPoint(100, 65, 100),
			new SpawnPoint(120, 65, 100)
		);
	}

	public static class SpawnPoint {
		public double x;
		public double y;
		public double z;

		public SpawnPoint() {}

		public SpawnPoint(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}