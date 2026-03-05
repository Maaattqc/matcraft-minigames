package qc.mat.minigames.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import qc.mat.minigames.MatMinigames;

public class ServerTransfer {
	private static final Identifier BUNGEECORD_CHANNEL = Identifier.fromNamespaceAndPath("bungeecord", "main");

	public static final CustomPacketPayload.Type<BungeeCordPayload> TYPE =
		new CustomPacketPayload.Type<>(BUNGEECORD_CHANNEL);

	public static final StreamCodec<FriendlyByteBuf, BungeeCordPayload> STREAM_CODEC =
		StreamCodec.of(
			(buf, payload) -> buf.writeBytes(payload.data()),
			buf -> {
				byte[] data = new byte[buf.readableBytes()];
				buf.readBytes(data);
				return new BungeeCordPayload(data);
			}
		);

	public record BungeeCordPayload(byte[] data) implements CustomPacketPayload {
		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void init() {
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
			.register(TYPE, STREAM_CODEC);
		MatMinigames.LOGGER.info("BungeeCord plugin messaging channel registered");
	}

	public static void transferPlayer(ServerPlayer player, String serverName) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeUtf("Connect");
		buf.writeUtf(serverName);
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		buf.release();

		try {
			ServerPlayNetworking.send(player, new BungeeCordPayload(data));
			MatMinigames.LOGGER.info("Transferring {} to server {}", player.getName().getString(), serverName);
		} catch (Exception e) {
			MatMinigames.LOGGER.error("Failed to transfer {}: {}", player.getName().getString(), e.getMessage());
		}
	}
}