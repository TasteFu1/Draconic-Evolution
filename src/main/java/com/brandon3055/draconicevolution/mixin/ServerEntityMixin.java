package com.brandon3055.draconicevolution.mixin;

import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.draconicevolution.entity.projectile.DraconicArrowEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    @ModifyArg(method = "sendChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerEntity$Synchronizer;sendToTrackingPlayers(Lnet/minecraft/network/protocol/Packet;)V"))
    private Packet<?> syncDraconicArrow(Packet<?> packet) {
        if (entity instanceof DraconicArrowEntity) {
            if (packet instanceof ClientboundMoveEntityPacket.PosRot) {
                return BCoreNetwork.sendEntityVelocity(entity, true);
            }
            if (packet instanceof ClientboundSetEntityMotionPacket) {
                return BCoreNetwork.sendEntityVelocity(entity, false);
            }
        }
        return packet;
    }

    @Inject(method = "sendPairingData", at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void pairDraconicArrow(ServerPlayer player, Consumer<Packet<? super ClientGamePacketListener>> packets,
                                   CallbackInfo callback) {
        if (entity instanceof DraconicArrowEntity) {
            packets.accept((Packet<? super ClientGamePacketListener>) BCoreNetwork.sendEntityVelocity(entity, false));
        }
    }
}
