package com.kotoff.allkotowant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Inventory-style personal item library, persisted locally across worlds and servers. */
public final class SavedItemsScreen extends Screen {
    private static final int COLS = 9, ROWS = 3, PAGE_SIZE = COLS * ROWS;
    private static final int CELL = 44, GAP = 5, GRID_W = COLS * CELL + (COLS - 1) * GAP;
    private final Screen parent;
    private int page;
    private String status = "Предметы хранятся локально и доступны в разных мирах";

    public SavedItemsScreen(Screen parent) { this(parent, 0, "Предметы хранятся локально и доступны в разных мирах"); }
    private SavedItemsScreen(Screen parent, int page, String status) {
        super(Component.literal("All-Kotoff-Want: Сохранённые предметы"));
        this.parent = parent; this.page = Math.max(0, page); this.status = status;
    }

    @Override protected void init() {
        ButtonSkin.beginAnimation();
        List<ItemStack> items = SavedItemStore.all();
        int pages = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));
        int w = Math.min(520, width - 20), h = 330, x = (width - w) / 2, y = (height - h) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, h));
        addRenderableWidget(Button.builder(Component.literal("＋ Сохранить текущий предмет"), b -> saveCurrent())
                .bounds(x + 22, y + 48, w - 44, 22).build());
        int gridX = x + (w - GRID_W) / 2, gridY = y + 80, start = page * PAGE_SIZE;
        for (int row = 0; row < ROWS; row++) for (int col = 0; col < COLS; col++) {
            int local = row * COLS + col, index = start + local;
            int sx = gridX + col * (CELL + GAP), sy = gridY + row * (CELL + GAP);
            if (index < items.size()) {
                addRenderableWidget(Button.builder(Component.empty(), b -> putInSlot(index)).bounds(sx, sy, CELL, CELL).build());
                addRenderableWidget(Button.builder(Component.literal("×"), b -> { SavedItemStore.remove(index); status = "Предмет удалён из библиотеки"; rebuild(); })
                        .bounds(sx + CELL - 13, sy, 13, 13).build());
            } else {
                addRenderableWidget(Button.builder(Component.empty(), b -> {}).bounds(sx, sy, CELL, CELL).build());
            }
        }
        addRenderableWidget(Button.builder(Component.literal("‹"), b -> { if (page > 0) { page--; rebuild(); } })
                .bounds(x + 22, y + 274, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("›"), b -> { if ((page + 1) * PAGE_SIZE < items.size()) { page++; rebuild(); } })
                .bounds(x + 58, y + 274, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> back()).bounds(x + w - 112, y + 274, 90, 20).build());
        ButtonSkin.hideVanillaButtons(this);
    }

    private void rebuild() {
        if (minecraft != null) minecraft.setScreen(new SavedItemsScreen(parent, page, status));
    }
    private void saveCurrent() {
        ItemStack stack = EditorContext.stack();
        if (stack == null || stack.isEmpty()) { status = "Сначала выбери предмет в инвентаре"; return; }
        SavedItemStore.add(stack);
        status = "Сохранено: " + stack.getHoverName().getString();
        rebuild();
    }
    private void putInSlot(int index) {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) return;
        if (!minecraft.player.isCreative()) { status = "Выдача доступна в творческом режиме"; return; }
        List<ItemStack> items = SavedItemStore.all();
        if (index < 0 || index >= items.size()) return;
        int hotbar = minecraft.player.getInventory().getSelectedSlot();
        ItemStack copy = items.get(index).copy();
        // Update the local selected hotbar slot immediately, then synchronize the same inventory slot to the server.
        minecraft.player.getInventory().setItem(hotbar, copy.copy());
        minecraft.gameMode.handleCreativeModeItemAdd(copy, hotbar + 36);
        status = "Выдано в слот " + (hotbar + 1) + ": " + copy.getHoverName().getString();
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(520, width - 20), h = 330, x = (width - w) / 2, y = (height - h) / 2;
        int gridX = x + (w - GRID_W) / 2, gridY = y + 80;
        g.text(font, "ALL-KOTOFF-WANT", x + 22, y + 16, 0xFFF4F0FF, false);
        g.text(font, "ЛИЧНАЯ БИБЛИОТЕКА · КАК ИНВЕНТАРЬ", x + 22, y + 31, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 48, w - 44, 22, "＋ Сохранить текущий предмет", mx, my);
        List<ItemStack> items = SavedItemStore.all();
        int start = page * PAGE_SIZE;
        for (int row = 0; row < ROWS; row++) for (int col = 0; col < COLS; col++) {
            int local = row * COLS + col, index = start + local;
            int sx = gridX + col * (CELL + GAP), sy = gridY + row * (CELL + GAP);
            boolean hovered = mx >= sx && mx < sx + CELL && my >= sy && my < sy + CELL;
            g.fill(sx, sy, sx + CELL, sy + CELL, hovered ? 0xFF343640 : 0xFF1C1E24);
            g.outline(sx, sy, CELL, CELL, hovered ? 0xFF9B8AFB : 0xFF41434D);
            if (index < items.size()) {
                ItemStack stack = items.get(index);
                g.item(stack, sx + 13, sy + 12);
                g.text(font, "×", sx + CELL - 10, sy + 1, 0xFFFFA7B8, false);
                if (hovered) {
                    String name = stack.getHoverName().getString() + " ×" + stack.getCount();
                    g.fill(mx + 8, my + 8, mx + 16 + font.width(name), my + 21, 0xF0101018);
                    g.text(font, name, mx + 12, my + 10, 0xFFF4F0FF, false);
                }
            } else g.text(font, "·", sx + 20, sy + 18, 0xFF555763, false);
        }
        int pages = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        ButtonSkin.draw(g, font, x + 22, y + 274, 32, 20, "‹", mx, my);
        ButtonSkin.draw(g, font, x + 58, y + 274, 32, 20, "›", mx, my);
        ButtonSkin.draw(g, font, x + w - 112, y + 274, 90, 20, "Назад", mx, my);
        g.text(font, "Страница " + (page + 1) + "/" + pages + " · " + items.size() + " предметов", x + 102, y + 280, 0xFFB4A9C9, false);
        g.text(font, status, x + 22, y + 307, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
