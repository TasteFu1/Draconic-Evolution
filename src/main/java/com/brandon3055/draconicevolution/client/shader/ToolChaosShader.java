package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;
import java.util.EnumMap;
import java.util.Map;

/** Original BC chaos program with the equipment material's nonblended, depth-writing state. */
public final class ToolChaosShader extends BCShader<ToolChaosShader> {
    private final Map<VertexFormat.Mode, RenderPipeline> pipelines = new EnumMap<>(VertexFormat.Mode.class);
    public ToolChaosShader() {
        super(Identifier.fromNamespaceAndPath("brandonscore", "chaos_entity"), DefaultVertexFormat.ENTITY);
        onShaderApplied(shader -> {
            shader.getTimeUniform().glUniform1f((float) codechicken.lib.util.ClientUtils.getRenderTime());
            var player = net.minecraft.client.Minecraft.getInstance().player;
            shader.uniform(112).glUniform1f(player == null ? 0 : (float)Math.toRadians(player.getYRot()));
            shader.uniform(116).glUniform1f(player == null ? 0 : (float)-Math.toRadians(player.getXRot()));
        });
    }
    @Override public RenderPipeline pipeline(VertexFormat.Mode mode) {
        return pipelines.computeIfAbsent(mode, m -> BCShaders.CHAOS_ENTITY_SHADER.pipeline(m).toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/tool_chaos_" + m.name().toLowerCase(java.util.Locale.ROOT)))
                .withColorTargetState(ColorTargetState.DEFAULT).build());
    }
}
