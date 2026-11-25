package com.brandon3055.draconicevolution.blocks.reactor.tileentity;

import com.brandon3055.draconicevolution.lib.TransactionalOPStorage;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import com.brandon3055.brandonscore.capability.CapabilityOP;

import com.brandon3055.brandonscore.utils.EnergyUtils;

import com.brandon3055.draconicevolution.init.DEContent;

import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;



/**
 * Created by brandon3055 on 18/01/2017.
 */
public class TileReactorStabilizer extends TileReactorComponent {

    public TileReactorStabilizer(BlockPos pos, BlockState state) {
        super(DEContent.TILE_REACTOR_STABILIZER.get(), pos, state);
        OPExtractor opExtractor = new OPExtractor(this);
        capManager.set(CapabilityOP.OP, opExtractor);
        capManager.setCapSideValidator(opExtractor, face -> face == this.facing.get().getOpposite());
    }

    public static void register(RegisterCapabilitiesEvent event) {
        com.brandon3055.brandonscore.capability.CapabilityOP.registerBlockEntity(event, DEContent.TILE_REACTOR_STABILIZER.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            return;
        }

        TileReactorCore tile = getCachedCore();

        if (tile != null && tile.reactorState.get() == TileReactorCore.ReactorState.RUNNING) {
            BlockEntity output = level.getBlockEntity(worldPosition.relative(facing.get().getOpposite()));
            if (output != null && EnergyUtils.canReceiveEnergy(output, facing.get())) {
                long sent = EnergyUtils.insertEnergy(output, tile.saturation.get(), facing.get(), false);
                tile.saturation.subtract(sent);
            }
        }
    }

    private static class OPExtractor implements TransactionalOPStorage {
        private final TileReactorStabilizer tile;

        public OPExtractor(TileReactorStabilizer tile) {
            this.tile = tile;
        }

        @Override
        public long extractOP(long maxExtract, boolean simulate) {
            TileReactorCore core = tile.getCachedCore();
            return core == null ? 0 : core.extractEnergy(maxExtract, simulate, null);
        }

        @Override
        public long getAmountAsLong() {
            TileReactorCore core = tile.getCachedCore();
            return core == null ? 0 : core.availableOutput();
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            TileReactorCore core = tile.getCachedCore();
            return core == null ? 0 : (int) core.extractEnergy(amount, false, transaction);
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return (int) extractOP(maxExtract, simulate);
        }

        @Override
        public long getMaxOPStored() {
            return Long.MAX_VALUE;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long modifyEnergyStored(long amount) {
            return 0; //Invalid operation for this device
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
