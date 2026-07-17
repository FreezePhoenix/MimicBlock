package com.freezephoenix.fabric.mimic.block.entity;

import com.freezephoenix.fabric.api.block.entity.InventoryBetterBlockEntity;
import com.freezephoenix.fabric.mimic.MimicBlockMod;
import com.freezephoenix.fabric.mimic.MimicPayload;
import com.freezephoenix.fabric.mimic.gui.MimicMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class MimicBlockEntity extends InventoryBetterBlockEntity<MimicBlockEntity> implements ExtendedMenuProvider<MimicPayload> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath("mimic", "mimic");
	private BlockState variantState = Blocks.AIR.defaultBlockState();

	public MimicBlockEntity(BlockPos pos, BlockState state) {
		super(MimicBlockMod.MIMIC.entity(), pos, state, 1);
	}

	public boolean disguised() {
		return !this.getVariantState().isAir();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public @Nullable Object getRenderData() {
		return this.disguised() ? this.getVariantState() : null;
	}

	public BlockState getVariantState() {
		return variantState;
	}

	public void setVariantState(BlockState state) {
		this.variantState = state;
		this.setChanged();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack item) {
		return item.getItem() instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof EntityBlock) && super.canPlaceItem(
				slot,
				item
		);
	}


	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveCustomOnly(registries);
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		view.store("state", BlockState.CODEC, this.getVariantState());
	}

	@Override
	public void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		view.read("state", BlockState.CODEC).ifPresent(this::setVariantState);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatableWithFallback("block.mimic.mimic", "Mimic Block");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new MimicMenu(syncId, inventory, this);
	}

	@Override
	public void setChanged() {
		var block = Block.byItem(getItem(0).getItem());
		if (!variantState.is(block)) {
			this.setVariantState(block.defaultBlockState());
		}
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),0);
		}
		super.setChanged();
	}

	@Override
	public MimicPayload getScreenOpeningData(ServerPlayer player) {
		return new MimicPayload(this.getBlockPos(), this.getVariantState());
	}
}
