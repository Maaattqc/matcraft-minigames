package qc.mat.minigames.game;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import qc.mat.minigames.arena.Arena;
import qc.mat.minigames.config.ConfigManager;

import java.util.*;

public abstract class Minigame {
	protected final String gameId;
	protected GameState state = GameState.WAITING;
	protected final Set<UUID> players = new LinkedHashSet<>();
	protected Arena arena;
	protected int tickCounter = 0;
	protected MinecraftServer server;

	public Minigame(String gameId, Arena arena, MinecraftServer server) {
		this.gameId = gameId;
		this.arena = arena;
		this.server = server;
	}

	public abstract String getType();
	public abstract int getMinPlayers();
	public abstract int getMaxPlayers();
	protected abstract void tickRunning();

	public void tick() {
		tickCounter++;
		switch (state) {
			case WAITING -> tickWaiting();
			case STARTING -> tickStarting();
			case RUNNING -> tickRunning();
			case ENDING -> tickEnding();
		}
	}

	protected void tickWaiting() {}
	protected void tickStarting() {}
	protected void tickEnding() {}

	public void setup() {
		state = GameState.WAITING;
		tickCounter = 0;
	}

	public void start() {
		state = GameState.STARTING;
		tickCounter = 0;
	}

	public void end() {
		state = GameState.ENDING;
		tickCounter = 0;
	}

	public void cleanup() {
		for (UUID uuid : new ArrayList<>(players)) {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			if (player != null) {
				onPlayerLeave(player);
			}
			PlayerManager.removePlayer(uuid);
		}
		players.clear();
	}

	public boolean addPlayer(ServerPlayer player) {
		if (players.size() >= getMaxPlayers()) return false;
		if (state != GameState.WAITING) return false;
		players.add(player.getUUID());
		PlayerManager.assignPlayer(player.getUUID(), gameId);
		onPlayerJoin(player);
		return true;
	}

	public void removePlayer(ServerPlayer player) {
		if (!players.contains(player.getUUID())) return;
		players.remove(player.getUUID());
		PlayerManager.removePlayer(player.getUUID());
		onPlayerLeave(player);
	}

	protected void onPlayerJoin(ServerPlayer player) {}
	protected void onPlayerLeave(ServerPlayer player) {}

	public void broadcast(String message) {
		String prefix = ConfigManager.getMessages().prefix;
		Component component = Component.literal(prefix + message);
		for (UUID uuid : players) {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			if (player != null) {
				player.sendSystemMessage(component);
			}
		}
	}

	public String getGameId() { return gameId; }
	public GameState getState() { return state; }
	public Set<UUID> getPlayers() { return Collections.unmodifiableSet(players); }
	public Arena getArena() { return arena; }
	public MinecraftServer getServer() { return server; }
	public boolean isFull() { return players.size() >= getMaxPlayers(); }
	public boolean isEmpty() { return players.isEmpty(); }
}