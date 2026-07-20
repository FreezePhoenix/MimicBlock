package com.freezephoenix.fabric.mimic;

import com.freezephoenix.fabric.FreezeLib;
import com.freezephoenix.fabric.gui.BetterScreen;
import com.freezephoenix.fabric.mimic.block.MimicBlock;
import com.freezephoenix.fabric.mimic.block.entity.MimicBlockEntity;
import com.freezephoenix.fabric.mimic.gui.MimicMenu;
import com.freezephoenix.fabric.mimic.model.MimicModel;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MimicBlockMod implements ModInitializer, ClientModInitializer {
	public static final String MOD_ID = "mimic";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static FreezeLib.BetterBlockEntity<MimicBlock, MimicBlockEntity> MIMIC;
	public static MenuType<MimicMenu> MIMIC_MENU_TYPE;

	public static final FreezeLib.FreezeTab ITEM_GROUP = FreezeLib.registerCreativeTab("mimic",() -> MIMIC.block());


	@Override
	public void onInitialize() {
		MimicPayload.register();

		MIMIC = FreezeLib.registerBlockEntity(MimicBlock.BlockItemID, MimicBlock::new, Blocks.COBWEB, MimicBlockEntity.ID, MimicBlockEntity::new);
		MIMIC_MENU_TYPE = registerExtendedMenu(MimicBlock.ID,
											   MimicMenu::new,
											   MimicPayload.CODEC
		);
	}

	public static <T extends AbstractContainerMenu, D> MenuType<T> registerExtendedMenu(Identifier ID, ExtendedMenuType.ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
		return Registry.register(BuiltInRegistries.MENU, ID, new ExtendedMenuType<>(factory, codec));
	}

	@Environment(EnvType.CLIENT)
	private static <T extends SyncedGuiDescription> void registerMenu(MenuType<T> screenHandlerType) {
		MenuScreens.register(screenHandlerType, BetterScreen<T>::new);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onInitializeClient() {
		CustomUnbakedBlockStateModel.register(MimicBlockMod.id("mimic_model"), MimicModel.Unbaked.CODEC);
		BlockColorRegistry.register((BlockState _, BlockAndTintGetter level, BlockPos pos, IntList tintValues) -> {
			if (level.getBlockEntityRenderData(pos) instanceof BlockState mimicState) {
				var factory = BlockColorRegistry.getFactory(mimicState);
				if(factory != null) {
					factory.collect(mimicState,level,pos,tintValues);
				} else {
					var tintSources = Minecraft.getInstance().getBlockColors().getTintSources(mimicState);
					tintValues.size(tintSources.size());
					for(int i = 0; i < tintSources.size(); i++) {
						tintValues.set(i, tintSources.get(i).colorInWorld(mimicState,level,pos));
					}
				}

			}
		}, MIMIC.block());
		MimicPayload.registerClient();
		registerMenu(MIMIC_MENU_TYPE);
		ITEM_GROUP.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
