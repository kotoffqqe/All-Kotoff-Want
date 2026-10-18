package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Decorative panel rendered before the interactive controls. */
final class PanelWidget extends AbstractWidget {
    private static final int PANEL = 0xF0181424;
    private static final int PURPLE = 0xFF8B5CF6;
    private static final int PURPLE_BRIGHT = 0xFFB49AFF;
    private long createdAt = System.nanoTime();

    PanelWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        long elapsed = Math.max(0L, System.nanoTime() - createdAt);
        float t = Math.min(1.0f, elapsed / 190_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0);
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + width, y + height, PANEL);
        graphics.outline(x, y, width, height, 0xFF45305F);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 4, PURPLE);
        graphics.fill(x + 16, y + 18, x + 19, y + 42, PURPLE_BRIGHT);
        graphics.fill(x + 18, y + 51, x + width - 18, y + 52, 0xFF382A4C);
        graphics.fill(x + 22, y + 119, x + width - 22, y + 120, 0xFF382A4C);
        int accentX = x + 2 + Math.round((width - 6) * eased);
        graphics.fill(accentX, y + 5, Math.min(x + width - 2, accentX + 2), y + 7, PURPLE_BRIGHT);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        // Decorative only.
    }
}
