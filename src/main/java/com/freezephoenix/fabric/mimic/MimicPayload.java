package com.freezephoenix.fabric.mimic;

import com.freezephoenix.fabric.mimic.block.entity.MimicBlockEntity;
import com.freezephoenix.fabric.mimic.gui.MimicMenu;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record MimicPayload(BlockPos pos, BlockState state) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MimicPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
			MimicBlockMod.MOD_ID,
			"mimic"
	));
	public static final StreamCodec<ByteBuf, MimicPayload> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC,
																						 MimicPayload::pos,
																						 ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), MimicPayload::state,
																						 MimicPayload::new
	);
	@Override
	public Type<MimicPayload> type() {
		return TYPE;
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
				MimicPayload.TYPE,
				MimicPayload::handle
		);
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(
				MimicPayload.TYPE,
				MimicPayload::handle
		);
	}

	@Environment(EnvType.CLIENT)
	private static void handle(MimicPayload payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (context.player().containerMenu instanceof MimicMenu roomControllerScreenHandler) {
				roomControllerScreenHandler.update(payload);
			}
		});
	}
	private static void handle(MimicPayload payload, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			BlockState state = payload.state();
			ServerLevel world = context.player().level();
			if (world.getBlockEntity(payload.pos()) instanceof MimicBlockEntity roomController) {
				roomController.setVariantState(state);
				for (ServerPlayer player : PlayerLookup.tracking(roomController)) {
					if (!player.equals(context.player()) && player.containerMenu instanceof MimicMenu roomControllerScreenHandler) {
						if (roomControllerScreenHandler.pos.equals(payload.pos())) {
							ServerPlayNetworking.send(player, payload);
						}
					}
				}
			}
		});
	}
}
