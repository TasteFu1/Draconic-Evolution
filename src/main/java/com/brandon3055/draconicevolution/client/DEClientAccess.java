package com.brandon3055.draconicevolution.client;

import com.brandon3055.draconicevolution.client.gui.DislocatorGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Client implementations called by shared item and module classes. */
public final class DEClientAccess {
    private DEClientAccess() {}

    public static List<Component> tooltip(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        return stack.getTooltipLines(Item.TooltipContext.of(client.level), client.player,
                client.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public static net.minecraft.world.level.Level level() {
        return Minecraft.getInstance().level;
    }

    public static Player player() {
        return Minecraft.getInstance().player;
    }

    public static void openDislocator(ItemStack stack, Player player) {
        Minecraft.getInstance().setScreen(new DislocatorGui.Screen(stack.getHoverName(), player));
    }
}
