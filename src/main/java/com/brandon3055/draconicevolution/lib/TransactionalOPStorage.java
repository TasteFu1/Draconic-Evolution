package com.brandon3055.draconicevolution.lib;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/** Exposes the same long-valued storage to OP and transactional NeoForge energy callers. */
public interface TransactionalOPStorage extends IOPStorage, EnergyHandler {
    @Override
    default long getAmountAsLong() {
        return getOPStored();
    }

    @Override
    default long getCapacityAsLong() {
        return getMaxOPStored();
    }
}
