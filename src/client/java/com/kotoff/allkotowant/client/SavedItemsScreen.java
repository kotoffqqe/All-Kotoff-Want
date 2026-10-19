package com.kotoff.allkotowant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Persistent personal item library, stored locally across worlds and servers. */
public final class SavedItemsScreen extends Screen {
    private static final int ROWS = 5;
    private final Screen parent;
    private int page;
    private String status = "Предметы хранятся локально в config и доступны в любых мирах";
    private long openedAt;

    public SavedItemsScreen(Screen parent) {
        super(Component.literal("All-Kotoff-Want: Сохранённые предметы"));
        this.parent = parent;
    }

    @Override protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        List<ItemStack> items = SavedItemStore.all();
        int pages = Math.max(1, (items.size() + ROWS - 1) / ROWS);
        page = Math.max(0, Math.min(page, pages - 1));
        int w = Math.min(500, width - 24), x = (width - w) / 2, y = (height - 330) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 330));
        addRenderableWidget(Button.builder(Component.literal("＋ Сохранить текущий предмет"), b -> saveCurrent())
                .bounds(x + 22, y + 48, w - 44, 22).build());
        int start = page * ROWS;
        for (int row = 0; row < ROWS; row++) {
            int index = start + row;
            if (index >= items.size()) break;
            int ry = y + 80 + row * 39;
            addRenderableWidget(Button.builder(Component.literal("В слот"), b -> putInSlot(index))
                    .bounds(x + w - 142, ry, 70, 22).build());
            addRenderableWidget(Button.builder(Component.literal("Удалить"), b -> { SavedItemStore.remove(index); rebuild(); })
                    .bounds(x + w - 66, ry, 44, 22).build());
        }
        addRenderableWidget(Button.builder(Component.literal("‹"), b -> { page = Math.max(0, page - 1); rebuild(); })
                .bounds(x + 22, y + 292, 30, 20).build());
        addRenderableWidget(Button.builder(Component.literal("›"), b -> { page++; rebuild(); })
                .bounds(x + 58, y + 292, 30, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> back())
                .bounds(x + w - 112, y + 292, 90, 20).build());
    }

    private void rebuild() { if (minecraft != null) minecraft.setScreen(new SavedItemsScreen(parentWithPage())); }
    private Screen parentWithPage() {
        SavedItemsScreen copy = new SavedItemsScreen(parent);
        copy.page = page;
        copy.status = status;
        return copy;
    }
    private void saveCurrent() {
        ItemStack stack = EditorContext.stack();
        if (stack == null || stack.isEmpty()) { status = "Сначала выбери предмет в инвентаре"; return; }
        SavedItemStore.add(stack);
        status = "Сохранено: " + stack.getHoverName().getString();
        if (minecraft != null) { SavedItemsScreen copy = parentWithPage(); minecraft.setScreen(copy); }
    }
    private void putInSlot(int index) {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) return;
        if (!minecraft.player.isCreative()) { status = "Выдача сохранённого предмета доступна в творческом режиме"; return; }
        List<ItemStack> items = SavedItemStore.all();
        if (index < 0 || index >= items.size()) return;
        int targetSlot = EditorContext.slot();
        if (targetSlot < 0) targetSlot = minecraft.player.getInventory().getSelectedSlot() + 36;
        minecraft.gameMode.handleCreativeModeItemAdd(items.get(index).copy(), targetSlot);
        status = "Предмет отправлен в выбранный слот";
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(500, width - 24), x = (width - w) / 2, y = (height - 330) / 2;
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3);
        y += Math.round((1f - eased) * 10f);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 16, 0xFFF4F0FF, false);
        g.text(font, "ЛИЧНАЯ БИБЛИОТЕКА ПРЕДМЕТОВ", x + 22, y + 31, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 48, w - 44, 22, "＋ Сохранить текущий предмет", mx, my);
        List<ItemStack> items = SavedItemStore.all();
        int start = page * ROWS;
        for (int row = 0; row < ROWS; row++) {
            int index = start + row;
            int ry = y + 80 + row * 39;
            g.fill(x + 18, ry - 3, x + w - 18, ry + 30, 0xFF1C1E24);
            g.outline(x + 18, ry - 3, w - 36, 33, 0xFF41434D);
            if (index < items.size()) {
                ItemStack stack = items.get(index);
                g.item(stack, x + 26, ry + 3);
                String label = stack.getHoverName().getString() + " ×" + stack.getCount();
                g.text(font, label, x + 50, ry + 9, 0xFFE4E5EB, false);
                ButtonSkin.draw(g, font, x + w - 142, ry, 70, 22, "В слот", mx, my);
                ButtonSkin.draw(g, font, x + w - 66, ry, 44, 22, "×", mx, my);
            } else {
                g.text(font, "Пустая ячейка библиотеки", x + 28, ry + 9, 0xFF777985, false);
            }
        }
        int pages = Math.max(1, (items.size() + ROWS - 1) / ROWS);
        ButtonSkin.draw(g, font, x + 22, y + 292, 30, 20, "‹", mx, my);
        ButtonSkin.draw(g, font, x + 58, y + 292, 30, 20, "›", mx, my);
        ButtonSkin.draw(g, font, x + w - 112, y + 292, 90, 20, "Назад", mx, my);
        g.text(font, "Страница " + (page + 1) + "/" + pages + " · " + items.size() + " предметов", x + 102, y + 298, 0xFFB4A9C9, false);
        g.text(font, status, x + 22, y + 318, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
