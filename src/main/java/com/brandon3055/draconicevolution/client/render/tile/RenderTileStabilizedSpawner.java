package com.brandon3055.draconicevolution.client.render.tile;

import com.brandon3055.draconicevolution.blocks.tileentity.StabilizedSpawnerLogic;
import com.brandon3055.draconicevolution.blocks.tileentity.TileStabilizedSpawner;
import com.brandon3055.draconicevolution.init.DEContent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Created by brandon3055 on 20/05/2016.
 */
public class RenderTileStabilizedSpawner implements BlockEntityRenderer<TileStabilizedSpawner, RenderTileStabilizedSpawner.State> {

    private static final java.util.List<java.util.function.Supplier<? extends net.minecraft.world.item.Item>> CORE_ITEMS =
            java.util.List.of(DEContent.CORE_DRACONIUM, DEContent.CORE_WYVERN, DEContent.CORE_AWAKENED, DEContent.CORE_CHAOTIC);

    private final ItemModelResolver itemModels;
    private final EntityRenderDispatcher entityRenderer;

    public RenderTileStabilizedSpawner(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
        entityRenderer = context.entityRenderer();
    }

    public static class State extends SpawnerRenderState {
        final ItemStackRenderState core = new ItemStackRenderState();
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileStabilizedSpawner tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        StabilizedSpawnerLogic logic = tile.spawnerLogic;
        Entity entity = logic.getOrCreateDisplayEntity(tile.getLevel(), tile.getBlockPos());
        state.displayEntity = null;
        if (entity != null) {
            state.displayEntity = entityRenderer.extractEntity(entity, partialTicks);
            state.displayEntity.lightCoords = state.lightCoords;
            state.spin = (float) Mth.lerp(partialTicks, logic.getOSpin(), logic.getSpin()) * 10F;
            state.scale = 0.53125F / Math.max(1F, Math.max(entity.getBbWidth(), entity.getBbHeight()));
        }
        itemModels.updateForTopItem(state.core, CORE_ITEMS.get(tile.spawnerTier.get().ordinal()).get().getDefaultInstance(), ItemDisplayContext.FIXED,
                tile.getLevel(), null, tile.posSeed());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, collector, state.displayEntity, entityRenderer, state.spin, state.scale, camera);
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 1F - (1F / 32F), 0.5);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90F));
        state.core.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
