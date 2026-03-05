package qc.mat.minigames.games.duel;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import qc.mat.minigames.config.ConfigManager;
import qc.mat.minigames.config.DuelConfig;

import java.util.Map;
import java.util.Optional;

public class DuelKit {

	public static void applyKit(ServerPlayer player, MinecraftServer server) {
		DuelConfig.KitConfig kit = ConfigManager.getDuelConfig().kit;

		player.getInventory().clearContent();

		HolderLookup<Enchantment> enchReg = server.registryAccess()
			.lookupOrThrow(Registries.ENCHANTMENT);

		// Main hand - sword (slot 0)
		player.getInventory().setItem(0, buildItem(kit.sword, enchReg));

		// Armor via EquipmentSlot
		player.setItemSlot(EquipmentSlot.HEAD, buildItem(kit.helmet, enchReg));
		player.setItemSlot(EquipmentSlot.CHEST, buildItem(kit.chestplate, enchReg));
		player.setItemSlot(EquipmentSlot.LEGS, buildItem(kit.leggings, enchReg));
		player.setItemSlot(EquipmentSlot.FEET, buildItem(kit.boots, enchReg));

		// Offhand
		player.setItemSlot(EquipmentSlot.OFFHAND, buildItem(kit.offhand, enchReg));

		// Extra items starting at slot 1
		int slot = 1;
		for (DuelConfig.ItemEntry entry : kit.items) {
			if (slot < 36) {
				player.getInventory().setItem(slot++, buildItem(entry, enchReg));
			}
		}

		player.containerMenu.broadcastChanges();
	}

	public static void clearKit(ServerPlayer player) {
		player.getInventory().clearContent();
		player.containerMenu.broadcastChanges();
	}

	private static ItemStack buildItem(DuelConfig.ItemEntry entry, HolderLookup<Enchantment> enchReg) {
		if (entry == null || entry.material == null) return ItemStack.EMPTY;

		Identifier itemId = Identifier.fromNamespaceAndPath("minecraft", entry.material.toLowerCase());
		var item = BuiltInRegistries.ITEM.get(itemId);
		if (item.isEmpty()) return ItemStack.EMPTY;

		ItemStack stack = new ItemStack(item.get(), Math.max(1, entry.count));

		// Apply potion if specified
		if (entry.potion != null && !entry.potion.isEmpty()) {
			Identifier potionId = Identifier.fromNamespaceAndPath("minecraft", entry.potion);
			Optional<Holder.Reference<Potion>> potionHolder = BuiltInRegistries.POTION.get(potionId);
			potionHolder.ifPresent(ref -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(ref)));
		}

		// Apply enchantments
		if (entry.enchantments != null && !entry.enchantments.isEmpty()) {
			for (Map.Entry<String, Integer> ench : entry.enchantments.entrySet()) {
				Identifier enchId = Identifier.fromNamespaceAndPath("minecraft", ench.getKey());
				Optional<Holder.Reference<Enchantment>> holder = enchReg.get(
					ResourceKey.create(Registries.ENCHANTMENT, enchId));
				holder.ifPresent(ref -> {
					ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
						EnchantmentHelper.getEnchantmentsForCrafting(stack));
					mutable.set(ref, ench.getValue());
					EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
				});
			}
		}

		return stack;
	}
}