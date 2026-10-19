package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/** RGB/HEX dye editor for vanilla dyeable items (leather armor). */
public final class DyeEditorScreen extends Screen {
    private final Screen parent;
    private EditBox hexField;
    private String status = "HEX: #RRGGBB";
    private long openedAt;

    public DyeEditorScreen(Screen parent) {
        super(Component.literal("All-Kotoff-Want: Цвет брони"));
        this.parent = parent;
    }

    @Override protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(380, width - 24), x = (width - w) / 2, y = (height - 190) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 190));
        ItemStack stack = EditorContext.stack();
        int current = stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xA06540)).rgb();
        hexField = new EditBox(font, x + 22, y + 70, w - 44, 22, Component.literal("HEX"));
        hexField.setMaxLength(7);
        hexField.setValue(String.format("#%06X", current & 0xFFFFFF));
        hexField.setResponder(v -> EditorContext.setDraft("dye.hex", v));
        hexField.setValue(EditorContext.draft("dye.hex", hexField.getValue()));
        addRenderableWidget(hexField);
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить цвет")), b -> apply()).bounds(x + 22, y + 104, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сбросить цвет")), b -> reset()).bounds(x + 22, y + 132, (w - 50) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x + 28 + (w - 50) / 2, y + 132, (w - 50) / 2, 20).build());
    }

    private void apply() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty() || !stack.is(ItemTags.DYEABLE)) { status = UiText.t("Выбери кожаную броню или другой окрашиваемый предмет"); return; }
        String raw = hexField.getValue().trim().replace("#", "");
        try {
            if (raw.length() != 6) throw new NumberFormatException();
            int rgb = Integer.parseInt(raw, 16) & 0xFFFFFF;
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
            status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
        } catch (NumberFormatException e) {
            status = UiText.t("Введи HEX-цвет, например #AA33FF");
        }
    }

    private void reset() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) return;
        stack.remove(DataComponents.DYED_COLOR);
        EditorContext.setDraft("dye.hex", "#A06540");
        hexField.setValue("#A06540");
        status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3);
        int w = Math.min(380, width - 24), x = (width - w) / 2, y = (height - 190) / 2 + Math.round((1f - eased) * 12f);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 17, 0xFFF4F0FF, false);
        g.text(font, UiText.t("ЦВЕТ КОЖАНОЙ БРОНИ"), x + 22, y + 43, 0xFFB49AFF, false);
        g.text(font, UiText.t("Введи HEX, например #AA33FF"), x + 22, y + 59, 0xFFB4A9C9, false);
        ButtonSkin.draw(g, font, x + 22, y + 104, w - 44, 22, UiText.t("Применить цвет"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 132, (w - 50) / 2, 20, UiText.t("Сбросить цвет"), mx, my);
        ButtonSkin.draw(g, font, x + 28 + (w - 50) / 2, y + 132, (w - 50) / 2, 20, UiText.t("Назад"), mx, my);
        g.text(font, status, x + 22, y + 165, 0xFFF4F0FF, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
