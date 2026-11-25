package com.brandon3055.draconicevolution.blocks.tileentity;

import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.lib.datamanager.DataFlags;
import com.brandon3055.brandonscore.lib.datamanager.ManagedPos;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.blocks.Portal;
import com.brandon3055.draconicevolution.client.render.particle.ParticlePortal;
import com.brandon3055.draconicevolution.init.DEContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Created by brandon3055 on 16/07/2016.
 */
public class TilePortal extends TileBCore {
    private final ManagedPos controllerPos = register(new ManagedPos("controller_pos", (BlockPos) null, DataFlags.SAVE_NBT_SYNC_TILE));

    private boolean hidden = false;
    private Player player = null;

    public TilePortal(BlockPos pos, BlockState state) {
        super(DEContent.TILE_PORTAL.get(), pos, state);
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos.set(worldPosition.subtract(controllerPos));
    }

    protected BlockPos getControllerPos() {
        return controllerPos.get() == null ? BlockPos.ZERO : worldPosition.subtract(controllerPos.get());
    }

    public TileDislocatorReceptacle getController() {
        BlockEntity tile = level.getBlockEntity(getControllerPos());
        return tile instanceof TileDislocatorReceptacle ? (TileDislocatorReceptacle) tile : null;
    }

    public boolean isPortalActive() {
        TileDislocatorReceptacle controller = getController();
        return controller != null && controller.isActive();
    }

    @Override
    public void tick() {
        if (level == null || !level.isClientSide()) {
            super.tick();
            return;
        }
        if (hidden && player != null) {
            BlockState state = getBlockState();
            if (!state.is(DEContent.PORTAL.get())) {
                hidden = false;
                player = null;
                return;
            }
            Direction.Axis axis = state.getValue(Portal.AXIS);
            Vector3 vec = Vector3.fromTileCenter(this).subtract(Vector3.fromEntity(player));
            double dist = Math.abs(axis == Direction.Axis.X ? vec.x : axis == Direction.Axis.Y ? vec.y : vec.z);
            if (dist > 1.5) {
                hidden = false;
                player = null;
                level.setBlock(getBlockPos(), state.setValue(Portal.VISIBLE, true), 0, 0);
            }
            return;
        }
        ClientEffects.spawn(this);
    }

    private static final class ClientEffects {
        private static void spawn(TilePortal tile) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            double distanceMod = Utils.getDistance(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5, player.getX(), player.getY(), player.getZ());
            if (tile.getLevel().getRandom().nextInt(Math.max((int) (distanceMod * (distanceMod / 5D)), 1)) == 0) {
                BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
                if (!state.is(DEContent.PORTAL.get())) {
                    return;
                }
                Direction.Axis axis = state.getValue(Portal.AXIS);

                double rD1 = tile.getLevel().getRandom().nextDouble();
                double rD2 = tile.getLevel().getRandom().nextDouble();
                double rO1 = -0.1 + tile.getLevel().getRandom().nextDouble() * 0.2;
                double rO2 = -0.1 + tile.getLevel().getRandom().nextDouble() * 0.2;

                if (axis == Direction.Axis.Z && player.getZ() < tile.getBlockPos().getZ() + 0.5) {
                    mc.particleEngine.add(new ParticlePortal((ClientLevel) tile.getLevel(), Vector3.fromTile(tile).add(rD1, rD2, 0), Vector3.fromTile(tile).add(rD1 + rO1, rD2 + rO2, 0.75)));
                } else if (axis == Direction.Axis.Z && player.getZ() > tile.getBlockPos().getZ() + 0.5) {
                    mc.particleEngine.add(new ParticlePortal((ClientLevel) tile.getLevel(), Vector3.fromTile(tile).add(rD1, rD2, 1), Vector3.fromTile(tile).add(rD1 + rO1, rD2 + rO2, 0.25)));
                } else if (axis == Direction.Axis.X && player.getX() < tile.getBlockPos().getX() + 0.5) {
                    mc.particleEngine.add(new ParticlePortal((ClientLevel) tile.getLevel(), Vector3.fromTile(tile).add(0, rD1, rD2), Vector3.fromTile(tile).add(0.75, rD1 + rO1, rD2 + rO2)));
                } else if (axis == Direction.Axis.X && player.getX() > tile.getBlockPos().getX() + 0.5) {
                    mc.particleEngine.add(new ParticlePortal((ClientLevel) tile.getLevel(), Vector3.fromTile(tile).add(1, rD1, rD2), Vector3.fromTile(tile).add(0.25, rD1 + rO1, rD2 + rO2)));
                } else if (axis == Direction.Axis.Y && player.getY() + player.getEyeHeight() > tile.getBlockPos().getY() + 0.5) {
                    mc.particleEngine.add(new ParticlePortal((ClientLevel) tile.getLevel(), Vector3.fromTile(tile).add(rD1, 1, rD2), Vector3.fromTile(tile).add(rD1 + rO1, 0.25, rD2 + rO2)));
                } else if (axis == Direction.Axis.Y && player.getY() + player.getEyeHeight() < tile.getBlockPos().getY() + 0.5) {
                    mc.particleEngine.add(new ParticlePortal((ClientLevel) tile.getLevel(), Vector3.fromTile(tile).add(rD1, 0, rD2), Vector3.fromTile(tile).add(rD1 + rO1, 0.75, rD2 + rO2)));
                }
            }
        }
    }

    public void clientArrived(Player player) {
        BlockState state = getBlockState();
        if (state.is(DEContent.PORTAL.get()) && level != null) {
            level.setBlock(getBlockPos(), state.setValue(Portal.VISIBLE, false), 0, 0);
            hidden = true;
            this.player = player;
        }
    }
}
