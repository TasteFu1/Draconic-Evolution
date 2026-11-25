package com.brandon3055.draconicevolution.blocks.tileentity;

import com.brandon3055.brandonscore.lib.datamanager.BCSerializable;
import com.brandon3055.brandonscore.lib.entityfilter.EntityFilter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Preserves registry-dependent filter data while a block entity is being attached to its world. */
final class EntityFilterState implements BCSerializable<CompoundTag> {
    private final BlockEntity owner;
    private final EntityFilter filter;
    private CompoundTag pending;

    EntityFilterState(BlockEntity owner, EntityFilter filter) {
        this.owner = owner;
        this.filter = filter;
    }

    @Override
    public CompoundTag serializeNBT() {
        if (pending != null) return pending.copy();
        var registries = owner.getLevel() != null ? owner.getLevel().registryAccess()
                : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return filter.serializeNBT(registries);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        pending = tag.isEmpty() ? null : tag.copy();
        attach();
    }

    void attach() {
        if (pending != null && owner.getLevel() != null) {
            filter.deserializeNBT(owner.getLevel().registryAccess(), pending);
            pending = null;
        }
    }
}
