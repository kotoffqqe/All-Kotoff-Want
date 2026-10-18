package com.kotoff.allkotowant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

/** Shared target stack and inventory backdrop for every editor page. */
final class EditorContext {
    private static ItemStack target = ItemStack.EMPTY;
    private static int menuSlot = -1;
    private static boolean hasTarget;
    private static Screen backdrop;

    private EditorContext() {}

    static void select(ItemStack stack, int slot, Screen background) {
        target = stack == null ? ItemStack.EMPTY : stack;
        hasTarget = true;
        menuSlot = slot;
        backdrop = background;
    }

    static ItemStack stack() {
        if (hasTarget) return target;
        Minecraft client = Minecraft.getInstance();
        return client.player == null ? ItemStack.EMPTY : client.player.getMainHandItem();
    }

    static int slot() { return menuSlot; }
    static Screen backdrop() { return backdrop; }

    static void renderBackdrop(Screen current, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Screen background = backdrop;
        if (background != null && background != current) {
            background.extractRenderState(graphics, mouseX, mouseY, delta);
        }
    }

    static void clear() {
        target = ItemStack.EMPTY;
        hasTarget = false;
        menuSlot = -1;
        backdrop = null;
    }
}
