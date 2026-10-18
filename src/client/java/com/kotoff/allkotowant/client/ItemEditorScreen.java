package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** First implementation milestone: dark-violet UI and a working main-hand custom-name editor. */
public final class ItemEditorScreen extends Screen {
    private static final int PURPLE_BRIGHT = 0xFFB49AFF;
    private static final int TEXT = 0xFFF4F0FF;
    private static final int MUTED = 0xFFB4A9C9;

    private EditBox nameField;
    private long openedAt;
    private String status = "Select an item in your main hand";

    public ItemEditorScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        openedAt = System.nanoTime();
        int panelWidth = Math.min(430, this.width - 24);
        int left = (this.width - panelWidth) / 2;

        int panelTop = (this.height - 280) / 2;
        addRenderableWidget(new PanelWidget(left, panelTop, panelWidth, 280));
        int fieldWidth = panelWidth - 44;

        ItemStack stack = currentStack();
        String existing = stack.get(DataComponents.CUSTOM_NAME) == null
                ? "" : stack.get(DataComponents.CUSTOM_NAME).getString();
        nameField = new EditBox(this.font, left + 22, panelTop + 76, fieldWidth, 22,
                Component.literal("Custom item name"));
        nameField.setMaxLength(128);
        nameField.setValue(existing);
        nameField.setHint(Component.literal("e.g. Voidbreaker"));
        addRenderableWidget(nameField);

        int buttonY = panelTop + 104;
        int gap = 8;
        int buttonWidth = (fieldWidth - gap) / 2;
        addRenderableWidget(Button.builder(Component.literal("APPLY NAME"), b -> applyName())
                .bounds(left + 22, buttonY, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("RESET NAME"), b -> resetName())
                .bounds(left + 22 + buttonWidth + gap, buttonY, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("LORE EDITOR"), b -> {
                    if (minecraft != null) minecraft.setScreen(new LoreEditorScreen(this));
                }).bounds(left + 22, buttonY + 30, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("ENCHANTMENTS"), b -> {
                    if (minecraft != null) minecraft.setScreen(new EnchantmentEditorScreen(this));
                }).bounds(left + 22 + buttonWidth + gap, buttonY + 30, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("ATTRIBUTES"), b -> {
                    if (minecraft != null) minecraft.setScreen(new AttributeEditorScreen(this));
                }).bounds(left + 22, buttonY + 58, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("ENTITY LAB"), b -> {
                    if (minecraft != null) minecraft.setScreen(new EntityEditorScreen(this));
                }).bounds(left + 22 + buttonWidth + gap, buttonY + 58, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("SPAWNER LAB"), b -> {
                    if (minecraft != null) minecraft.setScreen(new SpawnerEditorScreen(this));
                }).bounds(left + 22, buttonY + 84, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(left + 22 + buttonWidth + gap, buttonY + 84, buttonWidth, 20).build());
    }

    private ItemStack currentStack() {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        return minecraft.player.getMainHandItem();
    }

    private void applyName() {
        ItemStack stack = currentStack();
        if (stack.isEmpty()) {
            status = "Nothing in your main hand — hold an item first";
            return;
        }
        String value = nameField.getValue().trim();
        if (value.isEmpty()) stack.remove(DataComponents.CUSTOM_NAME);
        else stack.set(DataComponents.CUSTOM_NAME, Component.literal(value));
        boolean synced = ItemSync.syncIfCreative(stack);
        status = value.isEmpty()
                ? (synced ? "Custom name cleared and synced" : "Name cleared locally only; creative mode/server support required to save")
                : ItemSync.resultMessage(synced);
    }

    private void resetName() {
        nameField.setValue("");
        applyName();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1.0f, elapsed / 190_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0);
        int panelWidth = Math.min(430, this.width - 24);
        int panelHeight = 280;
        int x = (this.width - panelWidth) / 2;
        int targetY = (this.height - panelHeight) / 2;
        int y = targetY + Math.round((1.0f - eased) * 12.0f);

        int by = targetY + 104;
        int fw = panelWidth - 44;
        int bw = (fw - 8) / 2;
        ButtonSkin.draw(graphics, this.font, x + 22, by, bw, 22, "APPLY NAME", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by, bw, 22, "RESET NAME", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 30, bw, 22, "LORE EDITOR", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 30, bw, 22, "ENCHANTMENTS", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 58, bw, 20, "ATTRIBUTES", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 58, bw, 20, "ENTITY LAB", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 84, bw, 20, "SPAWNER LAB", mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 84, bw, 20, "CLOSE", mouseX, mouseY);
        graphics.text(this.font, "ALL-KOTOFF-WANT", x + 28, y + 17, TEXT, false);
        graphics.text(this.font, "ITEM WORKSHOP  /  01", x + 28, y + 30, MUTED, false);
        graphics.text(this.font, "CUSTOM NAME", x + 22, y + 61, PURPLE_BRIGHT, false);
        graphics.text(this.font, "EDITING MAIN-HAND ITEM", x + 22, y + 222, MUTED, false);
        graphics.text(this.font, status, x + 22, y + 246, TEXT, false);
        graphics.text(this.font, "R  •  OPEN EDITOR", x + 22, y + 260, MUTED, false);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
