package com.brandon3055.draconicevolution.client.render.item;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.render.modelfx.StaffModelEffect;
import com.brandon3055.draconicevolution.items.equipment.ModularStaff;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 22/5/20.
 */
public class RenderModularStaff extends ToolRenderBase {

    private static final StaffModelEffect effectRenderer = new StaffModelEffect();
    private final ToolPart baseGuiPart;
    private final ToolPart materialGuiPart;
    private final ToolPart traceGuiPart;
    private final ToolPart bladeGuiPart;
    private final ToolPart gemGuiPart;

    private final ToolPart basePart;
    private final ToolPart materialPart;
    private final ToolPart tracePart;
    private final ToolPart bladePart;
    private final ToolPart gemPart;

    public RenderModularStaff(TechLevel techLevel) {
        super(techLevel, "staff");

        Map<String, CCModel> guiModel = new OBJParser(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "models/item/equipment/staff_gui.obj")).ignoreMtl().parse();
        baseGuiPart = basePart(CCModel.combine(Arrays.asList(guiModel.get("handle"), guiModel.get("head_connection"))).backfacedCopy());
        materialGuiPart = materialPart(guiModel.get("head").backfacedCopy());
        traceGuiPart = tracePart(guiModel.get("trace"));
        bladeGuiPart = bladePart(guiModel.get("blade").backfacedCopy());
        gemGuiPart = gemPart(guiModel.get("focus_gem").backfacedCopy());

        Map<String, CCModel> model = new OBJParser(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "models/item/equipment/staff.obj")).ignoreMtl().parse();
        basePart = basePart(CCModel.combine(Arrays.asList(model.get("handle"), model.get("head_connection"), model.get("cage_connection"))).backfacedCopy());
        materialPart = materialPart(CCModel.combine(Arrays.asList(model.get("head"), model.get("crystal_cage"))).backfacedCopy());
        tracePart = tracePart(model.get("trace"));
        bladePart = bladePart(model.get("blade").backfacedCopy());
        gemPart = gemPart(CCModel.combine(Arrays.asList(model.get("focus_gem"), model.get("energy_crystal"))).backfacedCopy());
    }

    //TODO want to combine the swing and equip animation somehow so the 'draw back / return' after a stab corresponds to the equip cooldown
    @Override
    public void renderTool(CCRenderState ccrs, ItemStack stack, ItemDisplayContext transform, Matrix4 mat, MultiBufferSource buffers, boolean gui) {
        float flair = 0F;
        float partialTick = partialTicks;
        if (entity != null && entity.getMainHandItem() == stack) {
            flair = entity.getAttackAnim(partialTick);
            flair = MathHelper.clip(flair * 5F, 0F, 1F);
        }

        handleArmPose(stack, transform, mat);
        if (gui) {
            transform(mat, 0.19, 0.19, 0.5, 1.1);

            baseGuiPart.render(transform, buffers, mat);
            materialGuiPart.render(transform, buffers, mat);
            traceGuiPart.render(transform, buffers, mat);
            bladeGuiPart.render(transform, buffers, mat);
            gemGuiPart.render(transform, buffers, mat);
            return;
        }

        if (transform == ItemDisplayContext.FIXED || transform == ItemDisplayContext.GROUND || transform == ItemDisplayContext.NONE) {
            transform(mat, 0.6, 0.6, 0.5, 0.75);
        } else {
            transform(mat, 0.27, 0.27, 0.5, 1.125);
            if (transform == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transform == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                mat.rotate(45 * -MathHelper.torad, Vector3.Z_NEG);
            } else {
                mat.rotate(45 * MathHelper.torad, Vector3.Z_NEG);
            }
        }

        // TODO flair
        basePart.render(transform, buffers, mat);
        materialPart.render(transform, buffers, mat);
        tracePart.render(transform, buffers, mat, flair);
        bladePart.render(transform, buffers, mat, flair);
        gemPart.render(transform, buffers, mat, flair);

        RenderUtils.endBatch(buffers);

        Minecraft mc = Minecraft.getInstance();
        mat.rotate(torad(90), Vector3.X_NEG);
        mat.translate(-0.5, 0.1, -0.5);
        effectRenderer.renderEffect(mat, buffers, partialTick, techLevel);
    }

    private void handleArmPose(ItemStack stack, ItemDisplayContext transform, Matrix4 mat) {
        if (isThirdPerson(transform)) {
            if (transform == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                mat.rotate(torad(-15), new Vector3(-0.5, 0.5, 0));
            } else {
                mat.rotate(torad(15), new Vector3(-0.5, 0.5, 0));
            }
            if (entity != null) {
                double anim = entity.attackAnim;
                mat.translate(0.125 * anim, 0.125 * anim, 0);
                mat.rotate(torad(-20) * anim, Vector3.Z_POS);
            }
        }
    }

    private boolean isThirdPerson(ItemDisplayContext transform) {
        return transform == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || transform == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    public static void applyStaffPose(AvatarRenderState state, PlayerModel model) {
        if (!(state.getMainHandItemStack().getItem() instanceof ModularStaff)) return;
        boolean left = state.mainArm == HumanoidArm.LEFT;
        ItemStack offHand = left ? state.rightHandItemStack : state.leftHandItemStack;
        setStaffPose(state.attackTime, model.rightArm, model.leftArm, model.head, left, !(offHand.getItem() instanceof ModularStaff));
    }

    private static void setStaffPose(float attackTime, ModelPart rightArm, ModelPart leftArm, ModelPart head, boolean lefthand, boolean bothHands) {
        if (lefthand) {
            //Yaw
            leftArm.yRot = head.yRot + torad(15F);
            //Pitch
            leftArm.xRot = head.xRot - torad(60F) - torad(20F * attackTime);
            if (bothHands) {
                rightArm.xRot = head.xRot - torad(60F) - torad(20F * attackTime);
                rightArm.yRot = head.yRot - torad(45F);
            }
        } else {
            //Yaw
            rightArm.yRot = head.yRot - torad(15F);
            //Pitch
            rightArm.xRot = head.xRot - torad(60F) - torad(20F * attackTime);
            if (bothHands) {
                leftArm.xRot = head.xRot - torad(60F) - torad(20F * attackTime);
                leftArm.yRot = head.yRot + torad(45F);
            }
        }
    }

    public static float torad(double degrees) {
        return (float) (degrees * MathHelper.torad);
    }

    //@formatter:off //This is not cursed at all! idk what your talking about!
    public static class STAFF_DRACONIC extends RenderModularStaff { public STAFF_DRACONIC() {super(TechLevel.DRACONIC);}}
    public static class STAFF_CHAOTIC extends RenderModularStaff { public STAFF_CHAOTIC() {super(TechLevel.CHAOTIC);}}
    //@formatter::on
}
