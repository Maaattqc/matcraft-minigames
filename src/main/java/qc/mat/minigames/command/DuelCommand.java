package qc.mat.minigames.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import qc.mat.minigames.config.ConfigManager;
import qc.mat.minigames.config.MessagesConfig;
import qc.mat.minigames.game.GameManager;
import qc.mat.minigames.game.Minigame;
import qc.mat.minigames.game.PlayerManager;
import qc.mat.minigames.games.duel.DuelGame;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelCommand {
	private static final Map<UUID, DuelChallenge> pendingChallenges = new ConcurrentHashMap<>();

	public record DuelChallenge(UUID challenger, UUID target, long expiresAtTick) {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("duel")
			.then(Commands.argument("player", EntityArgument.player())
				.executes(ctx -> challengePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))
			.then(Commands.literal("accept").executes(ctx -> acceptChallenge(ctx.getSource())))
			.then(Commands.literal("deny").executes(ctx -> denyChallenge(ctx.getSource())))
		);
	}

	private static int challengePlayer(CommandSourceStack source, ServerPlayer target) {
		String prefix = ConfigManager.getMessages().prefix;
		MessagesConfig msgs = ConfigManager.getMessages();
		if (!(source.getEntity() instanceof ServerPlayer challenger)) return 0;

		if (challenger.getUUID().equals(target.getUUID())) {
			source.sendFailure(Component.literal(prefix + "Vous ne pouvez pas vous defier vous-meme."));
			return 0;
		}

		if (PlayerManager.isInGame(challenger.getUUID())) {
			source.sendFailure(Component.literal(prefix + msgs.alreadyInGame));
			return 0;
		}

		if (PlayerManager.isInGame(target.getUUID())) {
			source.sendFailure(Component.literal(prefix + target.getName().getString() + " est deja en partie."));
			return 0;
		}

		// Check if target already has a pending challenge
		if (pendingChallenges.containsKey(target.getUUID())) {
			source.sendFailure(Component.literal(prefix + target.getName().getString() + " a deja un defi en attente."));
			return 0;
		}

		int timeoutTicks = ConfigManager.getDuelConfig().challengeTimeoutSeconds * 20;
		long expiry = source.getServer().getTickCount() + timeoutTicks;
		pendingChallenges.put(target.getUUID(), new DuelChallenge(challenger.getUUID(), target.getUUID(), expiry));

		String challengeMsg = msgs.duelChallenge.replace("%challenger%", challenger.getName().getString());
		target.sendSystemMessage(Component.literal(prefix + challengeMsg));
		source.sendSuccess(() -> Component.literal(prefix + "Defi envoye a " + target.getName().getString() + "!"), false);
		return 1;
	}

	private static int acceptChallenge(CommandSourceStack source) {
		String prefix = ConfigManager.getMessages().prefix;
		MessagesConfig msgs = ConfigManager.getMessages();
		if (!(source.getEntity() instanceof ServerPlayer player)) return 0;

		DuelChallenge challenge = pendingChallenges.remove(player.getUUID());
		if (challenge == null) {
			source.sendFailure(Component.literal(prefix + "Vous n'avez aucun defi en attente."));
			return 0;
		}

		if (source.getServer().getTickCount() > challenge.expiresAtTick()) {
			source.sendFailure(Component.literal(prefix + msgs.duelTimeout));
			return 0;
		}

		ServerPlayer challenger = source.getServer().getPlayerList().getPlayer(challenge.challenger());
		if (challenger == null) {
			source.sendFailure(Component.literal(prefix + "Le joueur n'est plus en ligne."));
			return 0;
		}

		if (PlayerManager.isInGame(challenger.getUUID())) {
			source.sendFailure(Component.literal(prefix + msgs.alreadyInGame));
			return 0;
		}

		Minigame game = GameManager.getInstance().findOrCreateGame("duel");
		if (game == null) {
			source.sendFailure(Component.literal(prefix + "Aucune arene disponible."));
			return 0;
		}

		game.addPlayer(challenger);
		game.addPlayer(player);
		game.start();

		String acceptMsg = msgs.duelAccepted
			.replace("%countdown%", String.valueOf(ConfigManager.getDuelConfig().countdownSeconds));
		challenger.sendSystemMessage(Component.literal(prefix + acceptMsg));
		player.sendSystemMessage(Component.literal(prefix + acceptMsg));
		return 1;
	}

	private static int denyChallenge(CommandSourceStack source) {
		String prefix = ConfigManager.getMessages().prefix;
		MessagesConfig msgs = ConfigManager.getMessages();
		if (!(source.getEntity() instanceof ServerPlayer player)) return 0;

		DuelChallenge challenge = pendingChallenges.remove(player.getUUID());
		if (challenge == null) {
			source.sendFailure(Component.literal(prefix + "Vous n'avez aucun defi en attente."));
			return 0;
		}

		ServerPlayer challenger = source.getServer().getPlayerList().getPlayer(challenge.challenger());
		if (challenger != null) {
			challenger.sendSystemMessage(Component.literal(prefix + msgs.duelDenied));
		}
		source.sendSuccess(() -> Component.literal(prefix + msgs.duelDenied), false);
		return 1;
	}

	public static void cleanupExpired(long currentTick) {
		pendingChallenges.entrySet().removeIf(entry -> currentTick > entry.getValue().expiresAtTick());
	}
}