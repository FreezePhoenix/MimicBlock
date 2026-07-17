package com.freezephoenix.fabric.mimic.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class MimicModel implements BlockStateModel {
	private final BlockStateModel fallback;
	private final ModelManager modelManager;

	public MimicModel(BlockStateModel fallback) {
		this.fallback = fallback;
		this.modelManager = Minecraft.getInstance().getModelManager();
	}

	private BlockStateModel getModel(BlockAndTintGetter level, BlockPos pos) {
		return level.getBlockEntityRenderData(pos) instanceof BlockState mimicState
		? modelManager.getBlockStateModelSet().get(mimicState)
		: fallback;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		getModel(level, pos).emitQuads(emitter, level, pos, state, random, cullTest);
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return getModel(level, pos).createGeometryKey(level, pos, state, random);
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return getModel(level, pos).particleMaterial(level, pos, state);
	}

	@Override
	@BakedQuad.MaterialFlags
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return getModel(level, pos).materialFlags(level, pos, state, random);
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {}

	@Override
	public Material.Baked particleMaterial() {
		return fallback.particleMaterial();
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags() {
		return 3;
	}

	public record Unbaked(BlockStateModel.Unbaked fallback) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BlockStateModel.Unbaked.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback)
		).apply(instance, Unbaked::new));

		@Override
		public MapCodec<Unbaked> codec() {
			return CODEC;
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
			fallback.resolveDependencies(resolver);
		}

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			return new MimicModel(fallback.bake(baker));
		}
	}
}