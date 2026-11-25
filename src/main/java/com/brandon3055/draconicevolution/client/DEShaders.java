package com.brandon3055.draconicevolution.client;

import codechicken.lib.util.ClientUtils;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.draconicevolution.client.shader.ShieldShader;
import com.brandon3055.draconicevolution.client.shader.ToolShader;
import com.brandon3055.draconicevolution.client.shader.DEEffectShader;
import com.brandon3055.draconicevolution.client.shader.EffectUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/** Created by brandon3055 on 15/05/2022.
 * Original DE programs registered as MC26 pipelines; legacy renderer tails migrate separately. */
public class DEShaders {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final com.brandon3055.draconicevolution.client.shader.ToolChaosShader TOOL_CHAOS_SHADER =
            new com.brandon3055.draconicevolution.client.shader.ToolChaosShader();
    public static final ToolShader TOOL_BASE_SHADER = new ToolShader("tools/tool_base", DefaultVertexFormat.ENTITY);
    public static final ToolShader TOOL_GEM_SHADER = new ToolShader("tools/tool_gem", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader TOOL_TRACE_SHADER = new ToolShader("tools/tool_trace", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader TOOL_BLADE_SHADER = new ToolShader("tools/tool_blade", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader BOW_STRING_SHADER = new ToolShader("tools/bow_string", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader CHESTPIECE_GEM_SHADER = new ToolShader("tools/chestpiece_gem", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ShieldShader CHESTPIECE_SHIELD_SHADER = new ShieldShader("tools/chestpiece_shield", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final com.brandon3055.draconicevolution.client.shader.EnergyCrystalShader ENERGY_CRYSTAL =
            new com.brandon3055.draconicevolution.client.shader.EnergyCrystalShader()
                    .onShaderApplied(shader -> shader.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final DEEffectShader reactorShader = new DEEffectShader("reactor", "reactor", DefaultVertexFormat.POSITION_TEX, false, true, true, false, false, false);
    public static final DEEffectShader reactorShieldShader = new DEEffectShader("reactor", "reactor_shield", DefaultVertexFormat.POSITION_TEX, true, true, true, false, false, false);
    public static final DEEffectShader reactorBeamShader = new DEEffectShader("reactor_beam", "reactor_beam", DefaultVertexFormat.POSITION_TEX_COLOR, true, false, false, false, false, false);
    public static final DEEffectShader chaosBlockShader = new DEEffectShader("chaos_block", "chaos_block", DefaultVertexFormat.BLOCK, true, false, true, true, true, true);
    public static final DEEffectShader shieldShader = new DEEffectShader("shield", "shield", DefaultVertexFormat.POSITION_TEX, true, false, true, false, false, false);
    public static final DEEffectShader energyCoreShader = new DEEffectShader("energy_core", "energy_core", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, true, false, true, false, false, false);
    public static final DEEffectShader explosionShader = new DEEffectShader("explosion", "explosion", DefaultVertexFormat.POSITION_TEX, true, false, false, false, false, false, com.mojang.blaze3d.platform.CompareOp.ALWAYS_PASS);
    public static final DEEffectShader explosionFlashShader = new DEEffectShader("explosion_flash", "explosion_flash", DefaultVertexFormat.POSITION_COLOR, true, false, true, false, false, false);

    public static final EffectUniform reactorTime = new EffectUniform(reactorShader, 96, 1, false);
    public static final EffectUniform reactorIntensity = new EffectUniform(reactorShader, 100, 1, false);
    public static final EffectUniform reactorShieldTime = new EffectUniform(reactorShieldShader, 96, 1, false);
    public static final EffectUniform reactorShieldIntensity = new EffectUniform(reactorShieldShader, 100, 1, false);
    public static final EffectUniform reactorBeamTime = new EffectUniform(reactorBeamShader, 96, 1, false);
    public static final EffectUniform reactorBeamFade = new EffectUniform(reactorBeamShader, 100, 1, false);
    public static final EffectUniform reactorBeamPower = new EffectUniform(reactorBeamShader, 104, 1, false);
    public static final EffectUniform reactorBeamStartup = new EffectUniform(reactorBeamShader, 108, 1, false);
    public static final EffectUniform reactorBeamType = new EffectUniform(reactorBeamShader, 124, 1, true);
    public static final EffectUniform chaosBlockTime = new EffectUniform(chaosBlockShader, 96, 1, false);
    public static final EffectUniform chaosBlockYaw = new EffectUniform(chaosBlockShader, 112, 1, false);
    public static final EffectUniform chaosBlockPitch = new EffectUniform(chaosBlockShader, 116, 1, false);
    public static final EffectUniform chaosBlockAlpha = new EffectUniform(chaosBlockShader, 120, 1, false);
    public static final EffectUniform shieldTime = new EffectUniform(shieldShader, 96, 1, false);
    public static final EffectUniform shieldActivation = new EffectUniform(shieldShader, 100, 1, false);
    public static final EffectUniform shieldColour = new EffectUniform(shieldShader, 64, 4, false);
    public static final EffectUniform shieldBarMode = new EffectUniform(shieldShader, 128, 1, true);
    public static final EffectUniform energyCoreTime = new EffectUniform(energyCoreShader, 96, 1, false);
    public static final EffectUniform energyCoreActivation = new EffectUniform(energyCoreShader, 100, 1, false);
    public static final EffectUniform energyCoreEffectColour = new EffectUniform(energyCoreShader, 80, 3, false);
    public static final EffectUniform energyCoreFrameColour = new EffectUniform(energyCoreShader, 144, 3, false);
    public static final EffectUniform energyCoreRotTriColour = new EffectUniform(energyCoreShader, 160, 3, false);
    public static final EffectUniform explosionTime = new EffectUniform(explosionShader, 96, 1, false);
    public static final EffectUniform explosionScale = new EffectUniform(explosionShader, 132, 1, false);
    public static final EffectUniform explosionAlpha = new EffectUniform(explosionShader, 120, 1, false);
    public static final EffectUniform explosionType = new EffectUniform(explosionShader, 124, 1, true);
    public static final EffectUniform explosionFlashScreenPos = new EffectUniform(explosionFlashShader, 80, 2, false);
    public static final EffectUniform explosionFlashScreenSize = new EffectUniform(explosionFlashShader, 88, 2, false);
    public static final EffectUniform explosionFlashIntensity = new EffectUniform(explosionFlashShader, 136, 1, false);

    public static final DEEffectShader REACTOR_GUI = new DEEffectShader("reactor", "reactor", DefaultVertexFormat.POSITION_TEX, false, true, false, false, false, false);
    public static final DEEffectShader REACTOR_SHIELD_GUI = new DEEffectShader("reactor", "reactor_shield", DefaultVertexFormat.POSITION_TEX, true, true, false, false, false, false);

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) ->
            event.register(com.brandon3055.draconicevolution.client.shader.ReactorGuiState.class,
                com.brandon3055.draconicevolution.client.shader.ReactorGuiRenderer::new));
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) ->
                event.register(com.brandon3055.draconicevolution.client.shader.GuardianOverlayRenderer.State.class,
                        com.brandon3055.draconicevolution.client.shader.GuardianOverlayRenderer::new));
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) ->
                event.register(com.brandon3055.draconicevolution.client.shader.ExplosionFlashRenderer.State.class,
                        com.brandon3055.draconicevolution.client.shader.ExplosionFlashRenderer::new));
        shieldShader.onShaderApplied(shader -> shader.getTimeUniform().glUniform1f(shieldTime()));
        energyCoreShader.onShaderApplied(shader -> shader.getTimeUniform().glUniform1f(shieldTime()));
        reactorBeamShader.onShaderApplied(shader -> shader.getTimeUniform().glUniform1f(shieldTime() * 0.4F));
        modBus.addListener((RegisterRenderPipelinesEvent event) -> {
            for (var shader : new com.brandon3055.brandonscore.client.shader.BCShader<?>[]{TOOL_BASE_SHADER, TOOL_GEM_SHADER, TOOL_TRACE_SHADER, TOOL_BLADE_SHADER,
                    BOW_STRING_SHADER, CHESTPIECE_GEM_SHADER, CHESTPIECE_SHIELD_SHADER, ENERGY_CRYSTAL, TOOL_CHAOS_SHADER, reactorShader, reactorShieldShader, reactorBeamShader, chaosBlockShader, shieldShader, energyCoreShader, explosionShader, explosionFlashShader, REACTOR_GUI, REACTOR_SHIELD_GUI}) {
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS));
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES));
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLE_STRIP));
            }
        });
    }

    // port-26.1.2:Identifier/time-хелперы ниже не используются до этапа 3
    static Identifier coreId(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    static float shieldTime() {
        return (TimeKeeper.getClientTick() + net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)) / 20F;
    }
}
