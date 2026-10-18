package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Sets simple custom main-hand attack damage and attack speed modifiers. */
public final class AttributeEditorScreen extends Screen {
    private final Screen parent;
    private EditBox damageField;
    private EditBox speedField;
    private String status = "Values are modifier bonuses";
    private long openedAt;

    public AttributeEditorScreen(Screen parent) {
        super(Component.literal("All-Kotoff-Want: Attributes"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int w = Math.min(430, width - 24);
        int x = (width - w) / 2;
        addRenderableOnly(new PanelWidget(x, (height - 250) / 2, w, 250));
        damageField = new EditBox(font, x + 22, height / 2 - 42, w - 44, 22, Component.literal("Attack damage bonus"));
        damageField.setMaxLength(10);
        damageField.setValue("10");
        addRenderableWidget(damageField);
        speedField = new EditBox(font, x + 22, height / 2 - 2, w - 44, 22, Component.literal("Attack speed bonus"));
        speedField.setMaxLength(10);
        speedField.setValue("1");
        addRenderableWidget(speedField);
        addRenderableWidget(Button.builder(Component.literal("APPLY ATTRIBUTES"), b -> apply())
                .bounds(x + 22, height / 2 + 34, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("CLEAR CUSTOM ATTRIBUTES"), b -> clear())
                .bounds(x + 22, height / 2 + 62, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal("BACK"), b -> back())
                .bounds(x + 22, height / 2 + 90, w - 44, 20).build());
    }

    private ItemStack held() {
        return EditorContext.stack();
    }

    private Double parse(EditBox field) {
        try {
            double value = Double.parseDouble(field.getValue().trim());
            return Double.isFinite(value) && Math.abs(value) <= 1_000_000 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void apply() {
        ItemStack stack = held();
        if (stack.isEmpty()) { status = "Hold an item first"; return; }
        Double damage = parse(damageField);
        Double speed = parse(speedField);
        if (damage == null || speed == null) { status = "Enter valid numbers (max absolute value 1,000,000)"; return; }
        // This editor intentionally installs a clean pair of custom main-hand modifiers.
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("allkotowant", "attack_damage"), damage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("allkotowant", "attack_speed"), speed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void clear() {
        ItemStack stack = held();
        if (stack.isEmpty()) { status = "Hold an item first"; return; }
        stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        boolean synced = ItemSync.syncIfCreative(stack);
        status = synced ? "Attribute component cleared and synced" : "Cleared locally only; creative mode/server support required to save";
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EditorContext.renderBackdrop(this, graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1.0f, elapsed / 190_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0f);
        int w = Math.min(430, width - 24);
        int x = (width - w) / 2;
        int y = (height - 250) / 2 + Math.round((1.0f - eased) * 12.0f);
        ButtonSkin.draw(graphics, font, x + 22, height / 2 + 34, w - 44, 22, "APPLY ATTRIBUTES", mouseX, mouseY);
        ButtonSkin.draw(graphics, font, x + 22, height / 2 + 62, w - 44, 22, "CLEAR CUSTOM ATTRIBUTES", mouseX, mouseY);
        ButtonSkin.draw(graphics, font, x + 22, height / 2 + 90, w - 44, 20, "BACK", mouseX, mouseY);
        graphics.text(font, "ALL-KOTOFF-WANT", x + 28, y + 17, 0xFFF4F0FF, false);
        graphics.text(font, "ATTRIBUTE LAB  /  04", x + 28, y + 30, 0xFFB4A9C9, false);
        graphics.text(font, "ATTACK DAMAGE BONUS", x + 22, y + 47, 0xFFB49AFF, false);
        graphics.text(font, "ATTACK SPEED BONUS", x + 22, y + 87, 0xFFB49AFF, false);
        graphics.text(font, status, x + 22, y + 220, 0xFFF4F0FF, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
