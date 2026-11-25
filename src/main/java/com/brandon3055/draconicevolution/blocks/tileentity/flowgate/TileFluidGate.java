package com.brandon3055.draconicevolution.blocks.tileentity.flowgate;

import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.inventory.FlowGateMenu;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.TransferPreconditions;

import org.jetbrains.annotations.NotNull;


/**
 * Created by brandon3055 on 15/11/2016.
 */
public class TileFluidGate extends TileFlowGate {

    private FlowHandler inputHandler = new FlowHandler(this, true);
    private FlowHandler outputHandler = new FlowHandler(this, false);

    public TileFluidGate(BlockPos pos, BlockState state) {
        super(DEContent.TILE_FLUID_GATE.get(), pos, state);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, DEContent.TILE_FLUID_GATE.get(), (tile, side) -> {
            if (side != null && side.getAxis() == tile.getDirection().getAxis()) {
                return side == tile.getDirection() ? tile.outputHandler : tile.inputHandler;
            }
            return null;
        });
    }

    @Override
    public String getUnits() {
        return "MB/t";
    }

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

    private class FlowHandler implements ResourceHandler<FluidResource> {
        private final boolean isInput;
        public FlowHandler(TileFluidGate gate, boolean isInput) { this.isInput = isInput; }

        private ResourceHandler<FluidResource> neighbor() {
            if (level == null) return null;
            var pos = worldPosition.relative(isInput ? getDirection() : getDirection().getOpposite());
            // Query the face toward the gate; the source face points forward.
            return level.getCapability(Capabilities.Fluid.BLOCK, pos,
                    isInput ? getDirection().getOpposite() : getDirection());
        }

        @Override public int size() { var h = neighbor(); return h == null ? 0 : h.size(); }
        @Override public FluidResource getResource(int index) {
            var h = neighbor(); return h == null || index < 0 || index >= h.size() ? FluidResource.EMPTY : h.getResource(index);
        }
        @Override public long getAmountAsLong(int index) {
            var h = neighbor(); return h == null || index < 0 || index >= h.size() ? 0 : h.getAmountAsLong(index);
        }
        @Override public long getCapacityAsLong(int index, FluidResource resource) {
            var h = neighbor(); return h == null || index < 0 || index >= h.size() ? 0 : h.getCapacityAsLong(index, resource);
        }
        @Override public boolean isValid(int index, FluidResource resource) {
            var h = neighbor(); return h != null && index >= 0 && index < h.size() && h.isValid(index, resource);
        }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return transfer(index, resource, amount, transaction, true);
        }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return transfer(index, resource, amount, transaction, false);
        }
        private int transfer(int index, FluidResource resource, int amount, TransactionContext transaction, boolean insert) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (insert != isInput || amount == 0) return 0;
            var h = neighbor();
            if (h == null || index < 0 || index >= h.size()) return 0;
            int limit = (int) Math.min(amount, availableFlow());
            if (limit == 0) return 0;
            try (var nested = Transaction.open(transaction)) {
                flowJournal.updateSnapshots(nested);
                // Reserve before calling a neighbor so reentrant transfers cannot reuse this allowance.
                transferThisTick += limit;
                int moved = insert ? h.insert(index, resource, limit, nested) : h.extract(index, resource, limit, nested);
                transferThisTick -= limit - moved;
                nested.commit();
                return moved;
            }
        }
    }
}
