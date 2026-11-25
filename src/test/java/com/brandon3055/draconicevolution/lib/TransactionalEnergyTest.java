package com.brandon3055.draconicevolution.lib;

import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingInjector;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.lib.StackModuleContext;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.init.DEModules;
import com.brandon3055.draconicevolution.init.ItemData;
import com.brandon3055.draconicevolution.inventory.ManagedItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionalEnergyTest {
    @Test
    void adjacentMachinesCanChargeTheSameFusionInjectorWithoutLosingEnergy() {
        var injector = new TileFusionCraftingInjector(BlockPos.ZERO, DEContent.WYVERN_CRAFTING_INJECTOR.get().defaultBlockState());
        injector.energyRequired.set(200L);
        injector.chargeRate.set(150L);
        var energy = (EnergyHandler) injector.getCapability(CapabilityOP.OP, Direction.UP).orElseThrow();
        var otherSide = (EnergyHandler) injector.getCapability(CapabilityOP.OP, Direction.NORTH).orElseThrow();
        try (var tx = Transaction.openRoot()) {
            assertEquals(150, energy.insert(200, tx));
            try (var nested = Transaction.open(tx)) {
                assertEquals(50, otherSide.insert(100, nested));
            }
            assertEquals(150, energy.getAmountAsLong());
        }
        assertEquals(0L, injector.energy.get());
        try (var tx = Transaction.openRoot()) {
            assertEquals(120, energy.insert(120, tx));
            tx.commit();
        }
        assertEquals(120L, injector.energy.get());
    }

    @Test
    void modularEquipmentWritesEnergyBackToTheMachineSlot() {
        // NeoForge unit tests start without a world or its datapack-provided item defaults.
        DEContent.CAPACITOR_WYVERN.get().builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
        var owner = new TileFusionCraftingInjector(BlockPos.ZERO, DEContent.WYVERN_CRAFTING_INJECTOR.get().defaultBlockState());
        var inventory = new ManagedItemHandler(owner, 1);
        var capacitor = new ItemStack(DEContent.CAPACITOR_WYVERN.get());
        try (var host = DECapabilities.getHost(capacitor)) {
            host.addModule(DEModules.WYVERN_ENERGY.get().createEntity(),
                    new StackModuleContext(capacitor, null, EquipmentSlot.MAINHAND));
        }
        inventory.setStackInSlot(0, capacitor);
        var location = ItemAccess.forHandlerIndexStrict(inventory.automation, 0);
        var energy = location.getCapability(Capabilities.Energy.ITEM);
        assertNotNull(energy);
        try (var tx = Transaction.openRoot()) {
            assertEquals(10, energy.insert(10, tx));
        }
        assertEquals(0, energy.getAmountAsLong());
        try (var tx = Transaction.openRoot()) {
            assertEquals(20, energy.insert(20, tx));
            tx.commit();
        }
        assertEquals(20L, inventory.getStackInSlot(0).getOrDefault(ItemData.MODULAR_ENERGY_CAPABILITY, 0L).longValue());
    }
}
