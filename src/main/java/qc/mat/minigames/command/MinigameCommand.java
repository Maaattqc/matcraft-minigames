package qc.mat.minigames.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import qc.mat.minigames.config.ConfigManager;
import qc.mat.minigames.game.GameManager;
import qc.mat.minigames.game.GameState;
import qc.mat.minigames.game.Minigame;
import qc.mat.minigames.game.PlayerManager;

public class MinigameCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("minigame")
			.then(Commands.literal("list").executes(ctx -> listGames(ctx.getSource())))
			.then(Commands.literal("join")
				.then(Commands.argument("gameId", StringArgumentType.word())
					.executes(ctx -> joinGame(ctx.getSource(), StringArgumentType.getString(ctx, "gameId")))))
			.then(Commands.literal("leave").executes(ctx -> leaveGame(ctx.getSource())))
		);
	}

	private static int listGames(CommandSourceStack source) {
		String prefix = ConfigManager.getMessages().prefix;
		var games = GameManager.getInstance().getActiveGames();
		if (games.isEmpty()) {
			source.sendSuccess(() -> Component.literal(prefix + "Aucune partie active."), false);
			return 1;
		}
		source.sendSuccess(() -> Component.literal(prefix + "Parties actives:"), false);
		for (Minigame game : games) {
			String info = String.format("  %s [%s] - %d/%d joueurs - %s",
				game.getGameId(), game.getType(),
				game.getPlayers().size(), game.getMaxPlayers(),
				game.getState().name());
			source.sendSuccess(() -> Component.literal(info), false);
		}
		return 1;
	}

	private static int joinGame(CommandSourceStack source, String gameId) {
		String prefix = ConfigManager.getMessages().prefix;
		if (!(source.getEntity() instanceof ServerPlayer player)) return 0;

		if (PlayerManager.isInGame(player.getUUID())) {
			source.sendFailure(Component.literal(prefix + ConfigManager.getMessages().alreadyInGame));
			return 0;
		}

		Minigame game = GameManager.getInstance().getGame(gameId);
		if (game == null) {
			source.sendFailure(Component.literal(prefix + ConfigManager.getMessages().gameNotFound));
			return 0;
		}

		if (game.isFull()) {
			source.sendFailure(Component.literal(prefix + ConfigManager.getMessages().gameFull));
			return 0;
		}

		if (game.getState() != GameState.WAITING) {
			source.sendFailure(Component.literal(prefix + "Cette partie a deja commence."));
			return 0;
		}

		game.addPlayer(player);
		return 1;
	}

	private static int leaveGame(CommandSourceStack source) {
		String prefix = ConfigManager.getMessages().prefix;
		if (!(source.getEntity() instanceof ServerPlayer player)) return 0;

		if (!PlayerManager.isInGame(player.getUUID())) {
			source.sendFailure(Component.literal(prefix + ConfigManager.getMessages().notInGame));
			return 0;
		}

		String gameId = PlayerManager.getPlayerGame(player.getUUID());
		Minigame game = GameManager.getInstance().getGame(gameId);
		if (game != null) {
			game.removePlayer(player);
		}
		source.sendSuccess(() -> Component.literal(prefix + ConfigManager.getMessages().gameLeft), false);
		return 1;
	}
}