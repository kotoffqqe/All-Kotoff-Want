package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Builds server-side /data commands for the nearest matching entity. Requires permission to use commands. */
public final class EntityEditorScreen extends Screen {
    private final Screen parent;
    private EditBox entityId;
    private EditBox customName;
    private boolean noAi;
    private boolean silent;
    private boolean glowing;
    private boolean invulnerable;
    private boolean noGravity;
    private String status = "Command permission required on multiplayer servers";
    private long openedAt;

    public EntityEditorScreen(Screen parent) {
        super(Component.literal("All-Kotoff-Want: Entity Lab"));
        this.parent = parent;
    }

    @Override protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = (height - 310) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 310));
        entityId = new EditBox(font, x + 22, y + 55, w - 44, 20, Component.literal("Entity ID"));
        entityId.setMaxLength(128); entityId.setValue("minecraft:zombie"); addRenderableWidget(entityId);
        customName = new EditBox(font, x + 22, y + 99, w - 44, 20, Component.literal("Custom name"));
        customName.setMaxLength(64); customName.setHint(Component.literal("e.g. Void Guardian (optional)")); addRenderableWidget(customName);
        int bw = (w - 52) / 2, gap = 8, by = y + 139;
        addRenderableWidget(toggle("No AI: OFF", x + 22, by, bw, 22, 0));
        addRenderableWidget(toggle("Silent: OFF", x + 22 + bw + gap, by, bw, 22, 1));
        addRenderableWidget(toggle("Glowing: OFF", x + 22, by + 28, bw, 22, 2));
        addRenderableWidget(toggle("Invulnerable: OFF", x + 22 + bw + gap, by + 28, bw, 22, 3));
        addRenderableWidget(toggle("No gravity: OFF", x + 22, by + 56, w - 44, 22, 4));
        addRenderableWidget(Button.builder(Component.literal("APPLY TO NEAREST ENTITY"), b -> apply())
                .bounds(x + 22, by + 88, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("PASSENGER SUMMONER"), b -> {
            if (minecraft != null) minecraft.setScreen(new PassengerEditorScreen(this));
        }).bounds(x + 22, by + 116, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("BACK"), b -> back())
                .bounds(x + 22 + bw + gap, by + 116, bw, 20).build());
    }

    private Button toggle(String label, int x, int y, int w, int h, int which) {
        return Button.builder(Component.literal(label), b -> {
            switch (which) {
                case 0 -> noAi = !noAi; case 1 -> silent = !silent; case 2 -> glowing = !glowing;
                case 3 -> invulnerable = !invulnerable; case 4 -> noGravity = !noGravity;
            }
            b.setMessage(Component.literal(switch (which) {
                case 0 -> "No AI: " + onOff(noAi); case 1 -> "Silent: " + onOff(silent);
                case 2 -> "Glowing: " + onOff(glowing); case 3 -> "Invulnerable: " + onOff(invulnerable);
                default -> "No gravity: " + onOff(noGravity);
            }));
        }).bounds(x, y, w, h).build();
    }
    private String onOff(boolean value) { return value ? "ON" : "OFF"; }

    private void apply() {
        String id = entityId.getValue().trim();
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) { status = "Use a valid namespaced entity ID"; return; }
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
            status = "Command sent. It affects the nearest matching entity if permitted.";
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
        g.text(font, "ENTITY LAB  /  05", x + 28, y + 30, 0xFFB4A9C9, false);
        g.text(font, "ENTITY ID", x + 22, y + 44, 0xFFB49AFF, false);
        g.text(font, "CUSTOM NAME", x + 22, y + 88, 0xFFB49AFF, false);
        int bw = (w - 52) / 2, by = (height - 310) / 2 + 139;
        ButtonSkin.draw(g, font, x + 22, by, bw, 22, "No AI: " + onOff(noAi), mx, my);
        ButtonSkin.draw(g, font, x + 22 + bw + 8, by, bw, 22, "Silent: " + onOff(silent), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 28, bw, 22, "Glowing: " + onOff(glowing), mx, my);
        ButtonSkin.draw(g, font, x + 22 + bw + 8, by + 28, bw, 22, "Invulnerable: " + onOff(invulnerable), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 56, w - 44, 22, "No gravity: " + onOff(noGravity), mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 88, w - 44, 22, "APPLY TO NEAREST ENTITY", mx, my);
        ButtonSkin.draw(g, font, x + 22, by + 116, bw, 20, "PASSENGER SUMMONER", mx, my);
        ButtonSkin.draw(g, font, x + 22 + bw + 8, by + 116, bw, 20, "BACK", mx, my);
        g.text(font, status, x + 22, y + 290, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
