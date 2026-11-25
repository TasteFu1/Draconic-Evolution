package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.ShaderDraw;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;

public final class GuardianOverlayRenderer extends PictureInPictureRenderer<GuardianOverlayRenderer.State> {
    public record State(int x0, int y0, int x1, int y1, float shield, boolean immune, boolean shaders,
                        boolean showCrystal, int countWidth, float rotation, Matrix3x2f pose,
                        ScreenRectangle scissorArea) implements PictureInPictureRenderState {
        @Override public float scale() { return 1; }
        @Override public ScreenRectangle bounds() {
            ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
            return scissorArea == null ? bounds : scissorArea.intersection(bounds);
        }
    }

    private static final Identifier ENDER_CRYSTAL_TEXTURES = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/entity/guardian_crystal.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(ENDER_CRYSTAL_TEXTURES);
    private static final float SIN_45 = (float) Math.sin((Math.PI / 4D));

    private static final ModelPart glass;
    private static final ModelPart cube;

    static {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        ModelPart modelRoot = LayerDefinition.create(meshdefinition, 64, 32).bakeRoot();
        glass = modelRoot.getChild("glass");
        cube = modelRoot.getChild("cube");
    }

    private static final RenderType SHIELD = DEShaders.shieldShader.renderType("de_guardian_hud_shield", null, VertexFormat.Mode.QUADS);

    public GuardianOverlayRenderer(MultiBufferSource.BufferSource buffers) { super(buffers); }
    @Override public Class<State> getRenderStateClass() { return State.class; }
    @Override protected String getTextureLabel() { return "Draconic Guardian shield and crystals"; }
    @Override protected float getTranslateY(int height, int scale) { return 0; }

    @Override
    protected void renderToTexture(State state, PoseStack pose) {
        if (state.shaders()) {
            DEShaders.shieldColour.glUniform4f(state.immune() ? 0 : 1, state.immune() ? 1 : 0, state.immune() ? 1 : 0, 2);
            DEShaders.shieldBarMode.glUniform1i(1);
            DEShaders.shieldActivation.glUniform1f(state.shield());
            var vertices = bufferSource.getBuffer(ShaderDraw.capture(SHIELD));
            var mat = pose.last().pose();
            vertices.addVertex(mat, -91, 22, 0).setUv(0, 1);
            vertices.addVertex(mat, 91, 22, 0).setUv(1, 1);
            vertices.addVertex(mat, 91, 16, 0).setUv(1, 0);
            vertices.addVertex(mat, -91, 16, 0).setUv(0, 0);
            DEShaders.shieldBarMode.glUniform1i(0);
        }
        if (state.showCrystal()) {
            pose.translate(91 - state.countWidth() - 8, 10, 0);
            pose.scale(14, 14, 14);
            renderCrystal(pose, bufferSource.getBuffer(RENDER_TYPE), state.rotation());
            if (state.shaders()) {
                DEShaders.shieldColour.glUniform4f(1, 0, 0, 1.5F);
                DEShaders.shieldActivation.glUniform1f(1);
                renderCrystal(pose, bufferSource.getBuffer(ShaderDraw.capture(SHIELD)), state.rotation());
            }
        }
    }

    private static void renderCrystal(PoseStack pose, VertexConsumer vertices, float rotation) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        for (int layer = 0; layer < 3; layer++) {
            if (layer != 0) pose.scale(0.875F, 0.875F, 0.875F);
            pose.mulPose(new Quaternionf().setAngleAxis((float) Math.PI / 3F, SIN_45, 0F, SIN_45));
            pose.mulPose(Axis.YP.rotationDegrees(rotation));
            (layer == 2 ? cube : glass).render(pose, vertices, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }
}
