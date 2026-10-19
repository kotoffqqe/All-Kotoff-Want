package com.kotoff.allkotowant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

/** Shared target stack and inventory backdrop for every editor page. */
final class EditorContext {
    private static ItemStack target = ItemStack.EMPTY;
    private static int menuSlot = -1;
    private static boolean hasTarget;
    private static Screen backdrop;
    private static ItemStack draftStack;
    private static int draftSlot = Integer.MIN_VALUE;
    private static int originalCount = 1;
    private static String nameDraft;
    private static String loreDraft;
    private static final Map<String, String> drafts = new HashMap<>();

    private EditorContext() {}

    static void select(ItemStack stack, int slot, Screen background) {
        ItemStack next = stack == null ? ItemStack.EMPTY : stack;
        boolean sameTarget = draftStack != null && draftSlot == slot && !draftStack.isEmpty() && !next.isEmpty() && draftStack.getItem() == next.getItem();
        if (!sameTarget) { nameDraft = null; loreDraft = null; drafts.clear(); originalCount = Math.max(1, next.getCount()); }
        draftStack = next.copy();
        draftSlot = slot;
        target = next;
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
    static int originalCount() { return originalCount; }

    static String nameDraft() { return nameDraft; }
    static void nameDraft(String value) { nameDraft = value; }
    static String loreDraft() { return loreDraft; }
    static void loreDraft(String value) { loreDraft = value; }
    static String draft(String key, String fallback) { return drafts.getOrDefault(key, fallback); }
    static void setDraft(String key, String value) { drafts.put(key, value); }

    static void clearDrafts() {
        nameDraft = null;
        loreDraft = null;
        drafts.clear();
        draftStack = target == null ? ItemStack.EMPTY : target.copy();
        draftSlot = menuSlot;
    }
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
        // Keep unsaved text drafts so reopening the editor for the same stack does not erase input.
    }
}
