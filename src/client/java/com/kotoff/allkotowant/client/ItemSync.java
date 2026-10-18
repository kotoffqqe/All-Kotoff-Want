package com.kotoff.allkotowant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Sends edited hotbar stacks through Minecraft's normal creative inventory path.
 * Vanilla servers do not accept arbitrary client-authored item components in survival.
 */
final class ItemSync {
    private ItemSync() {}

    static boolean syncIfCreative(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null || stack.isEmpty() || !client.player.isCreative()) {
            return false;
        }
        int hotbarSlot = client.player.getInventory().getSelectedSlot();
        // Creative inventory protocol maps hotbar slots to container slots 36..44.
        client.gameMode.handleCreativeModeItemAdd(stack.copy(), hotbarSlot + 36);
        return true;
    }

    static String resultMessage(boolean synced) {
        return synced
                ? "Applied and sent to server (creative mode)"
                : "Applied locally only; creative mode/server support required to save";
    }
}
