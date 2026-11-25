package com.brandon3055.draconicevolution.client;

import com.brandon3055.draconicevolution.client.render.particle.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import static com.brandon3055.draconicevolution.client.DEParticles.*;

public final class DEParticleEffects {
    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FLAME.get(), CustomFlameParticle.Factory::new);
        event.registerSpriteSet(LINE_INDICATOR.get(), ParticleLineIndicator.Factory::new);
        event.registerSpriteSet(ENERGY.get(), ParticleEnergy.Factory::new);
        event.registerSpriteSet(ENERGY_BASIC.get(), ParticleEnergyBasic.Factory::new);
        event.registerSpriteSet(ENERGY_CORE.get(), ParticleEnergyCoreFX.Factory::new);
        event.registerSpriteSet(GUARDIAN_PROJECTILE.get(), GuardianProjectileParticle.Factory::new);
        event.registerSpriteSet(BLINK.get(), BlinkParticle.Factory::new);
        event.registerSpriteSet(GUARDIAN_CLOUD.get(), GuardianCloudParticle.Factory::new);
        event.registerSpriteSet(GUARDIAN_BEAM.get(), GuardianBeamParticle.Factory::new);
        event.registerSpriteSet(SPARK.get(), SparkParticle.Factory::new);
    }


    public static com.brandon3055.draconicevolution.client.render.effect.CrystalFXBase<?> createCrystalEffect(
            com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase tile, boolean directIO) {
        ClientLevel level = (ClientLevel) tile.getLevel();
        return directIO ? new com.brandon3055.draconicevolution.client.render.effect.CrystalFXIO(level, tile)
                : new com.brandon3055.draconicevolution.client.render.effect.CrystalFXRing(level, tile);
    }

    public static Particle addParticleDirect(Level world, Particle particle) {
        if (world instanceof ClientLevel && particle instanceof com.brandon3055.draconicevolution.client.render.effect.CrystalFXBase<?> crystal) {
            com.brandon3055.draconicevolution.client.render.effect.CrystalFXRenderer.add(crystal);
            return crystal;
        }
        if (world instanceof ClientLevel) {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return null;
            Camera activerenderinfo = mc.gameRenderer.getMainCamera();
            if (activerenderinfo.isInitialized() && mc.particleEngine != null) {
                mc.particleEngine.add(particle);
                return particle;
            }
        }
        return null;
    }

}
