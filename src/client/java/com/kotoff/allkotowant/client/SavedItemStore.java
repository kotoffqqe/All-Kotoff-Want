package com.kotoff.allkotowant.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Local client-side library. It lives in the game's config folder, not in a world save. */
final class SavedItemStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<ItemStack> ITEMS = new ArrayList<>();
    private static boolean loaded;
    private SavedItemStore() {}

    private static Path path() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("all-kotoff-want-items.json");
    }
    static void load() {
        if (loaded) return;
        loaded = true;
        Path path = path();
        if (!Files.exists(path)) return;
        try {
            JsonElement root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!root.isJsonArray()) return;
            var ops = ops();
            for (JsonElement element : root.getAsJsonArray()) {
                try {
                    ItemStack stack = ItemStack.CODEC.parse(ops, element).getOrThrow();
                    if (!stack.isEmpty()) ITEMS.add(stack);
                } catch (Exception ignored) { }
            }
        } catch (Exception ignored) { }
    }
    private static void saveFile() {
        JsonArray array = new JsonArray();
        var ops = ops();
        for (ItemStack stack : ITEMS) {
            try { array.add(ItemStack.CODEC.encodeStart(ops, stack).getOrThrow()); }
            catch (Exception ignored) { }
        }
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(array), StandardCharsets.UTF_8);
        } catch (IOException ignored) { }
    }
    private static com.mojang.serialization.DynamicOps<JsonElement> ops() {
        var client = Minecraft.getInstance();
        return client.level != null ? RegistryOps.create(JsonOps.INSTANCE, client.level.registryAccess()) : JsonOps.INSTANCE;
    }
    static List<ItemStack> all() { load(); return ITEMS.stream().map(ItemStack::copy).toList(); }
    static void add(ItemStack stack) {
        load();
        if (stack == null || stack.isEmpty()) return;
        ITEMS.add(stack.copy()); saveFile();
    }
    static void remove(int index) {
        load(); if (index >= 0 && index < ITEMS.size()) { ITEMS.remove(index); saveFile(); }
    }
    static int size() { load(); return ITEMS.size(); }
}
