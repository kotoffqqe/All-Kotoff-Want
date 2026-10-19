package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Draft-backed editor for mob custom names, using the same familiar color/format codes as item text. */
public final class MobNameStyleScreen extends Screen {
    private static final String[] COLORS = {"&c", "&6", "&e", "&a", "&b", "&d", "&#FFFFFF"};
    private static final String[] COLOR_LABELS = {"Красный", "Оранжевый", "Жёлтый", "Зелёный", "Голубой", "Розовый", "HEX"};
    private static final String[] FORMATS = {"&l", "&o", "&n", "&m", "&k", "&r"};
    private static final String[] FORMAT_LABELS = {"Жирный", "Курсив", "Подчёрк.", "Зачёрк.", "Обфускация", "Сброс"};
    private static final String[] FONTS = {"minecraft:default", "minecraft:uniform", "minecraft:alt"};
    private static final String[] FONT_LABELS = {"Стандартный", "Ровный", "Зачарованный"};
    private final Screen parent;
    private EditBox nameField;
    private int fontIndex;

    public MobNameStyleScreen(Screen parent) {
        super(Component.literal(UiText.t("Цвет и формат ника моба")));
        this.parent = parent;
    }

    @Override protected void init() {
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 286) / 2);
        addRenderableOnly(new PanelWidget(x, y, w, 286));
        String savedFont = EditorContext.draft("entity.namefont", "minecraft:default");
        fontIndex = 0;
        for (int i = 0; i < FONTS.length; i++) if (FONTS[i].equals(savedFont)) fontIndex = i;
        nameField = new EditBox(font, x + 20, y + 52, w - 40, 22, Component.literal(UiText.t("Ник моба")));
        nameField.setMaxLength(128);
        nameField.setValue(EditorContext.draft("entity.name", ""));
        nameField.setResponder(v -> EditorContext.setDraft("entity.name", v));
        nameField.setHint(Component.literal(UiText.t("Введи ник; коды цвета вставляй кнопками ниже")));
        addRenderableWidget(nameField);
        int gap = 4, inner = w - 40, bw = (inner - gap * 6) / 7;
        for (int i = 0; i < COLORS.length; i++) {
            final String code = COLORS[i];
            addRenderableWidget(Button.builder(Component.literal(COLOR_LABELS[i]), b -> insertCode(code))
                    .bounds(x + 20 + i * (bw + gap), y + 91, bw, 20).build());
        }
        int fw = (inner - gap * 5) / 6;
        for (int i = 0; i < FORMATS.length; i++) {
            final String code = FORMATS[i];
            addRenderableWidget(Button.builder(Component.literal(FORMAT_LABELS[i]), b -> insertCode(code))
                    .bounds(x + 20 + i * (fw + gap), y + 116, fw, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Шрифт: " + FONT_LABELS[fontIndex] + "  ›"), b -> {
            fontIndex = (fontIndex + 1) % FONTS.length;
            EditorContext.setDraft("entity.namefont", FONTS[fontIndex]);
            b.setMessage(Component.literal("Шрифт: " + FONT_LABELS[fontIndex] + "  ›"));
        }).bounds(x + 20, y + 147, inner, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Очистить ник")), b -> {
            nameField.setValue(""); EditorContext.setDraft("entity.name", "");
        }).bounds(x + 20, y + 202, (inner - 8) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Готово")), b -> back())
                .bounds(x + 28 + (inner - 8) / 2, y + 202, (inner - 8) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сбросить ник")), b -> {
            nameField.setValue(""); fontIndex = 0; EditorContext.setDraft("entity.name", "");
            EditorContext.setDraft("entity.namefont", FONTS[0]);
        }).bounds(x + 20, y + 230, inner, 20).build());
        ButtonSkin.hideVanillaButtons(this);
    }

    private void insertCode(String code) {
        if (nameField == null) return;
        String value = nameField.getValue(); int cursor = nameField.getCursorPosition();
        nameField.setValue(value.substring(0, cursor) + code + value.substring(cursor));
        nameField.setCursorPosition(cursor + code.length()); nameField.setFocused(true);
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 286) / 2);
        int gap = 4, inner = w - 40, bw = (inner - gap * 6) / 7, fw = (inner - gap * 5) / 6;
        g.text(font, UiText.t("ALL-KOTOFF-WANT"), x + 20, y + 15, 0xFFF4F0FF, false);
        g.text(font, UiText.t("НИК МОБА · ЦВЕТ / ФОРМАТ / ШРИФТ"), x + 20, y + 31, 0xFFB49AFF, false);
        g.text(font, UiText.t("ЦВЕТА"), x + 20, y + 78, 0xFFB4A9C9, false);
        for (int i = 0; i < COLOR_LABELS.length; i++) ButtonSkin.draw(g, font, x + 20 + i * (bw + gap), y + 91, bw, 20, COLOR_LABELS[i], mx, my);
        g.text(font, UiText.t("ФОРМАТИРОВАНИЕ"), x + 20, y + 106, 0xFFB4A9C9, false);
        for (int i = 0; i < FORMAT_LABELS.length; i++) ButtonSkin.draw(g, font, x + 20 + i * (fw + gap), y + 116, fw, 20, FORMAT_LABELS[i], mx, my);
        ButtonSkin.draw(g, font, x + 20, y + 147, inner, 22, "Шрифт: " + FONT_LABELS[fontIndex] + "  ›", mx, my);
        String preview = EditorContext.draft("entity.name", "");
        if (preview.isBlank()) preview = "Пример ника";
        g.text(font, StyledText.parse(preview), x + 20, y + 179, 0xFFF4F0FF, false);
        ButtonSkin.draw(g, font, x + 20, y + 202, (inner - 8) / 2, 20, UiText.t("Очистить ник"), mx, my);
        ButtonSkin.draw(g, font, x + 28 + (inner - 8) / 2, y + 202, (inner - 8) / 2, 20, UiText.t("Готово"), mx, my);
        ButtonSkin.draw(g, font, x + 20, y + 230, inner, 20, UiText.t("Сбросить ник"), mx, my);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
