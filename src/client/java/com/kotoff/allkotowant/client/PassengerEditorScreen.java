package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Creates a summon command with one passenger entity. */
public final class PassengerEditorScreen extends Screen {
    private final Screen parent;
    private EditBox vehicleId, passengerId;
    private String status = "Creates a new vehicle + passenger";
    private long openedAt;
    public PassengerEditorScreen(Screen parent) { super(Component.literal("Passenger Summoner")); this.parent = parent; }

    @Override protected void init() {
        openedAt = System.nanoTime();
        int w = Math.min(420, width - 24), x = (width - w) / 2, y = (height - 220) / 2;
        addRenderableWidget(new PanelWidget(x, y, w, 220));
        vehicleId = new EditBox(font, x + 22, y + 62, w - 44, 22, Component.literal("Vehicle entity ID"));
        vehicleId.setMaxLength(128); vehicleId.setValue("minecraft:zombie"); addRenderableWidget(vehicleId);
        passengerId = new EditBox(font, x + 22, y + 108, w - 44, 22, Component.literal("Passenger entity ID"));
        passengerId.setMaxLength(128); passengerId.setValue("minecraft:chicken"); addRenderableWidget(passengerId);
        addRenderableWidget(Button.builder(Component.literal("SUMMON PASSENGER STACK"), b -> summon())
                .bounds(x + 22, y + 146, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("BACK"), b -> back())
                .bounds(x + 22, y + 174, w - 44, 20).build());
    }
    private boolean valid(String s) { return s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"); }
    private void summon() {
        String vehicle = vehicleId.getValue().trim(), passenger = passengerId.getValue().trim();
        if (!valid(vehicle) || !valid(passenger)) { status = "Use valid namespaced entity IDs"; return; }
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand("summon " + vehicle + " ~ ~ ~ {Passengers:[{id:\"" + passenger + "\"}]}");
            status = "Summon command sent; requires command permission";
        }
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(420, width - 24), x = (width - w) / 2, y = (height - 220) / 2;
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3);
        y += Math.round((1f - eased) * 10f);
        g.text(font, "ALL-KOTOFF-WANT", x + 28, y + 17, 0xFFF4F0FF, false);
        g.text(font, "PASSENGER BUILDER  /  06", x + 28, y + 31, 0xFFB4A9C9, false);
        g.text(font, "VEHICLE ID", x + 22, y + 49, 0xFFB49AFF, false);
        g.text(font, "PASSENGER ID", x + 22, y + 95, 0xFFB49AFF, false);
        ButtonSkin.draw(g, font, x + 22, y + 146, w - 44, 22, "SUMMON PASSENGER STACK", mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 174, w - 44, 20, "BACK", mx, my);
        g.text(font, status, x + 22, y + 203, 0xFFEAE2FF, false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
