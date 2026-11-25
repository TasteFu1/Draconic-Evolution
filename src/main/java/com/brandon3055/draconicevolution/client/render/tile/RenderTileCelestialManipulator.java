package com.brandon3055.draconicevolution.client.render.tile;

import com.brandon3055.draconicevolution.blocks.tileentity.TileCelestialManipulator;
import com.brandon3055.draconicevolution.client.render.effect.EffectTrackerCelestialManipulator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.AABB;

public class RenderTileCelestialManipulator implements BlockEntityRenderer<TileCelestialManipulator, RenderTileCelestialManipulator.State> {
    public RenderTileCelestialManipulator(BlockEntityRendererProvider.Context context) {}

    public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        com.brandon3055.brandonscore.client.render.ExtractedModel effects;
    }
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileCelestialManipulator tile, State state, float partialTicks, net.minecraft.world.phys.Vec3 cameraPosition,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        var capture = new com.brandon3055.brandonscore.client.render.ExtractedModel.Capture();
        tile.clientEffects().extractEffects(partialTicks, new PoseStack(), capture);
        state.effects = capture.finish();
    }
    @Override
    public void submit(State state, PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
                       net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        state.effects.submit(pose, collector);
    }
    @Override public boolean shouldRenderOffScreen() { return true; }
    @Override
    public AABB getRenderBoundingBox(TileCelestialManipulator tile) {
        BlockPos pos = tile.getBlockPos();
        return new AABB(pos.getX() - 4, pos.getY() - 4, pos.getZ() - 4, pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5);
    }
}
