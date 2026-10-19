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
    private int selectedIndex = -1;
    private int targetHotbarSlot = -1;
    private String status = "Предметы хранятся локально и доступны в разных мирах";

    public SavedItemsScreen(Screen parent) { this(parent, 0, -1, -1, "Предметы хранятся локально и доступны в разных мирах"); }
    private SavedItemsScreen(Screen parent, int page, int selectedIndex, int targetHotbarSlot, String status) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Сохранённые предметы")));
        this.parent = parent;
        this.page = Math.max(0, page);
        this.selectedIndex = selectedIndex;
        this.targetHotbarSlot = targetHotbarSlot;
        this.status = status;
    }

    @Override protected void init() {
        ButtonSkin.beginAnimation();
        List<ItemStack> items = SavedItemStore.all();
        int pages = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));
        int w = Math.min(520, width - 20), h = 330, x = (width - w) / 2, y = (height - h) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, h));
        addRenderableWidget(Button.builder(Component.literal(UiText.t("＋ Сохранить")), b -> saveCurrent())
                .bounds(x + 22, y + 48, 104, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Выдать")), b -> { if (selectedIndex >= 0) putInSlot(selectedIndex); else status = "Сначала выбери предмет в библиотеке"; })
                .bounds(x + 132, y + 48, 90, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Удалить")), b -> { if (selectedIndex >= 0) { if (SavedItemStore.remove(selectedIndex)) { selectedIndex = -1; status = "Предмет удалён из библиотеки"; rebuild(); } else status = "Не удалось сохранить удаление в файл конфигурации"; } else status = "Сначала выбери предмет в библиотеке"; })
                .bounds(x + 228, y + 48, 90, 22).build());
        for (int slot = 0; slot < 9; slot++) {
            final int hotbarSlot = slot;
            addRenderableWidget(Button.builder(Component.literal(Integer.toString(slot + 1)), b -> {
                targetHotbarSlot = hotbarSlot;
                status = "Целевой слот хотбара: " + (hotbarSlot + 1);
            }).bounds(x + 22 + slot * 28, y + 75, 24, 20).build());
        }
        int gridX = x + (w - GRID_W) / 2, gridY = y + 101, start = page * PAGE_SIZE;
        for (int row = 0; row < ROWS; row++) for (int col = 0; col < COLS; col++) {
            int local = row * COLS + col, index = start + local;
            int sx = gridX + col * (CELL + GAP), sy = gridY + row * (CELL + GAP);
            if (index < items.size()) {
                addRenderableWidget(Button.builder(Component.empty(), b -> { selectedIndex = index; status = "Выбран: " + SavedItemStore.all().get(index).getHoverName().getString(); }).bounds(sx, sy, CELL, CELL).build());
            } else {
                addRenderableWidget(Button.builder(Component.empty(), b -> {}).bounds(sx, sy, CELL, CELL).build());
            }
        }
        addRenderableWidget(Button.builder(Component.literal(UiText.t("‹")), b -> { if (page > 0) { page--; rebuild(); } })
                .bounds(x + 22, y + 267, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("›")), b -> { if ((page + 1) * PAGE_SIZE < items.size()) { page++; rebuild(); } })
                .bounds(x + 58, y + 267, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x + w - 112, y + 267, 90, 20).build());
        ButtonSkin.hideVanillaButtons(this);
    }

    private void rebuild() {
        if (minecraft != null) minecraft.setScreen(new SavedItemsScreen(parent, page, selectedIndex, targetHotbarSlot, status));
    }
    private void saveCurrent() {
        ItemStack stack = EditorContext.stack();
        if (stack == null || stack.isEmpty()) { status = "Сначала выбери предмет в инвентаре"; return; }
        if (SavedItemStore.add(stack)) {
            selectedIndex = SavedItemStore.size() - 1;
            page = selectedIndex / PAGE_SIZE;
            status = "Сохранено: " + stack.getHoverName().getString();
            rebuild();
        } else {
            status = "Не удалось сохранить предмет в файл конфигурации";
        }
    }
    private void putInSlot(int index) {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) {
            status = "Сначала зайди в мир";
            return;
        }
        if (!minecraft.player.isCreative()) {
            status = "Выдача на сервер доступна в творческом режиме";
            return;
        }
        List<ItemStack> items = SavedItemStore.all();
        if (index < 0 || index >= items.size()) {
            selectedIndex = -1;
            status = "Выбери существующий предмет в библиотеке";
            return;
        }
        int hotbar = targetHotbarSlot >= 0 ? targetHotbarSlot : minecraft.player.getInventory().getSelectedSlot();
        ItemStack copy = items.get(index).copy();
        // Keep the client inventory and vanilla creative-slot packet in sync.
        minecraft.player.getInventory().setItem(hotbar, copy.copy());
        minecraft.gameMode.handleCreativeModeItemAdd(copy.copy(), hotbar + 36);
        minecraft.player.getInventory().setSelectedSlot(hotbar);
        status = "Выдано в хотбар " + (hotbar + 1) + ": " + copy.getHoverName().getString();
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(520, width - 20), h = 330, x = (width - w) / 2, y = (height - h) / 2;
        int gridX = x + (w - GRID_W) / 2, gridY = y + 101;
        g.text(font, UiText.t("ALL-KOTOFF-WANT"), x + 22, y + 16, 0xFFF4F0FF, false);
        g.text(font, UiText.t("ЛИЧНАЯ БИБЛИОТЕКА · КАК ИНВЕНТАРЬ"), x + 22, y + 31, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 48, 104, 22, "＋ Сохранить", mx, my);
        ButtonSkin.draw(g, font, x + 132, y + 48, 90, 22, "Выдать", mx, my);
        ButtonSkin.draw(g, font, x + 228, y + 48, 90, 22, "Удалить", mx, my);
        g.text(font, UiText.t("Слот:"), x + 280, y + 80, 0xFFB4A9C9, false);
        for (int slot = 0; slot < 9; slot++) {
            int bx = x + 22 + slot * 28;
            boolean chosen = targetHotbarSlot == slot;
            g.fill(bx, y + 75, bx + 24, y + 95, chosen ? 0xFF51466F : 0xFF24262D);
            g.outline(bx, y + 75, 24, 20, chosen ? 0xFFB49AFF : 0xFF41434D);
            String label = Integer.toString(slot + 1);
            g.text(font, label, bx + (24 - font.width(label)) / 2, y + 81, 0xFFE4E5EB, false);
        }
        List<ItemStack> items = SavedItemStore.all();
        int start = page * PAGE_SIZE;
        for (int row = 0; row < ROWS; row++) for (int col = 0; col < COLS; col++) {
            int local = row * COLS + col, index = start + local;
            int sx = gridX + col * (CELL + GAP), sy = gridY + row * (CELL + GAP);
            boolean hovered = mx >= sx && mx < sx + CELL && my >= sy && my < sy + CELL;
            boolean selected = index == selectedIndex;
            g.fill(sx, sy, sx + CELL, sy + CELL, (hovered || selected) ? 0xFF343640 : 0xFF1C1E24);
            g.outline(sx, sy, CELL, CELL, selected ? 0xFFB49AFF : (hovered ? 0xFF9B8AFB : 0xFF41434D));
            if (index < items.size()) {
                ItemStack stack = items.get(index);
                g.item(stack, sx + 13, sy + 12);
                if (stack.getCount() > 1) g.text(font, Integer.toString(stack.getCount()), sx + CELL - 17, sy + CELL - 12, 0xFFFFFFFF, true);

                if (hovered) {
                    String name = stack.getHoverName().getString() + " ×" + stack.getCount();
                    g.fill(mx + 8, my + 8, mx + 16 + font.width(name), my + 21, 0xF0101018);
                    g.text(font, name, mx + 12, my + 10, 0xFFF4F0FF, false);
                }
            } else g.text(font, UiText.t("·"), sx + 20, sy + 18, 0xFF555763, false);
        }
        int pages = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        ButtonSkin.draw(g, font, x + 22, y + 267, 32, 20, "‹", mx, my);
        ButtonSkin.draw(g, font, x + 58, y + 267, 32, 20, "›", mx, my);
        ButtonSkin.draw(g, font, x + w - 112, y + 267, 90, 20, "Назад", mx, my);
        g.text(font, UiText.t("Страница") + " " + (page + 1) + "/" + pages + " · " + items.size() + " " + UiText.t("предметов"), x + 102, y + 273, 0xFFB4A9C9, false);
        g.text(font, status, x + 22, y + 303, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
