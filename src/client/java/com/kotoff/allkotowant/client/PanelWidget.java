package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Minimal graphite panel with a restrained lavender accent. */
final class PanelWidget extends AbstractWidget {
    private static final int PANEL = 0xB816171D;
    private static final int ACCENT = 0xFF9B8AFB;
    PanelWidget(int x, int y, int width, int height) { super(x,y,width,height,Component.empty()); }
    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        int x=getX(), y=getY();
        g.fill(x,y,x+width,y+height,PANEL);
        g.outline(x,y,width,height,0xFF3A3C46);
        g.fill(x+1,y+1,x+width-1,y+3,ACCENT);
        g.fill(x+18,y+48,x+width-18,y+49,0xFF2B2D35);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
