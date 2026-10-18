package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Applies an enchantment by registry id, allowing levels above vanilla limits. */
public final class EnchantmentEditorScreen extends Screen {
    private static final int TEXT = 0xFFF4F0FF;
    private static final int MUTED = 0xFFB4A9C9;
    private final Screen parent;
    private EditBox idField;
    private EditBox levelField;
    private String status = "Use an id like minecraft:sharpness";
    private long openedAt;

    public EnchantmentEditorScreen(Screen parent) {
        super(Component.literal("All-Kotoff-Want: Enchantments"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(430, width - 24);
        int x = (width - w) / 2;
        addRenderableOnly(new PanelWidget(x, (height - 230) / 2, w, 230));
        idField = new EditBox(font, x + 22, height / 2 - 35, w - 44, 22, Component.literal("Enchantment id"));
        idField.setMaxLength(128);
        idField.setValue("minecraft:sharpness");
        idField.setHint(Component.literal("minecraft:sharpness"));
        addRenderableWidget(idField);
        levelField = new EditBox(font, x + 22, height / 2 - 2, w - 44, 22, Component.literal("Level"));
        levelField.setMaxLength(5);
        levelField.setValue("1000");
        levelField.setHint(Component.literal("1–32767"));
        addRenderableWidget(levelField);
        addRenderableWidget(Button.builder(Component.literal("APPLY ENCHANTMENT"), b -> apply())
                .bounds(x + 22, height / 2 + 30, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("REMOVE THIS ENCHANTMENT"), b -> remove())
                .bounds(x + 22, height / 2 + 58, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("BACK"), b -> back())
                .bounds(x + 22, height / 2 + 86, w - 44, 20).build());
    }

    private ItemStack held() {
        return EditorContext.stack();
    }

    private Holder<Enchantment> resolve(String raw) {
        if (minecraft == null || minecraft.level == null) return null;
        Identifier id = Identifier.tryParse(raw.trim());
        if (id == null) return null;
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
        return minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
    }

    private void apply() {
        ItemStack stack = held();
        if (stack.isEmpty()) { status = "Hold an item first"; return; }
        int level;
        try { level = Integer.parseInt(levelField.getValue().trim()); }
        catch (NumberFormatException ex) { status = "Level must be a number from 1 to 32767"; return; }
        if (level < 1 || level > 32767) { status = "Level must be from 1 to 32767"; return; }
        Holder<Enchantment> enchantment = resolve(idField.getValue());
        if (enchantment == null) { status = "Unknown enchantment id"; return; }
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        mutable.set(enchantment, level);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        status = "Applied " + idField.getValue() + " " + level + ". " + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void remove() {
        ItemStack stack = held();
        if (stack.isEmpty()) { status = "Hold an item first"; return; }
        Holder<Enchantment> enchantment = resolve(idField.getValue());
        if (enchantment == null) { status = "Unknown enchantment id"; return; }
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        mutable.set(enchantment, 0);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        status = "Removed " + idField.getValue() + ". " + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EditorContext.renderBackdrop(this, graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1.0f, elapsed / 190_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0f);
        int w = Math.min(430, width - 24);
        int x = (width - w) / 2;
        int y = (height - 230) / 2 + Math.round((1.0f - eased) * 12.0f);
        ButtonSkin.draw(graphics, font, x + 22, height / 2 + 30, w - 44, 22, "APPLY ENCHANTMENT", mouseX, mouseY);
        ButtonSkin.draw(graphics, font, x + 22, height / 2 + 58, w - 44, 22, "REMOVE THIS ENCHANTMENT", mouseX, mouseY);
        ButtonSkin.draw(graphics, font, x + 22, height / 2 + 86, w - 44, 20, "BACK", mouseX, mouseY);
        graphics.text(font, "ALL-KOTOFF-WANT", x + 28, y + 17, TEXT, false);
        graphics.text(font, "ENCHANTMENT LAB  /  03", x + 28, y + 30, MUTED, false);
        graphics.text(font, "REGISTRY ID", x + 22, y + 48, 0xFFB49AFF, false);
        graphics.text(font, "LEVEL  ·  1–32767", x + 22, y + 81, 0xFFB49AFF, false);
        graphics.text(font, status, x + 22, y + 197, TEXT, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
