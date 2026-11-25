package com.brandon3055.draconicevolution.mixin;

import com.brandon3055.draconicevolution.init.DEClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At("HEAD"), cancellable = true)
    private void hideCapeWithModularWings(PoseStack pose, SubmitNodeCollector collector, int light,
                                          AvatarRenderState state, float yaw, float pitch, CallbackInfo callback) {
        if (Boolean.TRUE.equals(state.getRenderData(DEClient.MODULAR_WINGS))) {
            callback.cancel();
        }
    }
}
