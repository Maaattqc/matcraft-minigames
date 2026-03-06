package qc.mat.minigames.games.duel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import qc.mat.minigames.arena.Arena;
import qc.mat.minigames.config.ConfigManager;
import qc.mat.minigames.config.DuelConfig;
import qc.mat.minigames.config.MessagesConfig;
import qc.mat.minigames.game.GameState;
import qc.mat.minigames.game.Minigame;
import qc.mat.minigames.game.PlayerManager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DuelGame extends Minigame {
	private UUID winner;
	private UUID loser;

	public DuelGame(String gameId, Arena arena, MinecraftServer server) {
		super(gameId, arena, server);
	}

	@Override
	public String getType() { return "duel"; }

	@Override
	public int getMinPlayers() { return 2; }

	@Override
	public int getMaxPlayers() { return 2; }

	@Override
	protected void tickStarting() {
		DuelConfig config = ConfigManager.getDuelConfig();
		int countdownTicks = config.countdownSeconds * 20;
		MessagesConfig msgs = ConfigManager.getMessages();

		// Teleport and equip at tick 0
		if (tickCounter == 1) {
			teleportPlayersToSpawns();
			for (UUID uuid : players) {
				ServerPlayer p = server.getPlayerList().getPlayer(uuid);
				if (p != null) {
					DuelKit.applyKit(p, server);
					p.setGameMode(GameType.ADVENTURE);
				}
			}
		}

		// Countdown messages every second
		int ticksRemaining = countdownTicks - tickCounter;
		int secondsRemaining = ticksRemaining / 20;
		if (ticksRemaining > 0 && ticksRemaining % 20 == 0) {
			String msg = msgs.duelCountdown.replace("%seconds%", String.valueOf(secondsRemaining));
			broadcast(msg);
		}

		// Start the fight
		if (tickCounter >= countdownTicks) {
			broadcast(msgs.duelStart);
			for (UUID uuid : players) {
				ServerPlayer p = server.getPlayerList().getPlayer(uuid);
				if (p != null) {
					p.setGameMode(GameType.SURVIVAL);
				}
			}
			state = GameState.RUNNING;
			tickCounter = 0;
		}
	}

	@Override
	protected void tickRunning() {
		// Death is handled via ALLOW_DEATH event in MatMinigames
	}

	public void onPlayerKilled(UUID loserId) {
		UUID winnerId = getOtherPlayer(loserId);
		if (winnerId != null) {
			declareWinner(winnerId, loserId);
		}
	}

	@Override
	protected void tickEnding() {
		DuelConfig config = ConfigManager.getDuelConfig();
		if (tickCounter >= config.endDelaySeconds * 20) {
			for (UUID uuid : new ArrayList<>(players)) {
				ServerPlayer p = server.getPlayerList().getPlayer(uuid);
				if (p != null) {
					DuelKit.clearKit(p);
					p.setHealth(p.getMaxHealth());
					p.getFoodData().setFoodLevel(20);
					p.setGameMode(GameType.ADVENTURE);
				}
			}
			cleanup();
		}
	}

	@Override
	protected void onPlayerLeave(ServerPlayer player) {
		if (state == GameState.RUNNING) {
			UUID other = getOtherPlayer(player.getUUID());
			if (other != null) {
				MessagesConfig msgs = ConfigManager.getMessages();
				ServerPlayer otherPlayer = server.getPlayerList().getPlayer(other);
				String winnerName = otherPlayer != null ? otherPlayer.getName().getString() : "?";
				String msg = msgs.duelDisconnect.replace("%winner%", winnerName);
				broadcastToAll(msg);
				declareWinner(other, player.getUUID());
			}
		}
		DuelKit.clearKit(player);
	}

	private void declareWinner(UUID winnerId, UUID loserId) {
		this.winner = winnerId;
		this.loser = loserId;

		MessagesConfig msgs = ConfigManager.getMessages();
		ServerPlayer winnerPlayer = server.getPlayerList().getPlayer(winnerId);
		ServerPlayer loserPlayer = server.getPlayerList().getPlayer(loserId);

		String winnerName = winnerPlayer != null ? winnerPlayer.getName().getString() : "?";
		String loserName = loserPlayer != null ? loserPlayer.getName().getString() : "?";

		String msg = msgs.duelWin.replace("%winner%", winnerName).replace("%loser%", loserName);
		broadcastToAll(msg);

		end();
	}

	private void teleportPlayersToSpawns() {
		List<UUID> playerList = new ArrayList<>(players);
		List<BlockPos> spawns = arena.spawnPoints();
		ResourceKey<Level> dim = ResourceKey.create(
			net.minecraft.core.registries.Registries.DIMENSION,
			arena.dimension());
		ServerLevel level = server.getLevel(dim);
		if (level == null) level = server.overworld();

		for (int i = 0; i < Math.min(playerList.size(), spawns.size()); i++) {
			ServerPlayer p = server.getPlayerList().getPlayer(playerList.get(i));
			BlockPos pos = spawns.get(i);
			if (p != null) {
				p.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
					EnumSet.noneOf(net.minecraft.world.entity.Relative.class),
					p.getYRot(), p.getXRot(), true);
			}
		}
	}

	private UUID getOtherPlayer(UUID uuid) {
		for (UUID id : players) {
			if (!id.equals(uuid)) return id;
		}
		return null;
	}

	private void broadcastToAll(String message) {
		String prefix = ConfigManager.getMessages().prefix;
		Component component = Component.literal(prefix + message);
		for (UUID uuid : players) {
			ServerPlayer p = server.getPlayerList().getPlayer(uuid);
			if (p != null) {
				p.sendSystemMessage(component);
			}
		}
	}
}