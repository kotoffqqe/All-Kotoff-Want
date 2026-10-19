package com.kotoff.allkotowant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private static boolean draftsLoaded;

    private static Path draftsPath() { return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("all-kotoff-want-drafts.properties"); }
    private static void loadDrafts() {
        if (draftsLoaded) return; draftsLoaded = true; Properties p = new Properties();
        try { if (Files.exists(draftsPath())) { try (var in = Files.newInputStream(draftsPath())) { p.load(in); } for (String k : p.stringPropertyNames()) drafts.put(k, p.getProperty(k)); } } catch (IOException ignored) {}
    }
    private static void saveDrafts() {
        Properties p = new Properties(); p.putAll(drafts); Path path = draftsPath();
        try { Files.createDirectories(path.getParent()); try (var out = Files.newOutputStream(path)) { p.store(out, "All-Kotoff-Want saved drafts"); } } catch (IOException ignored) {}
    }

    private EditorContext() {}

    static void select(ItemStack stack, int slot, Screen background) {
        ItemStack next = stack == null ? ItemStack.EMPTY : stack;
        boolean sameTarget = draftStack != null && draftSlot == slot && !draftStack.isEmpty() && !next.isEmpty() && draftStack.getItem() == next.getItem();
        if (!sameTarget) { loadDrafts(); nameDraft = null; loreDraft = null; drafts.remove("__nameDraft"); drafts.remove("__loreDraft"); drafts.remove("item.count"); saveDrafts(); originalCount = Math.max(1, next.getCount()); }
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

    static String nameDraft() { loadDrafts(); return nameDraft != null ? nameDraft : drafts.get("__nameDraft"); }
    static void nameDraft(String value) { loadDrafts(); nameDraft = value; if (value == null) drafts.remove("__nameDraft"); else drafts.put("__nameDraft", value); saveDrafts(); }
    static String loreDraft() { loadDrafts(); return loreDraft != null ? loreDraft : drafts.get("__loreDraft"); }
    static void loreDraft(String value) { loadDrafts(); loreDraft = value; if (value == null) drafts.remove("__loreDraft"); else drafts.put("__loreDraft", value); saveDrafts(); }
    static String draft(String key, String fallback) { loadDrafts(); return drafts.getOrDefault(key, fallback); }
    static void setDraft(String key, String value) { loadDrafts(); drafts.put(key, value); saveDrafts(); }
    static void resetDraftPrefix(String prefix) { loadDrafts(); drafts.keySet().removeIf(k -> k.startsWith(prefix)); saveDrafts(); }

    static void clearDrafts() {
        nameDraft = null;
        loreDraft = null;
        drafts.clear(); saveDrafts();
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
