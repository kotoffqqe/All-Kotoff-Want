package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** Multi-effect editor for summoned mobs. Levels and durations are stored in shared editor drafts. */
public final class MobEffectsScreen extends Screen {
    public record Effect(String id, String name) {}
    public static final List<Effect> EFFECTS = List.of(
        new Effect("minecraft:speed", "Скорость"),
        new Effect("minecraft:invisibility", "Невидимость"),
        new Effect("minecraft:strength", "Сила"),
        new Effect("minecraft:jump_boost", "Прыгучесть"),
        new Effect("minecraft:resistance", "Сопротивление"),
        new Effect("minecraft:regeneration", "Регенерация"),
        new Effect("minecraft:fire_resistance", "Огнестойкость"),
        new Effect("minecraft:slowness", "Замедление")
    );
    private final Screen parent;
    private final List<Button> toggles = new ArrayList<>();
    private String status = "Отметь эффекты, укажи уровень и длительность в секундах";
    public MobEffectsScreen(Screen parent) { super(Component.literal("All-Kotoff-Want: Эффекты моба")); this.parent = parent; }

    @Override protected void init() {
        toggles.clear();
        int w = Math.min(520, width - 24), h = 390, x = (width - w) / 2, y = (height - h) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, h));
        for (int i = 0; i < EFFECTS.size(); i++) {
            Effect effect = EFFECTS.get(i); int ry = y + 52 + i * 31;
            Button toggle = Button.builder(Component.literal(toggleLabel(effect)), b -> {
                boolean next = !enabled(effect); EditorContext.setDraft(key(effect, "enabled"), Boolean.toString(next));
                b.setMessage(Component.literal(toggleLabel(effect)));
            }).bounds(x + 18, ry, 178, 22).build();
            toggles.add(toggle); addRenderableWidget(toggle);
            addField(x + 207, ry, 54, "1", key(effect, "level"), "1", 3);
            addField(x + 270, ry, 78, "сек", key(effect, "seconds"), "60", 6);
        }
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> { if (minecraft != null) minecraft.setScreen(parent); })
                .bounds(x + w - 112, y + 322, 90, 20).build());
        ButtonSkin.hideVanillaButtons(this);
    }
    private void addField(int x, int y, int w, String hint, String key, String fallback, int max) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(hint));
        box.setMaxLength(max); box.setValue(EditorContext.draft(key, fallback)); box.setHint(Component.literal(hint));
        box.setResponder(v -> EditorContext.setDraft(key, v)); addRenderableWidget(box);
    }
    private static String key(Effect effect, String suffix) { return "mobfx." + effect.id().replace(':', '.') + "." + suffix; }
    private static boolean enabled(Effect effect) { return Boolean.parseBoolean(EditorContext.draft(key(effect, "enabled"), "false")); }
    private static String toggleLabel(Effect effect) { return (enabled(effect) ? "☑ " : "☐ ") + effect.name(); }
    public static String commandTag() {
        List<String> active = new ArrayList<>();
        for (Effect effect : EFFECTS) {
            if (!enabled(effect)) continue;
            int level; int seconds;
            try { level = Integer.parseInt(EditorContext.draft(key(effect, "level"), "1")); } catch (NumberFormatException e) { return "ERROR: " + effect.name() + " — уровень должен быть 1–255"; }
            try { seconds = Integer.parseInt(EditorContext.draft(key(effect, "seconds"), "60")); } catch (NumberFormatException e) { return "ERROR: " + effect.name() + " — время должно быть 1–999999"; }
            if (level < 1 || level > 255) return "ERROR: " + effect.name() + " — уровень должен быть 1–255";
            if (seconds < 1 || seconds > 999999) return "ERROR: " + effect.name() + " — время должно быть 1–999999 секунд";
            active.add("{id:\"" + effect.id() + "\",amplifier:" + (level - 1) + ",duration:" + (seconds * 20L) + ",show_particles:1b}");
        }
        return active.isEmpty() ? "" : ",active_effects:[" + String.join(",", active) + "]";
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta); super.extractRenderState(g, mx, my, delta);
        int w = Math.min(520, width - 24), h = 390, x = (width - w) / 2, y = (height - h) / 2;
        g.text(font, "ALL-KOTOFF-WANT", x + 22, y + 15, 0xFFF4F0FF, false);
        g.text(font, "ЭФФЕКТЫ МОБА · УРОВЕНЬ / ВРЕМЯ В СЕКУНДАХ", x + 22, y + 31, 0xFFB49AFF, false);
        g.text(font, "Эффект", x + 24, y + 43, 0xFFB4A9C9, false);
        g.text(font, "Ур.", x + 216, y + 43, 0xFFB4A9C9, false);
        g.text(font, "Секунд", x + 277, y + 43, 0xFFB4A9C9, false);
        for (int i = 0; i < EFFECTS.size(); i++) {
            Effect effect = EFFECTS.get(i); int ry = y + 52 + i * 31;
            ButtonSkin.draw(g, font, x + 18, ry, 178, 22, toggleLabel(effect), mx, my);
        }
        ButtonSkin.draw(g, font, x + w - 112, y + 322, 90, 20, "Назад", mx, my);
        g.text(font, "Уровень 1–255 · длительность 1–999999 секунд", x + 22, y + 354, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
