package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Short, practical overview of the mod and its author. */
final class AboutModScreen extends Screen {
    private final Screen parent;
    AboutModScreen(Screen parent) { super(Component.literal(UiText.t("О моде"))); this.parent = parent; }

    @Override protected void init() {
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 240) / 2);
        addRenderableOnly(new PanelWidget(x, y, w, 240));
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> {
            if (minecraft != null) minecraft.setScreen(parent);
        }).bounds(x + 18, y + 204, w - 36, 20).build());
        ButtonSkin.hideVanillaButtons(this);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 240) / 2);
        g.text(font, UiText.t("ALL-KOTOFF-WANT"), x + 22, y + 16, 0xFFF4F0FF, false);
        g.text(font, UiText.t("О моде") + " · " + UiText.t("Кратко о возможностях"), x + 22, y + 34, 0xFFB49AFF, false);
        g.text(font, "• " + UiText.t("Предметы: поиск, выбор и библиотека"), x + 22, y + 64, 0xFFF4F0FF, false);
        g.text(font, "• " + UiText.t("Выдача предметов в выбранный слот хотбара"), x + 22, y + 84, 0xFFF4F0FF, false);
        g.text(font, "• " + UiText.t("Имя, описание, чары, атрибуты, цвет брони и стаки"), x + 22, y + 104, 0xFFF4F0FF, false);
        g.text(font, "• " + UiText.t("Мобы: здоровье, имя, броня, оружие и эффекты"), x + 22, y + 124, 0xFFF4F0FF, false);
        g.text(font, "• " + UiText.t("Черновики сохраняются между открытиями"), x + 22, y + 144, 0xFFF4F0FF, false);
        g.text(font, "• " + UiText.t("Сброс отдельной вкладки к исходным настройкам"), x + 22, y + 164, 0xFFF4F0FF, false);
        g.text(font, UiText.t("Автор: Дмитрий Котов (nekotoff)"), x + 22, y + 186, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 18, y + 204, w - 36, 20, UiText.t("Назад"), mx, my);
    }

    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
