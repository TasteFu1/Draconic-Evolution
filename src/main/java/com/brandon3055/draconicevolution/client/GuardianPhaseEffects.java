package com.brandon3055.draconicevolution.client;

import codechicken.lib.vec.Vector3;
import com.brandon3055.draconicevolution.client.render.particle.GuardianChargeParticle;
import com.brandon3055.draconicevolution.client.sound.SimpleSoundImpl;
import com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity;
import com.brandon3055.draconicevolution.handlers.DESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

public final class GuardianPhaseEffects {
    private GuardianPhaseEffects() {}

    public static void spawnCharge(DraconicGuardianEntity guardian, BlockPos origin, int duration) {
        ClientLevel level = (ClientLevel) guardian.level();
        for (int i = 0; i < 32; i++) {
            Minecraft.getInstance().particleEngine.add(new GuardianChargeParticle(level,
                    Vector3.fromBlockPosCenter(origin), Vector3.fromEntity(guardian), i / 32D,
                    duration, guardian.getPhaseManager()));
        }
    }

    public static void playBeam(DraconicGuardianEntity guardian, RandomSource random,
                                Supplier<Float> pitch, Supplier<Boolean> stopped) {
        SimpleSoundImpl.create(DESounds.BEAM.get(), SoundSource.HOSTILE, random)
                .setPitchSupplier(pitch).setStoppedSupplier(stopped).setPos(guardian)
                .setVolume(100).loop().play(Minecraft.getInstance());
    }
}
