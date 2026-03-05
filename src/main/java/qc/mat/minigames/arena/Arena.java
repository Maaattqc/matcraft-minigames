package qc.mat.minigames.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.List;

public record Arena(
	String id,
	String name,
	String gameType,
	Identifier dimension,
	List<BlockPos> spawnPoints
) {}