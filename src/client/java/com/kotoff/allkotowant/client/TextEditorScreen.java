package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;

/** Compact combined editor for custom item name and lore. */
public final class TextEditorScreen extends Screen {
    private final Screen parent;
    private EditBox nameField;
    private EditBox loreField;
    private String status = UiText.t("Изменения применяются к выбранному предмету");
    private long openedAt;

    public TextEditorScreen(Screen parent) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Текст и описание")));
        this.parent = parent;
    }

    @Override protected void init() {
        openedAt = System.nanoTime();
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 250) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 250));
        ItemStack stack = EditorContext.stack();
        String currentName = "";
        String currentLore = "";
        if (!stack.isEmpty()) {
            Component custom = stack.get(DataComponents.CUSTOM_NAME);
            if (custom != null) currentName = custom.getString();
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null) currentLore = String.join(" | ", lore.lines().stream().map(Component::getString).toList());
        }
        String nameDraft = EditorContext.nameDraft();
        nameField = new EditBox(font, x + 22, y + 58, w - 44, 22, Component.literal(UiText.t("Название предмета")));
        nameField.setMaxLength(128);
        nameField.setValue(nameDraft != null ? nameDraft : currentName);
        nameField.setResponder(EditorContext::nameDraft);
        nameField.setHint(Component.literal(UiText.t("Имя без принудительного верхнего регистра")));
        addRenderableWidget(nameField);

        String loreDraft = EditorContext.loreDraft();
        loreField = new EditBox(font, x + 22, y + 105, w - 44, 42, Component.literal(UiText.t("Описание предмета")));
        loreField.setMaxLength(1024);
        loreField.setValue(loreDraft != null ? loreDraft : currentLore);
        loreField.setResponder(EditorContext::loreDraft);
        loreField.setHint(Component.literal(UiText.t("Строка 1 | Строка 2 | Строка 3")));
        addRenderableWidget(loreField);

        int half = (w - 52) / 2;
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить текст")), b -> applyName()).bounds(x + 22, y + 166, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить описание")), b -> applyLore()).bounds(x + 30 + half, y + 166, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Очистить текст и описание")), b -> clear()).bounds(x + 22, y + 194, w - 44, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x + 22, y + 218, w - 44, 20).build());
    }

    private void applyName() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        String value = nameField.getValue();
        if (value.isEmpty()) stack.remove(DataComponents.CUSTOM_NAME);
        else stack.set(DataComponents.CUSTOM_NAME, StyledText.parse(value));
        status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void applyLore() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        List<Component> lines = new ArrayList<>();
        for (String line : loreField.getValue().split("\\|", -1)) if (!line.isBlank()) lines.add(StyledText.parse(line));
        if (lines.isEmpty()) stack.remove(DataComponents.LORE);
        else stack.set(DataComponents.LORE, new ItemLore(lines));
        status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void clear() {
        nameField.setValue(""); loreField.setValue("");
        EditorContext.nameDraft(""); EditorContext.loreDraft("");
        ItemStack stack = EditorContext.stack();
        if (!stack.isEmpty()) { stack.remove(DataComponents.CUSTOM_NAME); stack.remove(DataComponents.LORE); status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack)); }
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 250) / 2;
        int half = (w - 52) / 2;
        g.text(font, "ALL-KOTOFF-WANT", x + 22, y + 17, 0xFFF4F0FF, false);
        g.text(font, UiText.t("ТЕКСТ И ОПИСАНИЕ"), x + 22, y + 34, 0xFFB49AFF, false);
        g.text(font, UiText.t("НАЗВАНИЕ"), x + 22, y + 48, 0xFFB4A9C9, false);
        g.text(font, UiText.t("ОПИСАНИЕ · разделитель строк: |"), x + 22, y + 94, 0xFFB4A9C9, false);
        ButtonSkin.draw(g, font, x + 22, y + 166, half, 22, UiText.t("Применить текст"), mx, my);
        ButtonSkin.draw(g, font, x + 30 + half, y + 166, half, 22, UiText.t("Применить описание"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 194, w - 44, 20, UiText.t("Очистить текст и описание"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 218, w - 44, 20, UiText.t("Назад"), mx, my);
        g.text(font, status, x + 22, y + 238, 0xFFF4F0FF, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
