package com.freezephoenix.fabric.mimic;

import com.freezephoenix.fabric.FreezeLib;
import com.freezephoenix.fabric.mimic.block.MimicBlock;
import com.freezephoenix.fabric.mimic.block.entity.MimicBlockEntity;
import com.freezephoenix.fabric.mimic.gui.MimicMenu;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MimicBlockMod implements ModInitializer {
	public static final String MOD_ID = "mimic";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static FreezeLib.BetterBlockEntity<MimicBlock, MimicBlockEntity> MIMIC;
	public static MenuType<MimicMenu> MIMIC_MENU_TYPE;

	public static final FreezeLib.FreezeTab ITEM_GROUP = FreezeLib.registerCreativeTab("mimic",() -> MIMIC.block());


	@Override
	public void onInitialize() {
		MimicPayload.register();

		MIMIC = FreezeLib.registerBlockEntity(MimicBlock.BlockItemID, MimicBlock::new, Blocks.COBWEB, MimicBlockEntity.ID, MimicBlockEntity::new);
		MIMIC_MENU_TYPE = FreezeLib.registerExtendedMenu(MimicBlock.ID,
											   MimicMenu::new,
											   MimicPayload.CODEC
		);
		MimicBlockMod.ITEM_GROUP.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
