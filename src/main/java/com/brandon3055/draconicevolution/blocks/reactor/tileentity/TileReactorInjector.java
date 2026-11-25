package com.brandon3055.draconicevolution.blocks.reactor.tileentity;

import com.brandon3055.draconicevolution.lib.TransactionalOPStorage;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.draconicevolution.init.DEContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Created by brandon3055 on 18/01/2017.
 */
public class TileReactorInjector extends TileReactorComponent {

    public TileReactorInjector(BlockPos pos, BlockState state) {
        super(DEContent.TILE_REACTOR_INJECTOR.get(), pos, state);
        OPInjector opInjector = new OPInjector(this);
        capManager.set(CapabilityOP.OP, opInjector);
        capManager.setCapSideValidator(opInjector, face -> face == this.facing.get().getOpposite());
    }

    public static void register(RegisterCapabilitiesEvent event) {
        com.brandon3055.brandonscore.capability.CapabilityOP.registerBlockEntity(event, DEContent.TILE_REACTOR_INJECTOR.get());
    }

    private static class OPInjector implements TransactionalOPStorage {
        private final TileReactorInjector tile;

        public OPInjector(TileReactorInjector tile) {
            this.tile = tile;
        }

        @Override
        public long receiveOP(long maxReceive, boolean simulate) {
            TileReactorCore core = tile.getCachedCore();
            return core == null ? 0 : core.injectEnergy(maxReceive, simulate, null);
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            TileReactorCore core = tile.getCachedCore();
            return core == null ? 0 : (int) core.injectEnergy(amount, false, transaction);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int) receiveOP(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
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
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
