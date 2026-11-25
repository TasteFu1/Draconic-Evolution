package com.brandon3055.draconicevolution.client.model;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Translation;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.client.model.ExtendedModelPart;
import com.brandon3055.brandonscore.client.render.EquippedItemModel;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.entities.ShieldControlEntity;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.render.item.ToolRenderBase;
import com.brandon3055.brandonscore.client.shader.BCShader;
import com.brandon3055.draconicevolution.client.shader.ShieldShader;
import com.brandon3055.draconicevolution.client.shader.ToolShader;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.ClientHooks;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * Created by brandon3055 on 13/11/2022
 */
public class ModularChestpieceModel<T extends LivingEntity> extends HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> implements EquippedItemModel {

    private static final Set<TechLevel> classicTiers = EnumSet.noneOf(TechLevel.class);
    private final TechLevel techLevel;
    private final Identifier classicTexture;
    private final HumanoidModel<HumanoidRenderState> classicArmor;

    private static Identifier classicTexture(TechLevel tier) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/equipment/humanoid/" +
                tier.name().toLowerCase(Locale.ROOT) + "_chestpiece.png");
    }

    public static void reloadClassicTextures(ResourceManager resources) {
        classicTiers.clear();
        for (TechLevel tier : List.of(TechLevel.WYVERN, TechLevel.DRACONIC, TechLevel.CHAOTIC)) {
            if (resources.getResource(classicTexture(tier)).isPresent()) classicTiers.add(tier);
        }
    }

    private int shieldColour;
    private float shieldState;

    public ModularChestpieceModel(TechLevel techLevel, boolean isOnArmor) {
        super(createEquipmentRoot());
        this.techLevel = techLevel;
        classicTexture = classicTexture(techLevel);
        CubeDeformation outer = LayerDefinitions.OUTER_ARMOR_DEFORMATION;
        if (isOnArmor) outer = outer.extend(0.15F);
        classicArmor = new HumanoidModel<>(HumanoidModel.createArmorMeshSet(
                LayerDefinitions.INNER_ARMOR_DEFORMATION, outer).chest().getRoot().bake(64, 32));
        Map<String, CCModel> model = new OBJParser(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "models/item/equipment/chestpeice.obj")).ignoreMtl().parse();
        CCModel baseModel = model.get("base_model").backfacedCopy();
        CCModel materialModel = model.get("chevrons").backfacedCopy();
        CCModel gemModel = model.get("power_crystals").backfacedCopy();
        CCModel coreGemModel = model.get("crystal_core").backfacedCopy();

        CCModel shieldHeadModel = model.get("shield_head").backfacedCopy();
        CCModel shieldBodyModel = model.get("shield_body").backfacedCopy();
        CCModel shieldRightArmModel = model.get("shield_right_arm").backfacedCopy();
        CCModel shieldLeftArmModel = model.get("shield_left_arm").backfacedCopy();
        CCModel shieldRightLegModel = model.get("shield_right_leg").backfacedCopy();
        CCModel shieldLeftLegModel = model.get("shield_left_leg").backfacedCopy();

        if (isOnArmor) {
            materialModel.apply(new Translation(0, 0, -0.0625));
            gemModel.apply(new Translation(0, 0, -0.0625 / 2));
            coreGemModel.apply(new Translation(0, 0, -0.0625 / 2));
        }

        String levelName = techLevel.name().toLowerCase(Locale.ROOT);
        RenderType baseType = DEShaders.TOOL_BASE_SHADER.renderType(MODID + ":chestpiece_base",
                Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/" + levelName + "_chestpeice.png"), VertexFormat.Mode.TRIANGLES);
        RenderType chaoticType = BCShaders.CHAOS_ENTITY_SHADER.renderType(MODID + ":chestpiece_chaos",
                Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/chaos_shader.png"), VertexFormat.Mode.TRIANGLES);
        Identifier fallback = Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/shader_fallback_" + levelName + ".png");
        RenderType gemType = DEShaders.TOOL_GEM_SHADER.renderType(MODID + ":chestpiece_gem", fallback, VertexFormat.Mode.TRIANGLES);
        RenderType coreGemType = DEShaders.CHESTPIECE_GEM_SHADER.renderType(MODID + ":chestpiece_core", fallback, VertexFormat.Mode.TRIANGLES);
        RenderType shieldType = DEShaders.CHESTPIECE_SHIELD_SHADER.renderType(MODID + ":armor_shield", null, VertexFormat.Mode.TRIANGLES);

        ExtendedModelPart body = (ExtendedModelPart) this.body;
        if (!isOnArmor) {
            body.addChild(new ChestpieceModelPart(baseModel, baseType, DEShaders.TOOL_BASE_SHADER));
        }
        if (techLevel == TechLevel.CHAOTIC) {
            body.addChild(new ChestpieceModelPart(materialModel, chaoticType, BCShaders.CHAOS_ENTITY_SHADER));
        } else {
            body.addChild(new ChestpieceModelPart(materialModel, baseType, DEShaders.TOOL_BASE_SHADER));
        }
        body.addChild(new ChestpieceModelPart(gemModel, gemType, DEShaders.TOOL_GEM_SHADER));
        body.addChild(new CoreGemModelPart(coreGemModel, coreGemType, DEShaders.CHESTPIECE_GEM_SHADER));

        ((ExtendedModelPart) head).addChild(new ShieldModelPart(shieldHeadModel, shieldType, DEShaders.CHESTPIECE_SHIELD_SHADER));
        body.addChild(new ShieldModelPart(shieldBodyModel, shieldType, DEShaders.CHESTPIECE_SHIELD_SHADER));
        ((ExtendedModelPart) leftArm).addChild(new ShieldModelPart(shieldLeftArmModel, shieldType, DEShaders.CHESTPIECE_SHIELD_SHADER));
        ((ExtendedModelPart) rightArm).addChild(new ShieldModelPart(shieldRightArmModel, shieldType, DEShaders.CHESTPIECE_SHIELD_SHADER));
        ((ExtendedModelPart) leftLeg).addChild(new ShieldModelPart(shieldLeftLegModel, shieldType, DEShaders.CHESTPIECE_SHIELD_SHADER));
        ((ExtendedModelPart) rightLeg).addChild(new ShieldModelPart(shieldRightLegModel, shieldType, DEShaders.CHESTPIECE_SHIELD_SHADER));
    }

    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(head);
    }

    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body, leftArm, rightArm, leftLeg, rightLeg);
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, ItemStack stack, int packedLight, int packedOverlay, float partialTicks) {
        shieldColour = 0xFFFFFFFF;
        shieldState = 0;
        try (ModuleHost host = DECapabilities.getHost(stack)) {
            if (!stack.isEmpty() && host != null) {
                ShieldControlEntity shieldControl = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER).map(e -> (ShieldControlEntity) e).findAny().orElse(null);
                if (shieldControl != null) {
                    shieldState = shieldControl.getShieldState();
                    shieldColour = shieldControl.getShieldColour() | 0xFF000000;
                    if (entity instanceof Player player) {
                        ContributorProperties props = ContributorHandler.getProps(player);
                        if (props.hasShieldRGB() && props.getConfig().overrideShield()) {
                            shieldColour = props.getConfig().getShieldColour(partialTicks);
                        }
                    }
                }
            }
        }

        if (entity.isBaby()) {
            poseStack.pushPose();
            float headScale = 1.5F / 2F;
            poseStack.scale(headScale, headScale, headScale);

            poseStack.translate(0.0D, 16F / 16.0F, 0F / 16.0F);
            this.headParts().forEach(part -> ((ExtendedModelPart) part).render(poseStack, buffers, packedLight, packedOverlay));
            poseStack.popPose();
            poseStack.pushPose();
            float f1 = 1.0F / 2F;
            poseStack.scale(f1, f1, f1);
            poseStack.translate(0.0D, 24F / 16.0F, 0.0D);
            renderBody(poseStack, buffers, packedLight, packedOverlay);
            poseStack.popPose();
        } else {
            this.headParts().forEach(part -> ((ExtendedModelPart) part).render(poseStack, buffers, packedLight, packedOverlay));
            renderBody(poseStack, buffers, packedLight, packedOverlay);
        }
    }

    private void renderBody(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (classicTiers.contains(techLevel)) {
            ClientHooks.copyModelProperties(this, classicArmor);
            int color = techLevel == TechLevel.CHAOTIC ? 0xFFB83256 : 0xFFFFFFFF;
            classicArmor.renderToBuffer(pose, buffers.getBuffer(RenderTypes.armorCutoutNoCull(classicTexture)), light, overlay, color);
        }
        bodyParts().forEach(part -> ((ExtendedModelPart) part).render(pose, buffers, light, overlay));
    }

    private static ModelPart createEquipmentRoot() {
        ModelPart vanilla = createMesh(new CubeDeformation(1), 0).getRoot().bake(64, 64);
        Map<String, ModelPart> parts = new HashMap<>();
        for (String name : new String[]{"head", "body", "left_arm", "right_arm", "left_leg", "right_leg"}) {
            ExtendedModelPart part = new ExtendedModelPart();
            part.loadPose(vanilla.getChild(name).storePose());
            parts.put(name, part);
        }
        // HumanoidModel's final fields are resolved from the root in MC26.
        ModelPart headWithHat = new ExtendedModelPartWithHat();
        headWithHat.loadPose(vanilla.getChild("head").storePose());
        parts.put("head", headWithHat);
        return new ModelPart(List.of(), parts);
    }

    private static class ExtendedModelPartWithHat extends ExtendedModelPart {
        private final ModelPart hat = new ModelPart(List.of(), Map.of());
        @Override public ModelPart getChild(String name) {
            return name.equals("hat") ? hat : super.getChild(name);
        }
    }

    public class ChestpieceModelPart extends ExtendedModelPart {
        protected final CCModel model;
        protected final RenderType renderType;
        protected final BCShader<?> shader;
        public ChestpieceModelPart(CCModel model, RenderType type, BCShader<?> shader) {
            this.model = model; this.renderType = type; this.shader = shader;
        }
        protected boolean shouldDraw() { return visible && !classicTiers.contains(techLevel); }
        protected void configureColour() { ToolRenderBase.glUniformBaseColor(shader, techLevel, 1F); }
        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay, float r, float g, float b, float a) {
            if (!shouldDraw()) return;
            pose.pushPose();
            translateAndRotate(pose);
            configureColour();
            shader.uniform(120).glUniform1f(1F);
            shader.getModelMatUniform().glUniformMatrix4f(new Matrix4(pose));
            shader.uniform(80).glUniform2i(overlay & 65535, overlay >>> 16);
            shader.uniform(88).glUniform2i(light & 65535, light >>> 16);
            shader.uniform(124).glUniform1i(0);
            shader.uniform(128).glUniform1i(0);
            shader.uniform(132).glUniform1i(0);
            CCRenderState state = CCRenderState.instance();
            state.reset(); state.brightness = light; state.overlay = overlay;
            // BC's Capture freezes the shader block at getBuffer, before another part changes it.
            state.bind(renderType, buffers);
            com.brandon3055.draconicevolution.client.render.DEModelEmission.render(model, state);
            pose.popPose();
        }
    }

    public class CoreGemModelPart extends ChestpieceModelPart {
        public CoreGemModelPart(CCModel model, RenderType type, ToolShader shader) { super(model, type, shader); }
        @Override protected void configureColour() { setShieldColour(shader); }
    }

    public class ShieldModelPart extends ChestpieceModelPart {
        public ShieldModelPart(CCModel model, RenderType type, ShieldShader shader) { super(model, type, shader); }
        @Override protected boolean shouldDraw() { return visible && shieldState > 0; }
        @Override protected void configureColour() {
            setShieldColour(shader);
            ((ShieldShader) shader).getActivationUniform().glUniform1f(shieldState);
        }
    }

    private void setShieldColour(BCShader<?> shader) {
        shader.uniform(64).glUniform4f(((shieldColour >>> 16) & 255) / 255F,
                ((shieldColour >>> 8) & 255) / 255F, (shieldColour & 255) / 255F,
                ((shieldColour >>> 24) & 255) / 255F);
    }
}
