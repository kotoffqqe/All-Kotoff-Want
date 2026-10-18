package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

/** Animated dark-violet skin painted over real, clickable vanilla button hitboxes. */
final class ButtonSkin {
    private static long animationStarted = System.nanoTime();
    private ButtonSkin() {}

    static void beginAnimation() { animationStarted = System.nanoTime(); }

    static void draw(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                     String label, int mouseX, int mouseY) {
        long elapsed = Math.max(0L, System.nanoTime() - animationStarted);
        float t = Math.min(1.0f, elapsed / 240_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0f);
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = hovered ? 0xFF30213F : 0xFF211A2D;
        int edge = hovered ? 0xFFB49AFF : 0xFF60427F;
        int accent = hovered ? 0xFFC7B4FF : 0xFF8B5CF6;
        g.fill(x, y, x + w, y + h, bg);
        g.outline(x, y, w, h, edge);
        int stripeX = x + 1 + Math.round((w - 4) * eased);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, bg);
        g.fill(Math.max(x + 1, stripeX - 14), y + 1, Math.min(x + w - 1, stripeX + 1), y + 3, accent);
        int tw = font.width(label);
        g.text(font, label, x + (w - tw) / 2, y + (h - 8) / 2 + 1,
                hovered ? 0xFFFFFFFF : 0xFFEAE2FF, false);
    }
}
