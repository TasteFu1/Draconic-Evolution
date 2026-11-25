package com.brandon3055.draconicevolution.api.modules.items;




import com.brandon3055.draconicevolution.api.modules.Module;
import com.brandon3055.draconicevolution.integration.enderstorage.EnderStorageIntegration;

import com.brandon3055.draconicevolution.api.modules.data.NoData;

import com.brandon3055.draconicevolution.api.modules.items.ModuleItem;

import com.brandon3055.draconicevolution.init.ItemData;

import com.brandon3055.draconicevolution.integration.ModHelper;

import com.brandon3055.draconicevolution.lib.TagUtils26;

import java.util.List;

import java.util.Optional;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;

import net.minecraft.core.HolderLookup;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.item.component.CustomData;

import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.entity.BlockEntity;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;


































public class EnderCollectionModuleItem extends ModuleItem<NoData> {

    public EnderCollectionModuleItem(Properties properties, Supplier<Module<?>> moduleSupplier) {
        super(properties, moduleSupplier);
    }

    public EnderCollectionModuleItem(Supplier<Module<?>> moduleSupplier) {
        super(moduleSupplier);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();

        if (ModHelper.ENDERSTORAGE) {
            return bindFrequency(stack, level, context);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult bindFrequency(ItemStack stack, Level level, UseOnContext context) {
        BlockEntity tile = level.getBlockEntity(context.getClickedPos());
        if (EnderStorageIntegration.isChest(tile) && context.getPlayer() != null && context.getPlayer().isCrouching()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            CompoundTag tag = stack.getOrDefault(ItemData.ENDER_MODULE_FREQUENCY, CustomData.EMPTY).copyTag();
            tag.put("frequency", EnderStorageIntegration.bind(tile, context.getPlayer(), level.registryAccess()));
            stack.set(ItemData.ENDER_MODULE_FREQUENCY, CustomData.of(tag));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) { // port-26.1.2: InteractionResultHolder удалён
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        CompoundTag tag = stack.getOrDefault(ItemData.ENDER_MODULE_FREQUENCY, CustomData.EMPTY).copyTag();
        if (player.isCrouching() && ModHelper.ENDERSTORAGE && tag.contains("frequency")) {
            tag.remove("frequency");
            stack.set(ItemData.ENDER_MODULE_FREQUENCY, CustomData.of(tag));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, display, tooltip, flagIn);

        CompoundTag tag = stack.getOrDefault(ItemData.ENDER_MODULE_FREQUENCY, CustomData.EMPTY).copyTag();
        if (ModHelper.ENDERSTORAGE && tag.contains("frequency")) {
            addEnderStorageInfo(tag, tooltip, context.registries());
        }

        tooltip.accept(Component.translatable("module." + MODID + ".ender_storage.about").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("module." + MODID + ".ender_storage.about_compat").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("module." + MODID + ".ender_storage.about_compat2").withStyle(ChatFormatting.DARK_GRAY));
    }

    private void addEnderStorageInfo(CompoundTag tag, java.util.function.Consumer<Component> tooltip, HolderLookup.Provider registries) {
        EnderStorageIntegration.ownerName(tag, registries).ifPresent(name -> tooltip.accept(
                Component.translatable("module." + MODID + ".ender_storage.owner").withStyle(ChatFormatting.GRAY)
                        .append(": ").append(name.copy().withStyle(ChatFormatting.DARK_GREEN))));
        tooltip.accept(Component.translatable("module." + MODID + ".ender_storage.frequency").withStyle(ChatFormatting.GRAY)
                .append(": ").append(EnderStorageIntegration.tooltip(tag, registries).copy().withStyle(ChatFormatting.DARK_GREEN)));
    }
}
