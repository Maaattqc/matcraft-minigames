package qc.mat.minigames.config;

import java.util.List;
import java.util.Map;

public class DuelConfig {
	public int countdownSeconds = 5;
	public int endDelaySeconds = 3;
	public int challengeTimeoutSeconds = 30;
	public KitConfig kit = new KitConfig();

	public static class KitConfig {
		public ItemEntry sword = new ItemEntry("DIAMOND_SWORD", 1, null, Map.of("sharpness", 1));
		public ItemEntry helmet = new ItemEntry("IRON_HELMET", 1, null, Map.of("protection", 1));
		public ItemEntry chestplate = new ItemEntry("IRON_CHESTPLATE", 1, null, Map.of("protection", 1));
		public ItemEntry leggings = new ItemEntry("IRON_LEGGINGS", 1, null, Map.of("protection", 1));
		public ItemEntry boots = new ItemEntry("IRON_BOOTS", 1, null, Map.of("protection", 1));
		public ItemEntry offhand = new ItemEntry("SHIELD", 1, null, Map.of());
		public List<ItemEntry> items = List.of(
			new ItemEntry("SPLASH_POTION", 2, "strong_healing", Map.of()),
			new ItemEntry("COOKED_BEEF", 16, null, Map.of())
		);
	}

	public static class ItemEntry {
		public String material;
		public int count;
		public String potion;
		public Map<String, Integer> enchantments;

		public ItemEntry() {}

		public ItemEntry(String material, int count, String potion, Map<String, Integer> enchantments) {
			this.material = material;
			this.count = count;
			this.potion = potion;
			this.enchantments = enchantments;
		}
	}
}