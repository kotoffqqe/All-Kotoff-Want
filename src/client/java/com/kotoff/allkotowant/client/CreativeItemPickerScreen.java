package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Searchable, inventory-grid item picker inspired by creative inventory selectors. */
final class CreativeItemPickerScreen extends Screen {
    enum Kind { ARMOR, WEAPON }

    private final Screen parent;
    private final Kind kind;
    private final Consumer<String> onSelect;
    private final List<Item> all = new ArrayList<>();
    private final List<Item> filtered = new ArrayList<>();
    private EditBox search;
    private int first;
    private static final int COLS = 9;
    private static final int ROWS = 5;
    private static final int CELL = 34;
    private static final int GAP = 4;
    private static final int PAGE_SIZE = COLS * ROWS;
    private static final int GRID_W = COLS * CELL + (COLS - 1) * GAP;

    CreativeItemPickerScreen(Screen parent, Kind kind, Consumer<String> onSelect) {
        super(Component.literal(kind == Kind.ARMOR ? "Выбрать броню" : "Выбрать оружие"));
        this.parent = parent;
        this.kind = kind;
        this.onSelect = onSelect;
        for (Item item : BuiltInRegistries.ITEM) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (matchesKind(path)) all.add(item);
        }
        all.sort((a, b) -> a.getDefaultInstance().getHoverName().getString()
                .compareToIgnoreCase(b.getDefaultInstance().getHoverName().getString()));
        filtered.addAll(all);
    }

    private boolean matchesKind(String path) {
        if (kind == Kind.ARMOR) {
            return path.endsWith("_helmet") || path.endsWith("_chestplate")
                    || path.endsWith("_leggings") || path.endsWith("_boots")
                    || path.equals("elytra") || path.equals("turtle_helmet")
                    || path.endsWith("_horse_armor") || path.equals("wolf_armor");
        }
        return path.endsWith("_sword") || path.endsWith("_axe")
                || path.equals("bow") || path.equals("crossbow")
                || path.equals("trident") || path.equals("mace")
                || path.contains("spear");
    }

    @Override protected void init() {
        int x = left(), y = top(), w = panelW();
        search = new EditBox(font, x + 14, y + 43, w - 28, 20, Component.literal("Поиск предмета"));
        search.setMaxLength(80);
        search.setHint(Component.literal("Поиск по названию или ID…"));
        search.setResponder(this::filter);
        addRenderableWidget(search);
    }

    private void filter(String query) {
        filtered.clear();
        first = 0;
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Item item : all) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
            String name = item.getDefaultInstance().getHoverName().getString().toLowerCase(Locale.ROOT);
            if (q.isEmpty() || id.contains(q) || name.contains(q)) filtered.add(item);
        }
    }

    private int panelW() { return Math.min(440, width - 12); }
    private int panelH() { return 54 + 20 + 12 + ROWS * CELL + (ROWS - 1) * GAP + 32; }
    private int left() { return (width - panelW()) / 2; }
    private int top() { return Math.max(4, (height - panelH()) / 2); }
    private int gridX() { return left() + (panelW() - GRID_W) / 2; }
    private int gridY() { return top() + 72; }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        parent.extractRenderState(g, mx, my, delta);
        g.fill(0, 0, width, height, 0xB807090F);
        int x = left(), y = top(), w = panelW(), h = panelH();
        g.fill(x, y, x + w, y + h, 0xF016171D);
        g.outline(x, y, w, h, 0xFF454752);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, 0xFF9B8AFB);
        g.text(font, kind == Kind.ARMOR ? "ВЫБОР БРОНИ" : "ВЫБОР ОРУЖИЯ", x + 14, y + 13, 0xFFF5F5FA, false);
        g.text(font, "Предметы · нажми на иконку · поиск по названию или ID", x + 14, y + 29, 0xFF9A9DAA, false);
        super.extractRenderState(g, mx, my, delta);

        int gx = gridX(), gy = gridY();
        int visible = Math.min(PAGE_SIZE, filtered.size() - first);
        ItemStack hoveredStack = ItemStack.EMPTY;
        for (int slot = 0; slot < PAGE_SIZE; slot++) {
            int col = slot % COLS, row = slot / COLS;
            int sx = gx + col * (CELL + GAP), sy = gy + row * (CELL + GAP);
            int index = first + slot;
            boolean hovered = mx >= sx && mx < sx + CELL && my >= sy && my < sy + CELL;
            g.fill(sx, sy, sx + CELL, sy + CELL, hovered ? 0xFF3A354A : 0xFF22242C);
            g.outline(sx, sy, CELL, CELL, hovered ? 0xFFB49AFF : 0xFF41434D);
            if (slot < visible) {
                ItemStack stack = filtered.get(index).getDefaultInstance();
                g.item(stack, sx + 9, sy + 9);
                if (hovered) hoveredStack = stack;
            } else {
                g.text(font, "·", sx + 14, sy + 11, 0xFF555763, false);
            }
        }
        String range = filtered.isEmpty() ? "0" : (first + 1) + "–" + Math.min(first + PAGE_SIZE, filtered.size());
        g.text(font, range + " / " + filtered.size() + " предметов", x + 14, y + h - 19, 0xFFB4A9C9, false);
        String pageText = "Колесо — прокрутка · Esc — назад";
        g.text(font, pageText, x + w - 14 - font.width(pageText), y + h - 19, 0xFF9A9DAA, false);
        if (!hoveredStack.isEmpty()) {
            String tip = hoveredStack.getHoverName().getString() + " · " + BuiltInRegistries.ITEM.getKey(hoveredStack.getItem());
            int tw = Math.min(font.width(tip), width - 24);
            int tx = Math.max(12, Math.min(mx + 10, width - tw - 12));
            int ty = my < height - 28 ? my + 10 : my - 22;
            g.fill(tx - 4, ty - 3, tx + tw + 4, ty + 13, 0xF0101018);
            g.outline(tx - 4, ty - 3, tw + 8, 16, 0xFF6B617D);
            g.text(font, tip, tx, ty, 0xFFF4F0FF, false);
        }
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != 0) return false;
        double mx = event.x(), my = event.y();
        int gx = gridX(), gy = gridY();
        if (mx >= gx && mx < gx + GRID_W && my >= gy && my < gy + ROWS * CELL + (ROWS - 1) * GAP) {
            int col = (int) (mx - gx) / (CELL + GAP);
            int row = (int) (my - gy) / (CELL + GAP);
            int inPage = row * COLS + col;
            int sx = gx + col * (CELL + GAP), sy = gy + row * (CELL + GAP);
            if (mx < sx + CELL && my < sy + CELL) {
                int index = first + inPage;
                if (index >= 0 && index < filtered.size()) {
                    onSelect.accept(BuiltInRegistries.ITEM.getKey(filtered.get(index)).toString());
                    if (minecraft != null) minecraft.setScreen(parent);
                }
                return true;
            }
        }
        if (mx < left() || mx > left() + panelW() || my < top() || my > top() + panelH()) {
            if (minecraft != null) minecraft.setScreen(parent);
        }
        return true;
    }

    @Override public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        int max = Math.max(0, filtered.size() - PAGE_SIZE);
        first = Math.max(0, Math.min(max, first + (vertical < 0 ? COLS : -COLS)));
        return true;
    }

    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
