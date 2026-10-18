package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Generates a command for the spawner block immediately below the player. */
public final class SpawnerEditorScreen extends Screen {
    private final Screen parent;
    private EditBox mobId, delay, minDelay, maxDelay, count, range;
    private String status = "Stand on or above the target spawner block";
    private long openedAt;
    public SpawnerEditorScreen(Screen parent) { super(Component.literal("Spawner Lab")); this.parent = parent; }

    @Override protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = (height - 300) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 300));
        mobId = field(x, y + 56, w - 44, "Entity ID", "minecraft:zombie");
        delay = field(x, y + 100, (w - 52) / 2, "Delay ticks", "20");
        minDelay = field(x + 30 + (w - 52) / 2, y + 100, (w - 52) / 2, "Min delay", "200");
        maxDelay = field(x, y + 144, (w - 52) / 2, "Max delay", "800");
        count = field(x + 30 + (w - 52) / 2, y + 144, (w - 52) / 2, "Spawn count", "4");
        range = field(x, y + 188, w - 44, "Activation range", "16");
        addRenderableWidget(Button.builder(Component.literal("APPLY TO SPAWNER BELOW"), b -> apply())
                .bounds(x + 22, y + 222, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("BACK"), b -> back())
                .bounds(x + 22, y + 250, w - 44, 20).build());
    }
    private EditBox field(int x, int y, int w, String hint, String value) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(hint));
        box.setMaxLength(128); box.setValue(value); addRenderableWidget(box); return box;
    }
    private Integer number(EditBox box, int min, int max) {
        try { int n = Integer.parseInt(box.getValue().trim()); return n >= min && n <= max ? n : null; }
        catch (NumberFormatException e) { return null; }
    }
    private void apply() {
        String id = mobId.getValue().trim();
        Integer d = number(delay, 0, 32767), min = number(minDelay, 1, 32767), max = number(maxDelay, 1, 32767), c = number(count, 1, 32767), r = number(range, 1, 128);
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || d == null || min == null || max == null || c == null || r == null || min > max) {
            status = "Check entity ID and numeric ranges (min delay <= max delay)"; return;
        }
        String command = "data merge block ~ ~-1 ~ {Delay:" + d + ",MinSpawnDelay:" + min + ",MaxSpawnDelay:" + max
                + ",SpawnCount:" + c + ",RequiredPlayerRange:" + r
                + ",SpawnData:{entity:{id:\"" + id + "\"}}}";
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand(command);
            status = "Command sent; requires permission and a spawner below you";
        }
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = (height - 300) / 2;
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3f);
        y += Math.round((1f - eased) * 10f);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 17, 0xFFF4F0FF, false);
        g.text(font, "SPAWNER LAB  /  07", x + 28, y + 30, 0xFFB4A9C9, false);
        g.text(font, "MOB ID", x + 22, y + 44, 0xFFB49AFF, false);
        g.text(font, "DELAY TICKS", x + 22, y + 88, 0xFFB49AFF, false);
        g.text(font, "MIN DELAY", x + 34 + (w - 52) / 2, y + 88, 0xFFB49AFF, false);
        g.text(font, "MAX DELAY", x + 22, y + 132, 0xFFB49AFF, false);
        g.text(font, "SPAWN COUNT", x + 34 + (w - 52) / 2, y + 132, 0xFFB49AFF, false);
        g.text(font, "ACTIVATION RANGE", x + 22, y + 176, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 222, w - 44, 22, "APPLY TO SPAWNER BELOW", mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 250, w - 44, 20, "BACK", mx, my);
        g.text(font, status, x + 22, y + 282, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
