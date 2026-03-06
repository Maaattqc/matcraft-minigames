package qc.mat.minigames;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qc.mat.minigames.arena.ArenaManager;
import qc.mat.minigames.command.DuelCommand;
import qc.mat.minigames.command.MinigameCommand;
import qc.mat.minigames.config.ConfigManager;
import qc.mat.minigames.game.GameManager;
import qc.mat.minigames.game.GameState;
import qc.mat.minigames.game.Minigame;
import qc.mat.minigames.game.PlayerManager;
import qc.mat.minigames.games.duel.DuelGame;
import qc.mat.minigames.lobby.LobbyManager;
import qc.mat.minigames.network.ServerTransfer;

public class MatMinigames implements DedicatedServerModInitializer {
	public static final String MOD_ID = "matminigames";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeServer() {
		LOGGER.info("MatCraft Minigames initializing...");

		ConfigManager.loadAll();
		ArenaManager.init();
		ServerTransfer.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			MinigameCommand.register(dispatcher);
			DuelCommand.register(dispatcher);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			GameManager.getInstance().init(server);
			LobbyManager.init(server);
			LOGGER.info("MatCraft Minigames ready! Mode: {}", ConfigManager.getServerConfig().serverMode);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GameManager.getInstance().tick();
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			PlayerManager.handleDisconnect(handler.getPlayer());
		});

		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player) {
				String gameId = PlayerManager.getPlayerGame(player.getUUID());
				if (gameId != null) {
					Minigame game = GameManager.getInstance().getGame(gameId);
					if (game instanceof DuelGame duelGame && game.getState() == GameState.RUNNING) {
						player.setHealth(player.getMaxHealth());
						duelGame.onPlayerKilled(player.getUUID());
						return false;
					}
				}
			}
			return true;
		});

		LOGGER.info("MatCraft Minigames initialized.");
	}
}