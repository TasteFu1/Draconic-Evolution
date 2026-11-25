package com.brandon3055.draconicevolution.client.render.item;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.Map;

/** Auxiliary passes use the same transformed OBJ triangles, retaining each material's alpha mask. */
public final class ToolSurfaceRenderTypes {
    private ToolSurfaceRenderTypes() {}
    private static final RenderPipeline GLINT_PIPELINE = RenderPipelines.GLINT.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/tool_glint"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES).build();
    private static final RenderPipeline OUTLINE_PIPELINE = RenderPipelines.OUTLINE_NO_CULL.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/tool_outline"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES).build();
    public static final RenderType GLINT = RenderType.create("de_tool_glint", RenderSetup.builder(GLINT_PIPELINE)
            .withTexture("Sampler0", ItemFeatureRenderer.ENCHANTED_GLINT_ITEM)
            .setTextureTransform(TextureTransform.GLINT_TEXTURING).createRenderSetup());
    private static final Map<Identifier, RenderType> OUTLINES = new HashMap<>();
    public static RenderType outline(Identifier texture) {
        return OUTLINES.computeIfAbsent(texture, t -> RenderType.create("de_tool_outline", RenderSetup.builder(OUTLINE_PIPELINE)
                .withTexture("Sampler0", t).setOutputTarget(OutputTarget.OUTLINE_TARGET)
                .setOutline(RenderSetup.OutlineProperty.IS_OUTLINE).createRenderSetup()));
    }
    public static void registerPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GLINT_PIPELINE);
        event.registerPipeline(OUTLINE_PIPELINE);
    }
}
