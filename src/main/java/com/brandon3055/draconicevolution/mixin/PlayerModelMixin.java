package com.brandon3055.draconicevolution.mixin;

import com.brandon3055.draconicevolution.client.handler.ModularItemRenderOverrideHandler;
import com.brandon3055.draconicevolution.client.render.item.RenderModularStaff;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("RETURN"))
    private void applyEquipmentPose(AvatarRenderState state, CallbackInfo callback) {
        PlayerModel model = (PlayerModel) (Object) this;
        RenderModularStaff.applyStaffPose(state, model);
        ModularItemRenderOverrideHandler.modifyPlayerPose(state, model);
    }
}
