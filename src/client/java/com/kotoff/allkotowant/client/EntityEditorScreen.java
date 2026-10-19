package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Builds server-side /data commands for the nearest matching entity. Requires permission to use commands. */
public final class EntityEditorScreen extends Screen {
    private final Screen parent;
    private record MobOption(String id, String name) {}
    private static final MobOption[] MOB_OPTIONS = {
        new MobOption("minecraft:zombie", "Зомби"), new MobOption("minecraft:skeleton", "Скелет"),
        new MobOption("minecraft:creeper", "Крипер"), new MobOption("minecraft:spider", "Паук"),
        new MobOption("minecraft:witch", "Ведьма"), new MobOption("minecraft:blaze", "Ифрит"),
        new MobOption("minecraft:enderman", "Эндермен"), new MobOption("minecraft:pig", "Свинья"),
        new MobOption("minecraft:cow", "Корова"), new MobOption("minecraft:iron_golem", "Железный голем"),
        new MobOption("minecraft:warden", "Хранитель"), new MobOption("minecraft:ender_dragon", "Дракон Края")
    };
    private int mobIndex;
    private Button mobButton;
    private EditBox customName;
    private boolean noAi;
    private boolean silent;
    private boolean glowing;
    private boolean invulnerable;
    private boolean noGravity;
    private String status = UiText.t("На сервере нужны права на команды");
    private long openedAt;

    public EntityEditorScreen(Screen parent) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Настройки моба")));
        this.parent = parent;
    }

    @Override protected void init() {
        try { mobIndex = Math.max(0, Math.min(MOB_OPTIONS.length - 1, Integer.parseInt(EditorContext.draft("entity.mob", "0")))); } catch (NumberFormatException ignored) { mobIndex = 0; }
        noAi = Boolean.parseBoolean(EditorContext.draft("entity.noAi", "false"));
        silent = Boolean.parseBoolean(EditorContext.draft("entity.silent", "false"));
        glowing = Boolean.parseBoolean(EditorContext.draft("entity.glowing", "false"));
        invulnerable = Boolean.parseBoolean(EditorContext.draft("entity.invulnerable", "false"));
        noGravity = Boolean.parseBoolean(EditorContext.draft("entity.noGravity", "false"));
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = (height - 310) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 310));
        mobButton = Button.builder(mobLabel(), b -> { if (minecraft != null) minecraft.setScreen(new CompactPickerScreen(this, UiText.t("Выбрать моба"), java.util.Arrays.stream(MOB_OPTIONS).map(m->UiText.t(m.name())).toList(), mobIndex, i->{mobIndex=i;EditorContext.draft("entity.mob",Integer.toString(i));if(mobButton!=null)mobButton.setMessage(mobLabel());})); })
                .bounds(x + 22, y + 55, w - 44, 20).build(); addRenderableWidget(mobButton);
        customName = new EditBox(font, x + 22, y + 99, w - 44, 20, Component.literal(UiText.t("Имя моба")));
        customName.setMaxLength(64); customName.setValue(EditorContext.draft("entity.name", "")); customName.setResponder(v -> EditorContext.draft("entity.name", v)); customName.setHint(Component.literal(UiText.t("Например: Страж пустоты (необязательно)"))); addRenderableWidget(customName);
        int bw = (w - 52) / 2, gap = 8, by = y + 139;
        addRenderableWidget(toggle(UiText.t("Без ИИ: ВЫКЛ"), x + 22, by, bw, 22, 0));
        addRenderableWidget(toggle(UiText.t("Без звука: ВЫКЛ"), x + 22 + bw + gap, by, bw, 22, 1));
        addRenderableWidget(toggle(UiText.t("Свечение: ВЫКЛ"), x + 22, by + 28, bw, 22, 2));
        addRenderableWidget(toggle(UiText.t("Неуязвимость: ВЫКЛ"), x + 22 + bw + gap, by + 28, bw, 22, 3));
        addRenderableWidget(toggle(UiText.t("Без гравитации: ВЫКЛ"), x + 22, by + 56, w - 44, 22, 4));
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить к ближайшему мобу")), b -> apply())
                .bounds(x + 22, by + 88, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Посадить моба на другого")), b -> {
            if (minecraft != null) minecraft.setScreen(new PassengerEditorScreen(this));
        }).bounds(x + 22, by + 116, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back())
                .bounds(x + 22 + bw + gap, by + 116, bw, 20).build());
    }

    private Button toggle(String label, int x, int y, int w, int h, int which) {
        return Button.builder(Component.literal(label), b -> {
            switch (which) {
                case 0 -> { noAi = !noAi; EditorContext.draft("entity.noAi", Boolean.toString(noAi)); }
                case 1 -> { silent = !silent; EditorContext.draft("entity.silent", Boolean.toString(silent)); }
                case 2 -> { glowing = !glowing; EditorContext.draft("entity.glowing", Boolean.toString(glowing)); }
                case 3 -> { invulnerable = !invulnerable; EditorContext.draft("entity.invulnerable", Boolean.toString(invulnerable)); }
                case 4 -> { noGravity = !noGravity; EditorContext.draft("entity.noGravity", Boolean.toString(noGravity)); }
            }
            b.setMessage(Component.literal(switch (which) {
                case 0 -> UiText.t("Без ИИ: ") + onOff(noAi); case 1 -> UiText.t("Без звука: ") + onOff(silent);
                case 2 -> UiText.t("Свечение: ") + onOff(glowing); case 3 -> UiText.t("Неуязвимость: ") + onOff(invulnerable);
                default -> UiText.t("Без гравитации: ") + onOff(noGravity);
            }));
        }).bounds(x, y, w, h).build();
    }
    private Component mobLabel() { return Component.literal(UiText.t("Тип моба: ") + UiText.t(MOB_OPTIONS[mobIndex].name()) + "  ›"); }
    private String onOff(boolean value) { return value ? UiText.t("ВКЛ") : UiText.t("ВЫКЛ"); }

    private void apply() {
        String id = MOB_OPTIONS[mobIndex].id();
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) { status = UiText.t("Выбери корректного моба"); return; }
        StringBuilder nbt = new StringBuilder("{");
        if (!customName.getValue().trim().isEmpty()) {
            String safe = customName.getValue().trim().replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'");
            nbt.append("CustomName:'{\"text\":\"").append(safe).append("\"}',");
        }
        nbt.append("NoAI:").append(noAi ? "1b" : "0b").append(",Silent:").append(silent ? "1b" : "0b")
            .append(",Glowing:").append(glowing ? "1b" : "0b").append(",Invulnerable:").append(invulnerable ? "1b" : "0b")
            .append(",NoGravity:").append(noGravity ? "1b" : "0b").append('}');
        String command = "data merge entity @e[type=" + id + ",sort=nearest,limit=1] " + nbt;
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand(command);
            status = UiText.t("Команда отправлена. Изменится ближайший моб выбранного типа; нужны права");
        }
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = (height - 310) / 2;
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3);
        y += Math.round((1f - eased) * 10f);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 16, 0xFFF4F0FF, false);
        g.text(font, UiText.t("НАСТРОЙКИ МОБА  /  05"), x + 28, y + 30, 0xFFB4A9C9, false);
        g.text(font, UiText.t("НАЖМИ, ЧТОБЫ ОТКРЫТЬ СПИСОК"), x + 22, y + 44, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 55, w - 44, 20, UiText.t("Тип моба: ") + UiText.t(MOB_OPTIONS[mobIndex].name()) + "  ›", mx, my);
        g.text(font, UiText.t("ИМЯ МОБА"), x + 22, y + 88, 0xFFB49AFF, false);
        int bw = (w - 52) / 2, by = (height - 310) / 2 + 139;
        ButtonSkin.draw(g, font, x + 22, by, bw, 22, UiText.t("Без ИИ: ") + onOff(noAi), mx, my);
        ButtonSkin.draw(g, font, x + 22 + bw + 8, by, bw, 22, UiText.t("Без звука: ") + onOff(silent), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 28, bw, 22, UiText.t("Свечение: ") + onOff(glowing), mx, my);
        ButtonSkin.draw(g, font, x + 22 + bw + 8, by + 28, bw, 22, UiText.t("Неуязвимость: ") + onOff(invulnerable), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 56, w - 44, 22, UiText.t("Без гравитации: ") + onOff(noGravity), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 88, w - 44, 22, UiText.t("Применить к ближайшему мобу"), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 116, bw, 20, UiText.t("Посадить моба на другого"), mx, my);
        ButtonSkin.draw(g, font, x + 22 + bw + 8, by + 116, bw, 20, UiText.t("Назад"), mx, my);
        g.text(font, status, x + 22, y + 290, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
