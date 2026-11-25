package com.brandon3055.draconicevolution.inventory;

import com.brandon3055.brandonscore.inventory.TileItemStackHandler;
import com.brandon3055.brandonscore.lib.datamanager.BCSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Managed legacy menu slots with transactional automation and commit-only change notifications. */
public final class ManagedItemHandler extends TileItemStackHandler implements BCSerializable<CompoundTag> {
    private final BlockEntity owner;
    private CompoundTag pendingLoad;
    private final SlotJournal[] journals;

    public ManagedItemHandler(BlockEntity owner, int size) {
        super(owner, size);
        journals = new SlotJournal[size];
        for (int i = 0; i < size; i++) journals[i] = new SlotJournal(i);
        this.owner = owner;
    }

    @Override
    public CompoundTag serializeNBT() {
        if (pendingLoad != null) return pendingLoad.copy();
        // Brandon's Core snapshots new handlers before the block entity has a level.
        // Decoded stacks keep their original NBT in pendingLoad until world attachment.
        var registries = owner.getLevel() != null ? owner.getLevel().registryAccess()
                : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        serialize(output);
        return output.buildResult();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        pendingLoad = tag.copy();
        loadPending();
    }

    public void loadPending() {
        if (pendingLoad == null || owner.getLevel() == null) return;
        var tag = pendingLoad;
        pendingLoad = null;
        deserialize(TagValueInput.create(ProblemReporter.DISCARDING, owner.getLevel().registryAccess(), tag));
    }

    @Override public ManagedItemHandler setSlotLimit(int limit) { super.setSlotLimit(limit); return this; }
    @Override public ManagedItemHandler setStackValidator(java.util.function.Predicate<ItemStack> validator) { super.setStackValidator(validator); return this; }
    @Override public ManagedItemHandler setStackValidator(java.util.function.BiPredicate<Integer, ItemStack> validator) { super.setStackValidator(validator); return this; }

    /** Views share the underlying journals, so different sides roll back atomically. */
    public ResourceHandler<ItemResource> view(java.util.function.BiPredicate<Integer, ItemStack> insert,
                                             java.util.function.BiPredicate<Integer, ItemStack> extract) {
        return new net.neoforged.neoforge.transfer.DelegatingResourceHandler<>(automation) {
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
                net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                return insert.test(index, resource.toStack()) ? automation.insert(index, resource, amount, transaction) : 0;
            }
            @Override public int insert(ItemResource resource, int amount, TransactionContext transaction) {
                net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                int moved = 0;
                for (int i = 0; i < size() && moved < amount; i++) moved += insert(i, resource, amount - moved, transaction);
                return moved;
            }
            @Override public int extract(ItemResource resource, int amount, TransactionContext transaction) {
                net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                int moved = 0;
                for (int i = 0; i < size() && moved < amount; i++) moved += extract(i, resource, amount - moved, transaction);
                return moved;
            }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                return extract.test(index, getStackInSlot(index)) ? automation.extract(index, resource, amount, transaction) : 0;
            }
        };
    }

    private final class SlotJournal extends SnapshotJournal<ItemStack> {
        private final int slot;
        private SlotJournal(int slot) { this.slot = slot; }
        @Override protected ItemStack createSnapshot() { return stacks.get(slot).copy(); }
        @Override protected void revertToSnapshot(ItemStack snapshot) { stacks.set(slot, snapshot); }
        @Override protected void onRootCommit(ItemStack original) { onContentsChanged(slot); }
    }

    public final ResourceHandler<ItemResource> automation = new ResourceHandler<>() {
        @Override public int size() { return getSlots(); }
        @Override public ItemResource getResource(int index) { return ItemResource.of(stacks.get(index)); }
        @Override public long getAmountAsLong(int index) { return stacks.get(index).getCount(); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) {
            return !resource.isEmpty() && !isValid(index, resource) ? 0 : Math.min(getSlotLimit(index), resource.isEmpty() ? getSlotLimit(index) : resource.getMaxStackSize());
        }
        @Override public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && isItemValid(index, resource.toStack());
        }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            ItemStack existing = stacks.get(index);
            if (amount == 0 || !isValid(index, resource) || (!existing.isEmpty() && !resource.matches(existing))) return 0;
            int inserted = (int) Math.min(amount, Math.max(0, getCapacityAsLong(index, resource) - existing.getCount()));
            if (inserted > 0) {
                journals[index].updateSnapshots(transaction);
                stacks.set(index, resource.toStack(existing.getCount() + inserted));
            }
            return inserted;
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            ItemStack existing = stacks.get(index);
            if (amount == 0 || !resource.matches(existing)) return 0;
            int extracted = Math.min(amount, existing.getCount());
            if (extracted > 0) {
                journals[index].updateSnapshots(transaction);
                stacks.set(index, existing.copyWithCount(existing.getCount() - extracted));
            }
            return extracted;
        }
    };
}
