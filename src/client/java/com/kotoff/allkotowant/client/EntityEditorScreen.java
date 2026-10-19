package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Summon builder with health, equipment and multiple configurable potion effects. */
public final class EntityEditorScreen extends Screen {
    private record MobOption(String id, String name) {}
    private static final MobOption[] MOBS = {
        new MobOption("minecraft:zombie", "Зомби"), new MobOption("minecraft:skeleton", "Скелет"),
        new MobOption("minecraft:creeper", "Крипер"), new MobOption("minecraft:spider", "Паук"),
        new MobOption("minecraft:witch", "Ведьма"), new MobOption("minecraft:blaze", "Ифрит"),
        new MobOption("minecraft:enderman", "Эндермен"), new MobOption("minecraft:pig", "Свинья"),
        new MobOption("minecraft:cow", "Корова"), new MobOption("minecraft:iron_golem", "Железный голем"),
        new MobOption("minecraft:warden", "Хранитель"), new MobOption("minecraft:ender_dragon", "Дракон Края")
    };
    private final Screen parent;
    private int mobIndex;
    private Button mobButton;
    private EditBox health, mainHand, head, chest, legs, feet, customName;
    private String status = "Нужны права на /summon (читы или оператор)";
    private long openedAt;

    public EntityEditorScreen(Screen parent) { super(Component.literal("All-Kotoff-Want: Создать моба")); this.parent = parent; }

    @Override protected void init() {
        try { mobIndex = Math.max(0, Math.min(MOBS.length - 1, Integer.parseInt(EditorContext.draft("entity.mob", "0")))); } catch (NumberFormatException ignored) { mobIndex = 0; }
        openedAt = System.nanoTime(); ButtonSkin.beginAnimation();
        int w = Math.min(500, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 390) / 2);
        addRenderableOnly(new PanelWidget(x, y, w, 390));
        mobButton = Button.builder(mobLabel(), b -> {
            if (minecraft != null) minecraft.setScreen(new CompactPickerScreen(this, "Выбрать моба", java.util.Arrays.stream(MOBS).map(m -> m.name()).toList(), mobIndex, i -> {
                mobIndex = i; EditorContext.setDraft("entity.mob", Integer.toString(i)); if (mobButton != null) mobButton.setMessage(mobLabel());
            }));
        }).bounds(x + 22, y + 48, w - 44, 22).build(); addRenderableWidget(mobButton);
        health = field(x + 22, y + 94, 92, "Здоровье", "entity.health", "40");
        customName = field(x + 126, y + 94, w - 148, "Имя (необязательно)", "entity.name", "");
        int half = (w - 52) / 2, gap = 8;
        addRenderableWidget(Button.builder(Component.literal("Оружие: " + shortId(EditorContext.draft("entity.hand", "minecraft:diamond_sword"))), b -> openItemPicker(CreativeItemPickerScreen.Kind.WEAPON, "entity.hand", "minecraft:diamond_sword", b))
                .bounds(x + 22, y + 142, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Шлем: " + shortId(EditorContext.draft("entity.head", ""))), b -> openItemPicker(CreativeItemPickerScreen.Kind.ARMOR, "entity.head", "", b))
                .bounds(x + 30 + half, y + 142, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Нагрудник: " + shortId(EditorContext.draft("entity.chest", ""))), b -> openItemPicker(CreativeItemPickerScreen.Kind.ARMOR, "entity.chest", "", b))
                .bounds(x + 22, y + 190, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Поножи: " + shortId(EditorContext.draft("entity.legs", ""))), b -> openItemPicker(CreativeItemPickerScreen.Kind.ARMOR, "entity.legs", "", b))
                .bounds(x + 30 + half, y + 190, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Ботинки: " + shortId(EditorContext.draft("entity.feet", ""))), b -> openItemPicker(CreativeItemPickerScreen.Kind.ARMOR, "entity.feet", "", b))
                .bounds(x + 22, y + 238, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Настроить эффекты…"), b -> { if (minecraft != null) minecraft.setScreen(new MobEffectsScreen(this)); })
                .bounds(x + 30 + half, y + 238, half, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Создать моба"), b -> summon()).bounds(x + 22, y + 278, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> back()).bounds(x + 22, y + 309, (w - 52) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Сбросить вкладку"), b -> { resetTab(); if (minecraft != null) minecraft.setScreen(new EntityEditorScreen(parent)); })
                .bounds(x + 30 + (w - 52) / 2, y + 309, (w - 52) / 2, 20).build());
            ButtonSkin.hideVanillaButtons(this);
    }

    private EditBox field(int x, int y, int w, String hint, String key, String fallback) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(hint));
        box.setMaxLength(100); box.setValue(EditorContext.draft(key, fallback));
        box.setResponder(v -> EditorContext.setDraft(key, v)); box.setHint(Component.literal(hint)); addRenderableWidget(box); return box;
    }
    private Component mobLabel() { return Component.literal("Моб: " + MOBS[mobIndex].name() + "  ›"); }
    private void openItemPicker(CreativeItemPickerScreen.Kind kind, String key, String fallback, Button button) {
        if (minecraft == null) return;
        minecraft.setScreen(new CreativeItemPickerScreen(this, kind, id -> {
            EditorContext.setDraft(key, id);
            if (button != null) button.setMessage(Component.literal(labelFor(key) + shortId(id)));
        }));
    }
    private String labelFor(String key) { return switch (key) { case "entity.hand" -> "Оружие: "; case "entity.head" -> "Шлем: "; case "entity.chest" -> "Нагрудник: "; case "entity.legs" -> "Поножи: "; default -> "Ботинки: "; }; }
    private static String shortId(String id) { if (id == null || id.isBlank()) return "пусто"; String[] p = id.split(":", 2); String value = p.length == 2 ? p[1] : id; return value.length() > 18 ? value.substring(0, 17) + "…" : value; }
    private void resetTab() { EditorContext.resetDraftPrefix("entity."); EditorContext.resetDraftPrefix("mobfx."); status = "Настройки моба сброшены к исходным"; }
    private String itemSnbt(String id) {
        id = id == null ? "" : id.trim();
        if (id.isEmpty()) return "{}";
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return null;
        return "{id:\"" + id + "\",count:1}";
    }
    private void summon() {
        if (minecraft == null || minecraft.player == null) { status = "Мир ещё не загружен"; return; }
        float hp;
        try { hp = Float.parseFloat(health.getValue().trim()); if (!Float.isFinite(hp) || hp < 1 || hp > 1024) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { status = "Здоровье: число от 1 до 1024"; return; }
        String handS = itemSnbt(EditorContext.draft("entity.hand", "minecraft:diamond_sword")), headS = itemSnbt(EditorContext.draft("entity.head", "")), chestS = itemSnbt(EditorContext.draft("entity.chest", "")), legsS = itemSnbt(EditorContext.draft("entity.legs", "")), feetS = itemSnbt(EditorContext.draft("entity.feet", ""));
        if (handS == null || headS == null || chestS == null || legsS == null || feetS == null) { status = "Предметы указывай как minecraft:diamond_sword"; return; }
        StringBuilder data = new StringBuilder("{Health:").append(hp).append("f,attributes:[{id:\"minecraft:max_health\",base:").append(hp).append("}],equipment:{mainhand:").append(handS).append(",head:").append(headS).append(",chest:").append(chestS).append(",legs:").append(legsS).append(",feet:").append(feetS).append("}");
        String name = customName.getValue().trim();
        if (!name.isEmpty()) data.append(",CustomName:'{\"text\":\"").append(name.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'")).append("\"}'");
        String effects = MobEffectsScreen.commandTag();
        if (effects.startsWith("ERROR:")) { status = effects.substring(7).trim(); return; }
        data.append(effects).append('}');
        minecraft.player.connection.sendCommand("summon " + MOBS[mobIndex].id() + " ~ ~ ~ " + data);
        status = "Команда отправлена. Если моб не появился — проверь права на команды";
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta); super.extractRenderState(g, mx, my, delta);
        int w = Math.min(500, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 390) / 2);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 16, 0xFFF4F0FF, false);
        g.text(font, "СОЗДАНИЕ МОБА · ЗДОРОВЬЕ / ЭКИПИРОВКА / ЭФФЕКТЫ", x + 22, y + 31, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 48, w - 44, 22, "Моб: " + MOBS[mobIndex].name() + "  ›", mx, my);
        g.text(font, "ЗДОРОВЬЕ / ИМЯ", x + 22, y + 83, 0xFFB49AFF, false);
        int half = (w - 52) / 2;
        ButtonSkin.draw(g, font, x + 22, y + 142, half, 22, "Оружие: " + shortId(EditorContext.draft("entity.hand", "minecraft:diamond_sword")), mx, my);
        ButtonSkin.draw(g, font, x + 30 + half, y + 142, half, 22, "Шлем: " + shortId(EditorContext.draft("entity.head", "")), mx, my);
        g.text(font, "БРОНЯ", x + 22, y + 179, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 190, half, 22, "Нагрудник: " + shortId(EditorContext.draft("entity.chest", "")), mx, my);
        ButtonSkin.draw(g, font, x + 30 + half, y + 190, half, 22, "Поножи: " + shortId(EditorContext.draft("entity.legs", "")), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 238, half, 22, "Ботинки: " + shortId(EditorContext.draft("entity.feet", "")), mx, my);
        ButtonSkin.draw(g, font, x + 30 + half, y + 238, half, 22, "Настроить эффекты…", mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 278, w - 44, 22, "Создать моба", mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 309, (w - 52) / 2, 20, "Назад", mx, my);
        ButtonSkin.draw(g, font, x + 30 + (w - 52) / 2, y + 309, (w - 52) / 2, 20, "Сбросить вкладку", mx, my);
        g.text(font, status, x + 22, y + 354, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
