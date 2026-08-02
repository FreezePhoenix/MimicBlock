package com.freezephoenix.fabric.mimic.client;

import com.freezephoenix.fabric.client.FreezeLibClient;
import com.freezephoenix.fabric.mimic.MimicBlockMod;
import com.freezephoenix.fabric.mimic.MimicPayload;
import com.freezephoenix.fabric.mimic.client.model.MimicModel;
import com.freezephoenix.fabric.mimic.gui.MimicMenu;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MimicBlockModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CustomUnbakedBlockStateModel.register(MimicBlockMod.id("mimic_model"), MimicModel.Unbaked.CODEC);
		BlockColorRegistry.register(
				(BlockState _, BlockAndTintGetter level, BlockPos pos, IntList tintValues) -> {
					if (level.getBlockEntityRenderData(pos) instanceof BlockState mimicState) {
						var factory = BlockColorRegistry.getFactory(mimicState);
						if (factory != null) {
							factory.collect(mimicState, level, pos, tintValues);
						} else {
							var tintSources = Minecraft.getInstance().getBlockColors().getTintSources(mimicState);
							tintValues.size(tintSources.size());
							for (int i = 0; i < tintSources.size(); i++) {
								tintValues.set(i, tintSources.get(i).colorInWorld(mimicState, level, pos));
							}
						}

					}
				}, MimicBlockMod.MIMIC.block()
		);
		ClientPlayNetworking.registerGlobalReceiver(
				MimicPayload.TYPE,
				(MimicPayload payload, ClientPlayNetworking.Context context) -> {
					context.client().execute(() -> {
						if (context.player().containerMenu instanceof MimicMenu roomControllerScreenHandler) {
							roomControllerScreenHandler.update(payload);
						}
					});
				}
		);
		FreezeLibClient.registerMenu(MimicBlockMod.MIMIC_MENU_TYPE);
	}
}
