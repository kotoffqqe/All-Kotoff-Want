package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;

/** Compact editor for item name/lore with quick legacy color and formatting codes. */
public final class TextEditorScreen extends Screen {
    private final Screen parent;
    private EditBox nameField;
    private EditBox loreField;
    private EditBox lastTarget;
    private String status = UiText.t("Изменения применяются к выбранному предмету");

    public TextEditorScreen(Screen parent) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Текст и описание")));
        this.parent = parent;
    }

    @Override protected void init() {
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 300) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 300));
        ItemStack stack = EditorContext.stack();
        String currentName = "", currentLore = "";
        if (!stack.isEmpty()) {
            Component custom = stack.get(DataComponents.CUSTOM_NAME);
            if (custom != null) currentName = custom.getString();
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null) currentLore = String.join(" | ", lore.lines().stream().map(Component::getString).toList());
        }
        String nameDraft = EditorContext.nameDraft();
        nameField = new EditBox(font, x + 22, y + 56, w - 44, 22, Component.literal(UiText.t("Название предмета")));
        nameField.setMaxLength(128);
        nameField.setValue(nameDraft != null ? nameDraft : currentName);
        nameField.setResponder(EditorContext::nameDraft);
        nameField.setHint(Component.literal(UiText.t("Имя без принудительного верхнего регистра")));
        addRenderableWidget(nameField);
        lastTarget = nameField;

        String loreDraft = EditorContext.loreDraft();
        loreField = new EditBox(font, x + 22, y + 100, w - 44, 38, Component.literal(UiText.t("Описание предмета")));
        loreField.setMaxLength(1024);
        loreField.setValue(loreDraft != null ? loreDraft : currentLore);
        loreField.setResponder(EditorContext::loreDraft);
        loreField.setHint(Component.literal(UiText.t("Строка 1 | Строка 2 | Строка 3")));
        addRenderableWidget(loreField);

        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&d", "&#FFFFFF"};
        String[] colorNames = {"Красн.", "Оранж.", "Жёлт.", "Зелён.", "Голуб.", "Розов.", "HEX"};
        int gap = 4, innerW = w - 44, bw = (innerW - gap * 6) / 7;
        for (int i = 0; i < colors.length; i++) {
            final String code = colors[i]; final String label = colorNames[i];
            int bx = x + 22 + i * (bw + gap);
            addRenderableWidget(Button.builder(Component.literal(label), b -> insertCode(code)).bounds(bx, y + 151, bw, 18).build());
        }
        String[] formats = {"&l", "&o", "&n", "&m", "&r"};
        String[] formatNames = {"Жирный", "Курсив", "Подчёрк.", "Зачёрк.", "Сброс"};
        int fw = (innerW - gap * 4) / 5;
        for (int i = 0; i < formats.length; i++) {
            final String code = formats[i]; final String label = formatNames[i];
            int bx = x + 22 + i * (fw + gap);
            addRenderableWidget(Button.builder(Component.literal(label), b -> insertCode(code)).bounds(bx, y + 173, fw, 18).build());
        }
        int half = (w - 52) / 2;
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить текст")), b -> applyName()).bounds(x + 22, y + 202, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить описание")), b -> applyLore()).bounds(x + 30 + half, y + 202, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Очистить текст и описание")), b -> clear()).bounds(x + 22, y + 230, w - 44, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x + 22, y + 254, (w - 52) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сбросить вкладку")), b -> { EditorContext.nameDraft(""); EditorContext.loreDraft(""); if (minecraft != null) minecraft.setScreen(new TextEditorScreen(parent)); })
                .bounds(x + 30 + (w - 52) / 2, y + 254, (w - 52) / 2, 20).build());
            ButtonSkin.hideVanillaButtons(this);
    }

    private void insertCode(String code) {
        EditBox target = lastTarget != null ? lastTarget : (loreField != null ? loreField : nameField);
        if (target == null) return;
        String value = target.getValue();
        int cursor = target.getCursorPosition();
        target.setValue(value.substring(0, cursor) + code + value.substring(cursor));
        target.setCursorPosition(cursor + code.length());
        target.setFocused(true);
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

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (nameField != null && event.x() >= nameField.getX() && event.x() <= nameField.getX() + nameField.getWidth()
                && event.y() >= nameField.getY() && event.y() <= nameField.getY() + nameField.getHeight()) lastTarget = nameField;
        else if (loreField != null && event.x() >= loreField.getX() && event.x() <= loreField.getX() + loreField.getWidth()
                && event.y() >= loreField.getY() && event.y() <= loreField.getY() + loreField.getHeight()) lastTarget = loreField;
        return super.mouseClicked(event, doubleClick);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 300) / 2;
        int innerW = w - 44, gap = 4, bw = (innerW - gap * 6) / 7, fw = (innerW - gap * 4) / 5, half = (w - 52) / 2;
        g.text(font, UiText.t("ALL-KOTOFF-WANT"), x + 22, y + 15, 0xFFF4F0FF, false);
        g.text(font, UiText.t("ТЕКСТ И ОПИСАНИЕ"), x + 22, y + 32, 0xFFB49AFF, false);
        g.text(font, UiText.t("НАЗВАНИЕ"), x + 22, y + 46, 0xFFB4A9C9, false);
        g.text(font, UiText.t("ОПИСАНИЕ · разделитель строк: |"), x + 22, y + 88, 0xFFB4A9C9, false);
        String[] colorNames = {"Красн.", "Оранж.", "Жёлт.", "Зелён.", "Голуб.", "Розов.", "HEX"};
        for (int i = 0; i < colorNames.length; i++) ButtonSkin.draw(g, font, x + 22 + i * (bw + gap), y + 151, bw, 18, colorNames[i], mx, my);
        String[] formatNames = {"Жирный", "Курсив", "Подчёрк.", "Зачёрк.", "Сброс"};
        for (int i = 0; i < formatNames.length; i++) ButtonSkin.draw(g, font, x + 22 + i * (fw + gap), y + 173, fw, 18, formatNames[i], mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 202, half, 22, UiText.t("Применить текст"), mx, my);
        ButtonSkin.draw(g, font, x + 30 + half, y + 202, half, 22, UiText.t("Применить описание"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 230, w - 44, 20, UiText.t("Очистить текст и описание"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 254, (w - 52) / 2, 20, UiText.t("Назад"), mx, my);
        ButtonSkin.draw(g, font, x + 30 + (w - 52) / 2, y + 254, (w - 52) / 2, 20, "Сбросить вкладку", mx, my);
        g.text(font, status, x + 22, y + 282, 0xFFF4F0FF, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
