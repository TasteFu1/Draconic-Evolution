package com.brandon3055.draconicevolution.client;

import codechicken.lib.data.MCDataInput;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.shader.GuardianOverlayRenderer;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.common.NeoForge;
import java.util.Map;
import java.util.UUID;

/**
 * Created by brandon3055 on 24/7/21
 */
public class CustomBossInfoHandler {
    //TODO Update this to sprites.
    private static final Identifier GUI_BARS_LOCATION = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/gui/bars.png");
    private static final Map<UUID, BossShieldInfo> events = Maps.newLinkedHashMap();

    public static void init() {
        NeoForge.EVENT_BUS.addListener(CustomBossInfoHandler::onClientDisconnect);
        NeoForge.EVENT_BUS.addListener(CustomBossInfoHandler::preDrawBossInfo);
    }

    public static void preDrawBossInfo(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent info = event.getBossEvent();
        if (!events.containsKey(info.getId())) return;
        event.setCanceled(true);
        BossShieldInfo shieldInfo = events.get(info.getId());
        Minecraft mc = Minecraft.getInstance();
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int x = event.getX();
        int y = event.getY();
        drawBar(graphics, x, y, info);

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Component counter = Component.literal("x" + shieldInfo.crystals);
        int countWidth = mc.font.width(counter);
        if (DEConfig.guardianShaders || shieldInfo.crystals > 0) {
            graphics.nextStratum();
            graphics.submitPictureInPictureRenderState(new GuardianOverlayRenderer.State(x, y - 16, x + 182, y + 6,
                    shieldInfo.isImmune() ? 1 : shieldInfo.getShield(), shieldInfo.isImmune(), DEConfig.guardianShaders,
                    shieldInfo.crystals > 0, countWidth, (TimeKeeper.getClientTick() + partialTick) * 3F,
                    new org.joml.Matrix3x2f(graphics.pose()), graphics.peekScissorStack()));
        }
        graphics.nextStratum();
        if (shieldInfo.crystals > 0) {
            graphics.text(mc.font, counter, x + 182 - countWidth, y - 9, 0xFFFFFFFF);
        }
        int nameX = shieldInfo.crystals > 0 ? x : (event.getWindow().getGuiScaledWidth() - mc.font.width(info.getName())) / 2;
        graphics.text(mc.font, info.getName(), nameX, y - 9, 0xFFFF0000);
    }

    private static void drawBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent info) {
        drawRect(graphics, x, y, 0, info.getColor().ordinal() * 5 * 2, 182, 5);
        if (info.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            drawRect(graphics, x, y, 0, 80 + (info.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
        }

        int i = (int) (info.getProgress() * 183.0F);
        if (i > 0) {
            drawRect(graphics, x, y, 0, info.getColor().ordinal() * 5 * 2 + 5, i, 5);
            if (info.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
                drawRect(graphics, x, y, 0, 80 + (info.getOverlay().ordinal() - 1) * 5 * 2 + 5, i, 5);
            }
        }
    }

    private static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        events.clear();
    }

    public static void handlePacket(MCDataInput input) {
        UUID id = input.readUUID();
        int op = input.readByte();
//        LOGGER.info("Packet, OP: " + op + ". ID: " + id + ", Exists: " + events.containsKey(id));
        switch (op) {
            case 0: //Add
                events.put(id, new BossShieldInfo(input.readFloat(), input.readByte(), input.readBoolean()));
                break;
            case 1: //Remove
                events.remove(id);
                break;
            case 2: //Update Shield
                events.get(id).setShield(input.readFloat());
                break;
            case 3: //Update Crystals
                events.get(id).setCrystals(input.readByte());
                break;
            case 4: //Update Immune
                events.get(id).setImmune(input.readBoolean());
                break;
        }
    }

    public static void drawRect(GuiGraphicsExtractor render, int x, int y, int u, int v, int width, int height) {
        render.blit(RenderPipelines.GUI_TEXTURED, GUI_BARS_LOCATION, x, y, u, v, width, height, 256, 256);
    }

    public static class BossShieldInfo {
        protected float lastPower;
        protected float targetPower;
        protected long setTime;
        protected int crystals;
        protected boolean immune;

        public BossShieldInfo(float power, int crystals, boolean immune) {
            this.lastPower = power;
            this.targetPower = power;
            this.crystals = crystals;
            this.immune = immune;
            this.setTime = Util.getMillis();
        }

        public BossShieldInfo setShield(float power) {
            this.lastPower = this.getShield();
            this.targetPower = power;
            this.setTime = Util.getMillis();
            return this;
        }

        public float getShield() {
            long i = Util.getMillis() - this.setTime;
            float f = Mth.clamp((float) i / 100.0F, 0.0F, 1.0F);
            return Mth.lerp(f, this.lastPower, this.targetPower);
        }

        public void setCrystals(int crystals) {
            this.crystals = crystals;
        }

        public int getCrystals() {
            return crystals;
        }

        public void setImmune(boolean immune) {
            this.immune = immune;
        }

        public boolean isImmune() {
            return immune;
        }
    }
}
