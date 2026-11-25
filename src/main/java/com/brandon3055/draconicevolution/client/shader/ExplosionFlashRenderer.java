package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.ShaderDraw;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;

public final class ExplosionFlashRenderer extends PictureInPictureRenderer<ExplosionFlashRenderer.State> {
    public record State(int x0, int y0, int x1, int y1, float centerX, float centerY, float intensity,
                        Matrix3x2f pose, ScreenRectangle scissorArea) implements PictureInPictureRenderState {
        @Override public float scale() { return 1; }
        @Override public ScreenRectangle bounds() {
            ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
            return scissorArea == null ? bounds : scissorArea.intersection(bounds);
        }
    }

    private static final RenderType FLASH = DEShaders.explosionFlashShader.renderType("de_explosion_flash", null, VertexFormat.Mode.QUADS);

    public ExplosionFlashRenderer(MultiBufferSource.BufferSource buffers) { super(buffers); }
    @Override public Class<State> getRenderStateClass() { return State.class; }
    @Override protected String getTextureLabel() { return "Draconic explosion flash"; }
    @Override protected float getTranslateY(int height, int scale) { return 0; }

    @Override
    protected void renderToTexture(State state, PoseStack pose) {
        int width = state.x1() - state.x0();
        int height = state.y1() - state.y0();
        int scale = Minecraft.getInstance().getWindow().getGuiScale();
        DEShaders.explosionFlashScreenPos.glUniform2f(state.centerX(), state.centerY());
        DEShaders.explosionFlashScreenSize.glUniform2f(width * scale, height * scale);
        DEShaders.explosionFlashIntensity.glUniform1f(state.intensity());
        var vertices = bufferSource.getBuffer(ShaderDraw.capture(FLASH));
        var matrix = pose.last().pose();
        float left = -width / 2F;
        vertices.addVertex(matrix, left, height, 0).setColor(-1);
        vertices.addVertex(matrix, left + width, height, 0).setColor(-1);
        vertices.addVertex(matrix, left + width, 0, 0).setColor(-1);
        vertices.addVertex(matrix, left, 0, 0).setColor(-1);
    }
}
