package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Straightforward summon builder: health, equipment, head item and one potion effect. */
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
    private EditBox health, mainHand, head, chest, legs, feet, effect, effectLevel, customName;
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
        mainHand = field(x + 22, y + 142, half, "Оружие: minecraft:diamond_sword", "entity.hand", "minecraft:diamond_sword");
        head = field(x + 30 + half, y + 142, half, "Голова: minecraft:diamond_helmet", "entity.head", "");
        chest = field(x + 22, y + 190, half, "Нагрудник: minecraft:diamond_chestplate", "entity.chest", "");
        legs = field(x + 30 + half, y + 190, half, "Поножи: minecraft:diamond_leggings", "entity.legs", "");
        feet = field(x + 22, y + 238, half, "Ботинки: minecraft:diamond_boots", "entity.feet", "");
        effect = field(x + 30 + half, y + 238, half, "Эффект: minecraft:speed", "entity.effect", "minecraft:speed");
        effectLevel = field(x + 22, y + 286, 100, "Уровень эффекта", "entity.effectLevel", "1");
        addRenderableWidget(Button.builder(Component.literal("Создать моба"), b -> summon()).bounds(x + 130, y + 286, w - 152, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> back()).bounds(x + 22, y + 324, w - 44, 20).build());
            ButtonSkin.hideVanillaButtons(this);
    }

    private EditBox field(int x, int y, int w, String hint, String key, String fallback) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(hint));
        box.setMaxLength(100); box.setValue(EditorContext.draft(key, fallback));
        box.setResponder(v -> EditorContext.setDraft(key, v)); box.setHint(Component.literal(hint)); addRenderableWidget(box); return box;
    }
    private Component mobLabel() { return Component.literal("Моб: " + MOBS[mobIndex].name() + "  ›"); }
    private String itemSnbt(EditBox box) {
        String id = box.getValue().trim();
        if (id.isEmpty()) return "{}";
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return null;
        return "{id:\"" + id + "\",count:1}";
    }
    private void summon() {
        if (minecraft == null || minecraft.player == null) { status = "Мир ещё не загружен"; return; }
        float hp;
        int level;
        try { hp = Float.parseFloat(health.getValue().trim()); if (!Float.isFinite(hp) || hp < 1 || hp > 1024) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { status = "Здоровье: число от 1 до 1024"; return; }
        try { level = Integer.parseInt(effectLevel.getValue().trim()); if (level < 1 || level > 255) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { status = "Уровень эффекта: число от 1 до 255"; return; }
        String handS = itemSnbt(mainHand), headS = itemSnbt(head), chestS = itemSnbt(chest), legsS = itemSnbt(legs), feetS = itemSnbt(feet);
        if (handS == null || headS == null || chestS == null || legsS == null || feetS == null) { status = "Предметы указывай как minecraft:diamond_sword"; return; }
        String effectId = effect.getValue().trim();
        if (!effectId.isEmpty() && !effectId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) { status = "Эффект указывай как minecraft:speed"; return; }
        StringBuilder data = new StringBuilder("{Health:").append(hp).append("f,attributes:[{id:\"minecraft:max_health\",base:").append(hp).append("}],equipment:{mainhand:").append(handS).append(",head:").append(headS).append(",chest:").append(chestS).append(",legs:").append(legsS).append(",feet:").append(feetS).append("}");
        String name = customName.getValue().trim();
        if (!name.isEmpty()) data.append(",CustomName:'{\"text\":\"").append(name.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'")).append("\"}'");
        if (!effectId.isEmpty()) data.append(",active_effects:[{id:\"").append(effectId).append("\",amplifier:").append(level - 1).append(",duration:999999,show_particles:1b}]");
        data.append('}');
        minecraft.player.connection.sendCommand("summon " + MOBS[mobIndex].id() + " ~ ~ ~ " + data);
        status = "Команда отправлена. Если моб не появился — проверь права на команды";
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta); super.extractRenderState(g, mx, my, delta);
        int w = Math.min(500, width - 24), x = (width - w) / 2, y = Math.max(8, (height - 390) / 2);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt); float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3); y += Math.round((1f - eased) * 10f);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 16, 0xFFF4F0FF, false);
        g.text(font, "СОЗДАНИЕ МОБА · ЗДОРОВЬЕ / ЭКИПИРОВКА / ЭФФЕКТЫ", x + 22, y + 31, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 48, w - 44, 22, "Моб: " + MOBS[mobIndex].name() + "  ›", mx, my);
        g.text(font, "ЗДОРОВЬЕ / ИМЯ", x + 22, y + 83, 0xFFB49AFF, false);
        g.text(font, "ОРУЖИЕ / ПРЕДМЕТ НА ГОЛОВУ", x + 22, y + 131, 0xFFB49AFF, false);
        g.text(font, "БРОНЯ", x + 22, y + 179, 0xFFB49AFF, false);
        g.text(font, "БОТИНКИ / ЭФФЕКТ", x + 22, y + 227, 0xFFB49AFF, false);
        g.text(font, "Уровень эффекта 1–255 (1 = I)", x + 22, y + 277, 0xFFB4A9C9, false);
        ButtonSkin.draw(g, font, x + 130, y + 286, w - 152, 22, "Создать моба", mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 324, w - 44, 20, "Назад", mx, my);
        g.text(font, status, x + 22, y + 357, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
