package com.brandon3055.draconicevolution.blocks.tileentity.flowgate;

import codechicken.lib.data.MCDataInput;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.inventory.FlowGateMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Created by brandon3055 on 15/11/2016.
 */
public class TileFluxGate extends TileFlowGate {

    private final OPRegulator inputReg = new OPRegulator(this, true);
    private final OPRegulator outputReg = new OPRegulator(this, false);

    public TileFluxGate(BlockPos pos, BlockState state) {
        super(DEContent.TILE_FLUX_GATE.get(), pos, state);
    }

    private OPRegulator regulator(Direction side) {
        if (side == getDirection()) return outputReg;
        if (side == getDirection().getOpposite()) return inputReg;
        return null;
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(CapabilityOP.OP, DEContent.TILE_FLUX_GATE.get(), TileFluxGate::regulator);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, DEContent.TILE_FLUX_GATE.get(), TileFluxGate::regulator);
    }

    @Override
    public String getUnits() {
        return "RF/t";
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        if (level != null) level.invalidateCapabilities(worldPosition);
    }

    //endregion

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FlowGateMenu(id, player.getInventory(), this);
    }

    @Override
    public InteractionResult onBlockUse(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer) {
            player.openMenu(this, worldPosition);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void receivePacketFromClient(MCDataInput data, ServerPlayer client, int id) {
        if (flowOverridden.get()) {
            return;
        }

        try {
            String value = data.readString();
            long l = Long.parseLong(value);
            if (l < 0) {
                l = 0;
            }

            if (id == 0) {
                minFlow.set(l);
            } else if (id == 1) {
                maxFlow.set(l);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private static class OPRegulator implements IOPStorage, EnergyHandler {

        private final TileFluxGate tile;
        private final boolean isInput;

        public OPRegulator(TileFluxGate tile, boolean isInput) {
            this.tile = tile;
            this.isInput = isInput;
        }

        @Override
        public long getAmountAsLong() {
            return getOPStored();
        }

        @Override
        public long getCapacityAsLong() {
            return getMaxOPStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return transfer(amount, transaction, true);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return transfer(amount, transaction, false);
        }

        private int transfer(int amount, TransactionContext transaction, boolean insert) {
            TransferPreconditions.checkNonNegative(amount);
            if (insert != isInput || tile.level == null) return 0;
            int limit = (int) Math.min(amount, tile.availableFlow());
            if (limit == 0) return 0;
            Direction offset = isInput ? tile.getDirection() : tile.getDirection().getOpposite();
            EnergyHandler neighbor = tile.level.getCapability(Capabilities.Energy.BLOCK,
                    tile.worldPosition.relative(offset), offset.getOpposite());
            if (neighbor == null) return 0;
            try (Transaction nested = Transaction.open(transaction)) {
                tile.flowJournal.updateSnapshots(nested);
                tile.transferThisTick += limit;
                int moved = insert ? neighbor.insert(limit, nested) : neighbor.extract(limit, nested);
                tile.transferThisTick -= limit - moved;
                nested.commit();
                return moved;
            }
        }

        @Override
        public long receiveOP(long maxReceive, boolean simulate) {
            if (isInput) {
                BlockEntity target = tile.getTarget();

                if (target == null) {
                    return 0;
                }

                Direction tInputSide = tile.getDirection().getOpposite();
                long sim = EnergyUtils.insertEnergy(target, maxReceive, tInputSide, true);
                long transfer = EnergyUtils.insertEnergy(target, Math.min(Math.max(0, tile.getFlow() - tile.transferThisTick), sim), tInputSide, simulate);

                if (!simulate) {
                    tile.transferThisTick += transfer;
                }
                return transfer;
            }

            return 0;
        }

        @Override
        public long extractOP(long maxExtract, boolean simulate) {
            if (!isInput) {
                BlockEntity source = tile.getSource();

                if (source == null) {
                    return 0;
                }

                Direction tExtractSide = tile.getDirection();
                long sim = EnergyUtils.extractEnergy(source, maxExtract, tExtractSide, true);
                long transfer = EnergyUtils.extractEnergy(source, Math.min(Math.max(0, tile.getFlow() - tile.transferThisTick), sim), tExtractSide, simulate);

                if (!simulate) {
                    tile.transferThisTick += transfer;
                }
                return transfer;
            }

            return 0;
        }

        @Override
        public long getOPStored() {
            if (isInput) {
                return 0;
            }

            BlockEntity target = tile.getTarget();

            if (target == null) {
                return 0;
            }

            return EnergyUtils.getEnergyStored(target, tile.getDirection().getOpposite());
        }

        @Override
        public long getMaxOPStored() {
            if (isInput) {
                return 0;
            }

            BlockEntity target = tile.getTarget();

            if (target == null) {
                return 0;
            }

            return EnergyUtils.getMaxEnergyStored(target, tile.getDirection().getOpposite());
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int) receiveOP(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return (int) extractOP(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, getOPStored());
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, getMaxOPStored());
        }

        @Override
        public long modifyEnergyStored(long amount) {
            return 0; //Invalid operation for this device
        }

        @Override
        public boolean canExtract() {
            return !isInput;
        }

        @Override
        public boolean canReceive() {
            return isInput;
        }
    }


}
