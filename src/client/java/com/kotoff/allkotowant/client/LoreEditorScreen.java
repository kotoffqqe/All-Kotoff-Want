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

/** Edits item lore; one line per row. */
public final class LoreEditorScreen extends Screen {
    private static final int TEXT = 0xFFF4F0FF;
    private static final int MUTED = 0xFFB4A9C9;
    private final Screen parent;
    private EditBox loreField;
    private String status = UiText.t("Разделяй строки символом |");
    private long openedAt;

    public LoreEditorScreen(Screen parent) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Описание")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(430, width - 24);
        int x = (width - w) / 2;
        int top=(height-290)/2;
        addRenderableOnly(new PanelWidget(x, top, w, 290));
        String existing = "";
        ItemStack stack = held();
        if (!stack.isEmpty()) {
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null) existing = String.join(" | ", lore.lines().stream().map(Component::getString).toList());
        }
        loreField = new EditBox(font, x + 22, top + 58, w - 44, 54, Component.literal(UiText.t("Описание предмета")));
        loreField.setMaxLength(1024);
        String draft = EditorContext.loreDraft();
        loreField.setValue(draft != null ? draft : existing);
        loreField.setResponder(EditorContext::loreDraft);
        loreField.setHint(Component.literal(UiText.t("Строка 1 | Строка 2 | Строка 3")));
        addRenderableWidget(loreField);
        String[][] format = {{UiText.t("Красн."),"&c"},{UiText.t("Зел."),"&a"},{UiText.t("Голуб."),"&b"},{UiText.t("Золото"),"&6"},{UiText.t("Фиол."),"&d"},{UiText.t("Жирн."),"&l"},{UiText.t("Курсив"),"&o"},{UiText.t("Подч."),"&n"},{UiText.t("Зачерк."),"&m"},{UiText.t("Магия"),"&k"},{UiText.t("Сброс"),"&r"}};
        int fg=3, fw=(w-44-fg*5)/6;
        for(int i=0;i<format.length;i++){final String code=format[i][1];int col=i%6,row=i/6;addRenderableWidget(Button.builder(Component.literal(format[i][0]),b->loreField.insertText(code)).bounds(x+22+col*(fw+fg),top+117+row*20,fw,18).build());}
        int bw = (w - 52) / 2;
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить описание")), b -> apply())
                .bounds(x + 22, top + 160, bw, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Очистить описание")), b -> clear())
                .bounds(x + 30 + bw, top + 160, bw, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back())
                .bounds(x + 22, top + 188, w - 44, 20).build());
    }

    private ItemStack held() {
        return EditorContext.stack();
    }

    private void apply() {
        ItemStack stack = held();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        List<Component> lines = new ArrayList<>();
        for (String line : loreField.getValue().split("\\|", -1)) {
            if (!line.isBlank()) lines.add(StyledText.parse(line));
        }
        if (lines.isEmpty()) stack.remove(DataComponents.LORE);
        else stack.set(DataComponents.LORE, new ItemLore(lines));
        status = UiText.t("Строк в описании: ") + lines.size() + ". " + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void clear() {
        loreField.setValue("");
        ItemStack stack = held();
        if (!stack.isEmpty()) {
            stack.remove(DataComponents.LORE);
            boolean synced = ItemSync.syncIfCreative(stack);
            status = synced ? UiText.t("Описание очищено и отправлено на сервер") : UiText.t("Описание очищено локально; для сохранения нужен творческий режим");
        } else status = UiText.t("Сначала выбери предмет");
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EditorContext.renderBackdrop(this, graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1.0f, elapsed / 190_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0f);
        int w = Math.min(430, width - 24);
        int x = (width - w) / 2;
        int y = (height - 290) / 2 + Math.round((1.0f - eased) * 12.0f);
        int bw = (w - 52) / 2;
        String[][] format = {{UiText.t("Красн."),"&c"},{UiText.t("Зел."),"&a"},{UiText.t("Голуб."),"&b"},{UiText.t("Золото"),"&6"},{UiText.t("Фиол."),"&d"},{UiText.t("Жирн."),"&l"},{UiText.t("Курсив"),"&o"},{UiText.t("Подч."),"&n"},{UiText.t("Зачерк."),"&m"},{UiText.t("Магия"),"&k"},{UiText.t("Сброс"),"&r"}};
        int fg=3, fw=(w-44-fg*5)/6;
        for(int i=0;i<format.length;i++){int col=i%6,row=i/6;ButtonSkin.draw(graphics,font,x+22+col*(fw+fg),y+117+row*20,fw,18,format[i][0],mouseX,mouseY);}
        ButtonSkin.draw(graphics, font, x + 22, y + 160, bw, 22, UiText.t("Применить описание"), mouseX, mouseY);
        ButtonSkin.draw(graphics, font, x + 30 + bw, y + 160, bw, 22, UiText.t("Очистить описание"), mouseX, mouseY);
        ButtonSkin.draw(graphics, font, x + 22, y + 188, w - 44, 20, UiText.t("Назад"), mouseX, mouseY);
        graphics.text(font, "ALL-KOTOFF-WANT", x + 28, y + 17, TEXT, false);
        graphics.text(font, UiText.t("РЕДАКТОР ОПИСАНИЯ"), x + 28, y + 30, MUTED, false);
        graphics.text(font, UiText.t("ТЕКСТ ОПИСАНИЯ"), x + 22, y + 49, 0xFFB49AFF, false);
        graphics.text(font, status, x + 22, y + 264, TEXT, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
