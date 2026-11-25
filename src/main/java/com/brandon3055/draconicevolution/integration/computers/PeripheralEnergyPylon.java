package com.brandon3055.draconicevolution.integration.computers;

import com.brandon3055.brandonscore.api.power.IOInfo;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

public class PeripheralEnergyPylon implements IPeripheral {
	
	TileEnergyPylon tile;
//	private LazyOptional<IPeripheral> self;
	
	public PeripheralEnergyPylon(TileEnergyPylon tile) {
        this.tile = tile;
    }
	
	@Override
	public String getType() {
		return "draconic_rf_storage";
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other instanceof PeripheralEnergyPylon o && tile == o.tile;
	}
	
	@LuaFunction(mainThread = true)
	public final long getEnergyStored() {
		return tile.opAdapter.getOPStored();
	}
	
	@LuaFunction(mainThread = true)
	public final long getMaxEnergyStored() {
		return tile.opAdapter.getMaxOPStored();
	}
	
	@LuaFunction(mainThread = true)
	public final Map<Object, Object> getEnergyStoredInNotation() {
		var core = tile.getCore();
		return notation(core == null ? 0L : core.energy.getStoredBig());
	}
	
	@LuaFunction(mainThread = true)
	public final Map<Object, Object> getMaxEnergyStoredInNotation() {
		long maxEnergy = tile.opAdapter.getMaxOPStored();
		return maxEnergy == Long.MAX_VALUE ? Map.of("coefficient", -1D, "exponent", -1) : notation(maxEnergy);
	}

	private static Map<Object, Object> notation(Number energy) {
		String[] parts = new DecimalFormat("0.######E0", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(energy).split("E", 2);
		return Map.of("coefficient", Double.parseDouble(parts[0]), "exponent", Integer.parseInt(parts[1]));
	}
	
	@LuaFunction(mainThread = true)
	public final long getTransferPerTick() {
		if (tile.coreOffset.isNull() || tile.getCore() == null) {
            return 0;
        }
        IOInfo io = tile.getCore().energy.getIOInfo();//transferRate.get();
		return io == null ? 0 : io.currentInput() - io.currentOutput();
	}

	@LuaFunction(mainThread = true)
	public final long getInputPerTick() {
		if (tile.coreOffset.isNull() || tile.getCore() == null) {
			return 0;
		}
		IOInfo io = tile.getCore().energy.getIOInfo();
		return io == null ? 0 : io.currentInput();
	}

	@LuaFunction(mainThread = true)
	public final long getOutputPerTick() {
		if (tile.coreOffset.isNull() || tile.getCore() == null) {
			return 0;
		}
		IOInfo io = tile.getCore().energy.getIOInfo();
		return io == null ? 0 : io.currentOutput();
	}
	
//	@Override
//	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
//		if (CCOCIntegration.isPeripheral(cap)) {
//			if (self == null) self = LazyOptional.of(() -> this);
//			return self.cast();
//		}
//		return LazyOptional.empty();
//	}
//
//	public void invalidate() {
//		if (self == null) return;
//		self.invalidate();
//		self = null;
//    }
}
