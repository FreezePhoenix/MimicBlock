package com.freezephoenix.fabric.mimic;

import com.freezephoenix.fabric.mimic.block.MimicBlock;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.BlockItemTagAppender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypeIds;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MimicBlockModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(LootTableGenerator::new);
		pack.addProvider(MimicBlockModRecipeGenerator::new);
		pack.addProvider(BlockTagGenerator::new);
	}

	private static class BlockTagGenerator extends FabricTagsProvider.BlockTagsProvider {
		public BlockTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		protected BlockItemTagAppender<Block> tag(final TagKey<Block> tag) {
			return new BlockItemTagAppender<>(super.tag(tag)) {
				protected ResourceKey<Block> convertElement(final BlockItemId element) {
					return element.block();
				}
			};
		}

		@Override
		protected void addTags(HolderLookup.Provider arg) {
			tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
					MimicBlock.BlockItemID
			);
		}
	}

	private static class LootTableGenerator extends FabricBlockLootSubProvider {
		protected LootTableGenerator(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(dataOutput, registriesFuture);
		}

		@Override
		public void generate() {
			add(MimicBlockMod.MIMIC.block(), this::createNameableBlockEntityTable);
		}
	}
	private static class MimicBlockModRecipeGenerator extends FabricRecipeProvider {
		public MimicBlockModRecipeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
			return new RecipeProvider(registryLookup, exporter) {
				@Override
				public void buildRecipes() {
					shaped(RecipeCategory.REDSTONE, MimicBlockMod.MIMIC.block())
							.pattern("CCC")
							.pattern("COC")
							.pattern("CCC")
							.define('C', Items.CINNABAR)
							.define('O', Items.OBSERVER)
							.unlockedBy(
									getHasName(Items.OBSERVER),
									has(Items.OBSERVER)
							)
							.save(output);

				}
			};
		}

		@Override
		public String getName() {
			return "MimicBlockMod";
		}
	}
}
