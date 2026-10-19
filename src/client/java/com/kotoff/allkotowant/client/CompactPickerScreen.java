package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.IntConsumer;

/** Compact click-to-open picker with visible rows and mouse-wheel scrolling. */
final class CompactPickerScreen extends Screen {
    private final Screen parent;
    private final String title;
    private final List<String> options;
    private final int selected;
    private final IntConsumer onSelect;
    private int first;
    private static final int ROW_H = 20;
    private static final int VISIBLE = 7;

    CompactPickerScreen(Screen parent, String title, List<String> options, int selected, IntConsumer onSelect) {
        super(Component.literal(title));
        this.parent = parent; this.title = title; this.options = List.copyOf(options);
        this.selected = selected; this.onSelect = onSelect;
        this.first = Math.max(0, Math.min(selected - VISIBLE / 2, Math.max(0, options.size() - VISIBLE)));
    }

    @Override protected void init() {}

    private int panelW() { return Math.min(340, width - 24); }
    private int panelH() { return 54 + Math.min(VISIBLE, options.size()) * ROW_H + 28; }
    private int left() { return (width - panelW()) / 2; }
    private int top() { return (height - panelH()) / 2; }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        parent.extractRenderState(g, mx, my, delta);
        int x=left(), y=top(), w=panelW(), h=panelH();
        g.fill(0,0,width,height,0x9907090F);
        g.fill(x,y,x+w,y+h,0xF016171D); g.outline(x,y,w,h,0xFF454752);
        g.fill(x+1,y+1,x+w-1,y+3,0xFF9B8AFB);
        g.text(font,title,x+16,y+13,0xFFF5F5FA,false);
        g.text(font,UiText.t("Выбери вариант · колесо мыши — прокрутка"),x+16,y+29,0xFF9A9DAA,false);
        int rows=Math.min(VISIBLE,options.size());
        for(int r=0;r<rows;r++) {
            int i=first+r, ry=y+48+r*ROW_H;
            boolean hover=mx>=x+10&&mx<x+w-10&&my>=ry&&my<ry+ROW_H;
            if(i==selected) g.fill(x+10,ry,x+w-10,ry+ROW_H,0xFF353344);
            else if(hover) g.fill(x+10,ry,x+w-10,ry+ROW_H,0xFF292B34);
            g.text(font, (i==selected?"●  ":"   ")+options.get(i),x+17,ry+6,i==selected?0xFFD9D2FF:(hover?0xFFFFFFFF:0xFFE4E5EB),false);
        }
        g.text(font,(first+1)+"–"+Math.min(first+rows,options.size())+" / "+options.size()+"     "+UiText.t("Esc — назад"),x+16,y+h-18,0xFF9A9DAA,false);
    }

    @Override public boolean mouseClicked(double mx,double my,int button) {
        if(button!=0) return super.mouseClicked(mx,my,button);
        int x=left(),y=top(),w=panelW(),rows=Math.min(VISIBLE,options.size());
        if(mx>=x+10&&mx<x+w-10&&my>=y+48&&my<y+48+rows*ROW_H) {
            int i=first+(int)((my-(y+48))/ROW_H);
            if(i>=0&&i<options.size()) { onSelect.accept(i); if(minecraft!=null)minecraft.setScreen(parent); return true; }
        }
        if(mx<x||mx>x+w||my<y||my>y+panelH()) { if(minecraft!=null)minecraft.setScreen(parent); return true; }
        return true;
    }

    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical) {
        int max=Math.max(0,options.size()-VISIBLE);
        first=Math.max(0,Math.min(max,first+(vertical<0?1:-1)));
        return true;
    }
    @Override public void onClose(){if(minecraft!=null)minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
