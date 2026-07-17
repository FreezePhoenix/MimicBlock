package com.freezephoenix.fabric.mimic.gui;

import com.freezephoenix.fabric.mimic.MimicBlockMod;
import com.freezephoenix.fabric.mimic.MimicPayload;
import com.freezephoenix.fabric.mimic.block.entity.MimicBlockEntity;
import com.google.common.collect.Iterables;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WPlayerInvPanel;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public final class MimicMenu extends SyncedGuiDescription {
	public final WBoundSlider[] SLIDERS = new WBoundSlider[2];
	public BlockState state;
	public final BlockPos pos;
	private @Nullable Property<?> selectedProperty;

	public MimicMenu(int syncId, Inventory playerInventory, MimicBlockEntity blockEntity) {
		this(syncId, playerInventory, blockEntity, new MimicPayload(
				blockEntity.getBlockPos(),
				Blocks.AIR.defaultBlockState()
		));
	}

	public MimicMenu(int syncId, Inventory playerInventory, MimicPayload payload) {
		this(syncId, playerInventory, new SimpleContainer(1) {
			@Override
			public int getMaxStackSize() {
				return 1;
			}
		}, payload);
	}

	public MimicMenu(int syncId, Inventory playerInventory, Container inventory, MimicPayload payload) {
		super(MimicBlockMod.MIMIC_MENU_TYPE, syncId, playerInventory, inventory, null);
		pos = payload.pos();
		state = payload.state();
		WPlainPanel root = new WPlainPanel();
		root.setInsets(new Insets(2, 7, 0, 7));
		setRootPanel(root);
		setTitleVisible(true);
		root.setSize(176, 114 + 9 + 9);
		WItemSlot slot = WItemSlot.of(inventory, 0);
		root.add(slot, 0, 7 + 8);
		slot.setInputFilter((item) -> {
			Block b = Block.byItem(item.getItem());
			return b != Blocks.AIR && !(b instanceof EntityBlock);
		});
		var invLabel = WPlayerInvPanel.createInventoryLabel(playerInventory);
		invLabel.setSize(invLabel.getWidth(), invLabel.getHeight() - 1);
		root.add(invLabel, 1, 18 + 18);
		root.add(this.createPlayerInventoryPanel(false), 0, 18 + 18 + 10);
		WBoundSlider slider = SLIDERS[0] = new WBoundSlider(0, 1, Axis.HORIZONTAL) {
			@Override
			public void addTooltip(TooltipBuilder tooltip) {
				if (selectedProperty == null) {
					tooltip.add(Component.literal("property=null"));
				} else {
					tooltip.add(Component.literal("property=" + selectedProperty.getName()));
				}
			}
		};
		WBoundSlider slider2 = SLIDERS[1] = new WBoundSlider(0, 1, Axis.HORIZONTAL) {
			@Override
			public void addTooltip(TooltipBuilder tooltip) {
				if (selectedProperty == null) {
					tooltip.add(Component.literal("null=null"));
				} else {
					tooltip.add(Component.literal(selectedProperty.getName() + "=" + getValueString(state, selectedProperty)));
				}
			}
		};
		slider.setValueChangeListener((value) -> {
			selectedProperty = Iterables.get(state.getProperties(), value, null);
			if (selectedProperty == null) {
				slider2.setValues(0, 0, 0);
			} else {
				slider2.setValues(
						0,
						selectedProperty.getPossibleValues().size() - 1,
						value(state, selectedProperty)
				);
			}
		});
		slider2.setValueChangeListener((value) -> {
			if (selectedProperty != null) {
				state = with(state, selectedProperty, value);
				this.sync();
			}
		});
		{
			var properties = state.getProperties();
			if (properties.isEmpty()) {
				selectedProperty = null;
				slider.setValues(0, 0, 0);
				slider2.setValues(0, 0, 0);
			} else {
				selectedProperty = Iterables.get(properties, 0);
				slider.setValues(0, properties.size() - 1, 0);
				slider2.setValues(
						0,
						selectedProperty.getPossibleValues().size() - 1,
						value(state, selectedProperty)
				);

			}
		}

		slot.addChangeListener((ignoredSlot, ignoredInventory, _, stack) -> {
			var blockItem = Block.byItem(stack.getItem());
			if (!state.is(blockItem)) {
				state = blockItem.defaultBlockState();
				var properties = state.getProperties();
				if (properties.isEmpty()) {
					selectedProperty = null;
					slider.setValues(0, 0, 0);
					slider2.setValues(0, 0, 0);
				} else {
					selectedProperty = Iterables.get(properties, 0);
					slider.setValues(0, properties.size() - 1, 0);
					slider2.setValues(
							0,
							selectedProperty.getPossibleValues().size() - 1,
							value(state, selectedProperty)
					);
				}
			}
		});
		root.add(slider, 20, 7 + 8, 176 - 14 - 20, 8);
		root.add(slider2, 20, 7 + 8 + 2 + 8, 176 - 14 - 20, 8);
		root.validate(this);

	}

	private static <T extends Comparable<T>> String getValueString(BlockState state, Property<T> property) {
		return property.getName(state.getValue(property));
	}

	private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, int index) {
		return state.setValue(property, property.getPossibleValues().get(index));
	}

	private static <T extends Comparable<T>> int value(BlockState state, Property<T> property) {
		return property.getInternalIndex(property.value(state).value());
	}

	public void sync() {
		if (getLevel().isClientSide()) {
			this.sendAllDataToRemote();
			ClientPlayNetworking.send(new MimicPayload(pos, state));
		}
	}

	public void update(MimicPayload payload) {
		if (payload.state().is(state.getBlock())) {
			state = payload.state();
			if (selectedProperty != null) {
				SLIDERS[1].setValue(value(state, selectedProperty));
			}
		} else {
			state = payload.state();
			var properties = state.getProperties();
			if (properties.isEmpty()) {
				selectedProperty = null;
				SLIDERS[0].setValues(0, 0, 0);
				SLIDERS[1].setValues(0, 0, 0);
			} else {
				selectedProperty = Iterables.get(properties, 0);
				SLIDERS[0].setValues(0, properties.size() - 1, 0);
				SLIDERS[1].setValues(
						0,
						selectedProperty.getPossibleValues().size() - 1,
						value(state, selectedProperty)
				);
			}
		}
	}
}