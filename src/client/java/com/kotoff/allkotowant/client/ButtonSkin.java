package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

/** Restrained, low-contrast skin painted over real clickable button hitboxes. */
final class ButtonSkin {
    private ButtonSkin() {}
    static void beginAnimation() {}
    /** Keep the real widget hitboxes/actions, but render buttons only once using this skin. */
    static void hideVanillaButtons(Screen screen) {
        for (var child : screen.children()) {
            if (child instanceof net.minecraft.client.gui.components.Button button) button.setAlpha(0.0f);
        }
    }
    static void draw(GuiGraphicsExtractor g,Font font,int x,int y,int w,int h,String label,int mouseX,int mouseY) {
        boolean hovered=mouseX>=x&&mouseX<x+w&&mouseY>=y&&mouseY<y+h;
        int bg=hovered?0xFF343640:0xFF24262D;
        int edge=hovered?0xFF9B8AFB:0xFF41434D;
        g.fill(x,y,x+w,y+h,bg); g.outline(x,y,w,h,edge);
        if(hovered) g.fill(x+1,y+1,x+w-1,y+2,0xFF9B8AFB);
        int tw=font.width(label);
        g.text(font,label,x+(w-tw)/2,y+(h-8)/2+1,hovered?0xFFFFFFFF:0xFFE4E5EB,false);
    }
}
