package com.brandon3055.draconicevolution.client.render.item;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.init.DEContent;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * Created by brandon3055 on 27/2/20.
 */
public class RenderItemChaosShard implements DEItemRenderer {

    private static final RenderType CHAOS_CRYSTAL_INNER = BCShaders.CHAOS_ENTITY_SHADER.renderType(
            MODID + ":chaos_crystal_inner", Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/chaos_shader.png"), VertexFormat.Mode.TRIANGLES);
    private static final RenderType CHAOS_CRYSTAL = RenderType.create(MODID + ":chaos_crystal",
            net.minecraft.client.renderer.rendertype.RenderSetup.builder(
                    com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath(MODID, "pipeline/chaos_crystal_shell"))
                            .withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("PER_FACE_LIGHTING")
                            .withSampler("Sampler1").withCull(false)
                            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.TRIANGLES)
                            .withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.TRANSLUCENT))
                            .build())
                    .withTexture("Sampler0", Identifier.fromNamespaceAndPath(MODID, "textures/block/chaos_crystal.png"))
                    .useLightmap().useOverlay().createRenderSetup());
    public static void registerPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(CHAOS_CRYSTAL.pipeline());
    }

    private final CCModel shard;
    private final Item item;

    public RenderItemChaosShard() { this(null); }

    public RenderItemChaosShard(Item item) {
        this.item = item;
        Map<String, CCModel> map = new OBJParser(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "models/item/chaos_shard.obj"))
                .ignoreMtl()
                .parse();
        shard = CCModel.combine(map.values())
                .backfacedCopy()
                .computeNormals();
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack mStack, MultiBufferSource getter, int packedLight, int packedOverlay) {
        Matrix4 mat = new Matrix4(mStack);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        Item renderedItem = item == null ? stack.getItem() : item;
        mat.apply(new Scale(renderedItem == DEContent.CHAOS_SHARD.get() ? 1 : renderedItem == DEContent.CHAOS_FRAG_LARGE.get() ? 0.75 : renderedItem == DEContent.CHAOS_FRAG_MEDIUM.get() ? 0.5 : 0.25).at(new Vector3(0.5, 0.5, 0.5)));

        BCShaders.CHAOS_ENTITY_SHADER.getModelMatUniform().glUniformMatrix4f(new Matrix4());
        BCShaders.CHAOS_ENTITY_SHADER.getAlphaUniform().glUniform1f(1F);
        BCShaders.CHAOS_ENTITY_SHADER.getSimpleLightUniform().glUniform1i(1);
        BCShaders.CHAOS_ENTITY_SHADER.getDisableLightUniform().glUniform1i(0);
        BCShaders.CHAOS_ENTITY_SHADER.getDisableOverlayUniform().glUniform1i(0);

        ccrs.bind(CHAOS_CRYSTAL_INNER, getter);
        com.brandon3055.draconicevolution.client.render.DEModelEmission.render(shard, ccrs, mat);

        ccrs.baseColour = 0xFFFFFFF0;
        ccrs.bind(CHAOS_CRYSTAL, getter);
        mat.apply(new Scale(1.005).at(new Vector3(0.5, 0.5, 0.5)));
        com.brandon3055.draconicevolution.client.render.DEModelEmission.render(shard, ccrs, mat);
    }

    @Override
    public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
        // Include the 1.005 shell expansion; the smaller fragments fit these full-shard bounds.
        for (var vertex : shard.verts) {
            var v = vertex.vec;
            output.accept(new org.joml.Vector3f((float) ((v.x - 0.5) * 1.005 + 0.5),
                    (float) ((v.y - 0.5) * 1.005 + 0.5), (float) ((v.z - 0.5) * 1.005 + 0.5)));
        }
    }

    //This is not cursed at all! idk what your talking about!
    public static class CHAOS_SHARD extends RenderItemChaosShard { public CHAOS_SHARD() {super(DEContent.CHAOS_SHARD.get());}}
    public static class CHAOS_FRAG_LARGE extends RenderItemChaosShard { public CHAOS_FRAG_LARGE() {super(DEContent.CHAOS_FRAG_LARGE.get());}}
    public static class CHAOS_FRAG_MEDIUM extends RenderItemChaosShard { public CHAOS_FRAG_MEDIUM() {super(DEContent.CHAOS_FRAG_MEDIUM.get());}}
    public static class CHAOS_FRAG_SMALL extends RenderItemChaosShard { public CHAOS_FRAG_SMALL() {super(DEContent.CHAOS_FRAG_SMALL.get());}}
    // @formatter:on
}
