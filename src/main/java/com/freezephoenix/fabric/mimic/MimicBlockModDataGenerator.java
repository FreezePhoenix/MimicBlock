package com.freezephoenix.fabric.mimic;

import com.freezephoenix.fabric.mimic.block.MimicBlock;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.BlockItemTagAppender;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;

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
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
			return new RecipeProvider(recipes, advancements) {
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
