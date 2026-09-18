package com.freezephoenix.fabric.mimic.block;

import com.freezephoenix.fabric.api.block.InventoryBetterBlock;
import com.freezephoenix.fabric.mimic.MimicBlockMod;
import com.freezephoenix.fabric.mimic.block.entity.MimicBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class MimicBlock extends InventoryBetterBlock<MimicBlockEntity> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath("mimic", "mimic");
	public static final BlockItemId BlockItemID = BlockItemId.create(ID, ID);

	public MimicBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getAppearance(BlockState state, BlockAndLightGetter blockAndLightGetter, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
		if (blockAndLightGetter instanceof ServerLevel serverLevel) {
			if (serverLevel.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
				return mimicBlockEntity.getVariantState();
			}
		} else {
			if (blockAndLightGetter.getBlockEntityRenderData(pos) instanceof BlockState blockState) {
				return blockState;
			}
		}
		return super.getAppearance(state, blockAndLightGetter, pos, side, sourceState, sourcePos);
	}

	@Override
	public BlockEntityType<MimicBlockEntity> getBlockEntityType() {
		return MimicBlockMod.MIMIC.entity();
	}
}