package com.brandon3055.draconicevolution.lib;

import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.inventory.InventoryDynamic;
import com.brandon3055.brandonscore.utils.TargetPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Component formats from DE 1.21.1 for value types supplied by Brandon's Core. */
public final class CoreDataCodecs {
    private CoreDataCodecs() {}

    public static final Codec<TechLevel> TECH_LEVEL = Codec.STRING.comapFlatMap(
            name -> Arrays.stream(TechLevel.values()).filter(level -> level.name().equalsIgnoreCase(name))
                    .findFirst().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown tech level " + name)),
            level -> level.name().toLowerCase(Locale.ROOT));
    public static final StreamCodec<ByteBuf, TechLevel> TECH_LEVEL_STREAM = ByteBufCodecs.idMapper(TechLevel::byIndex, level -> level.index);

    private static final Codec<Vec2> FACING = RecordCodecBuilder.create(b -> b.group(
            Codec.FLOAT.fieldOf("x").forGetter(v -> v.x),
            Codec.FLOAT.fieldOf("y").forGetter(v -> v.y)
    ).apply(b, Vec2::new));
    public static final Codec<TargetPos> TARGET_POS = RecordCodecBuilder.create(b -> b.group(
            Vector3.CODEC.fieldOf("pos").forGetter(TargetPos::getPos),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(TargetPos::getDimension),
            FACING.optionalFieldOf("facing").forGetter(CoreDataCodecs::facing)
    ).apply(b, CoreDataCodecs::target));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetPos> TARGET_POS_STREAM = ByteBufCodecs.fromCodecWithRegistries(TARGET_POS);

    private static TargetPos target(Vector3 pos, ResourceKey<Level> dimension, Optional<Vec2> facing) {
        return facing.map(v -> new TargetPos(pos, dimension, v.x, v.y))
                .orElseGet(() -> new TargetPos(pos, dimension).setIncludeHeading(false));
    }

    private static Optional<Vec2> facing(TargetPos target) {
        return target.writeToNBT().getBooleanOr("heading", false)
                ? Optional.of(new Vec2(target.getPitch(), target.getYaw())) : Optional.empty();
    }

    public static final Codec<InventoryDynamic> INVENTORY = RecordCodecBuilder.create(b -> b.group(
            ItemStack.CODEC.listOf().fieldOf("stacks").forGetter(InventoryDynamic::getStacks),
            Codec.INT.fieldOf("xp").forGetter(v -> v.xp)
    ).apply(b, CoreDataCodecs::inventory));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryDynamic> INVENTORY_STREAM = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), InventoryDynamic::getStacks,
            ByteBufCodecs.INT, v -> v.xp, CoreDataCodecs::inventory);

    private static InventoryDynamic inventory(List<ItemStack> stacks, int xp) {
        InventoryDynamic result = new InventoryDynamic();
        result.getStacks().addAll(stacks);
        result.xp = xp;
        return result;
    }

    public static InventoryDynamic copy(InventoryDynamic inventory) {
        return inventory(inventory.getStacks().stream().map(ItemStack::copy).toList(), inventory.xp);
    }
}
