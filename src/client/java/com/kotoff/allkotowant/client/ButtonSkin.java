package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

/** Draws a low-contrast violet skin over the vanilla hitboxes/buttons. */
final class ButtonSkin {
    private ButtonSkin() {}

    static void draw(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                     String label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = hovered ? 0xFF30213F : 0xFF211A2D;
        int edge = hovered ? 0xFFB49AFF : 0xFF60427F;
        int accent = hovered ? 0xFFC7B4FF : 0xFF8B5CF6;
        g.fill(x, y, x + w, y + h, bg);
        g.outline(x, y, w, h, edge);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, accent);
        int tw = font.width(label);
        g.text(font, label, x + (w - tw) / 2, y + (h - 8) / 2 + 1,
                hovered ? 0xFFFFFFFF : 0xFFEAE2FF, false);
    }
}
