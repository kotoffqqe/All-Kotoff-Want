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

/** Adds our modifiers without deleting the item's existing/default modifiers. */
public final class AttributeEditorScreen extends Screen {
    private final Screen parent;
    private EditBox damageField, speedField, movementField;
    private String status = UiText.t("Базовые характеристики предмета сохраняются");
    private long openedAt;

    public AttributeEditorScreen(Screen parent) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Атрибуты")));
        this.parent = parent;
    }

    @Override protected void init() {
        openedAt = System.nanoTime(); ButtonSkin.beginAnimation();
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 290) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 290));
        damageField = new EditBox(font, x + 22, y + 62, w - 44, 22, Component.literal(UiText.t("Бонус урона")));
        damageField.setMaxLength(10); damageField.setValue("10"); addRenderableWidget(damageField);
        speedField = new EditBox(font, x + 22, y + 102, w - 44, 20, Component.literal(UiText.t("Бонус скорости атаки")));
        speedField.setMaxLength(10); speedField.setValue("1"); addRenderableWidget(speedField);
        movementField = new EditBox(font, x + 22, y + 140, w - 44, 20, Component.literal(UiText.t("Бонус скорости передвижения")));
        movementField.setMaxLength(10); movementField.setValue("0.05"); addRenderableWidget(movementField);
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Добавить бонусы")), b -> apply()).bounds(x + 22, y + 174, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Убрать только бонусы мода")), b -> clear()).bounds(x + 22, y + 202, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x + 22, y + 230, w - 44, 20).build());
    }

    private Double parse(EditBox field) {
        try { double v = Double.parseDouble(field.getValue().trim()); return Double.isFinite(v) && Math.abs(v) <= 1_000_000 ? v : null; }
        catch (NumberFormatException e) { return null; }
    }

    private ItemAttributeModifiers withOurModifiers(ItemAttributeModifiers existing, Double damage, Double speed, Double movement) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            if (!entry.modifier().id().getNamespace().equals("allkotowant")) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        if (damage != null) builder.add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Identifier.fromNamespaceAndPath("allkotowant", "attack_damage"), damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        if (speed != null) builder.add(Attributes.ATTACK_SPEED,
                new AttributeModifier(Identifier.fromNamespaceAndPath("allkotowant", "attack_speed"), speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        if (movement != null) builder.add(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(Identifier.fromNamespaceAndPath("allkotowant", "movement_speed"), movement, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }

    private void apply() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        Double damage = parse(damageField), speed = parse(speedField), movement = parse(movementField);
        if (damage == null || speed == null || movement == null) { status = UiText.t("Введи числа от −1 000 000 до 1 000 000"); return; }
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        // Repair items edited by older builds that replaced the full component with only our modifiers.
        if (!existing.modifiers().isEmpty() && existing.modifiers().stream().allMatch(e -> e.modifier().id().getNamespace().equals("allkotowant"))) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, withOurModifiers(existing, damage, speed, movement));
        status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void clear() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        boolean hasNonMod = existing.modifiers().stream().anyMatch(e -> !e.modifier().id().getNamespace().equals("allkotowant"));
        if (!hasNonMod) {
            // Removing the override lets Minecraft restore the item's own default attributes (e.g. armor).
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        } else {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, withOurModifiers(existing, null, null, null));
        }
        status = UiText.t("Убраны только бонусы All-Kotoff-Want. Базовые атрибуты восстановлены. ") + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta); super.extractRenderState(g, mx, my, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt); float t = Math.min(1f, elapsed / 190_000_000f), eased = 1f - (float)Math.pow(1f - t, 3);
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 290) / 2 + Math.round((1f-eased)*12f);
        g.text(font, "ALL-KOTOFF-WANT", x+28,y+17,0xFFF4F0FF,false); g.text(font,UiText.t("АТРИБУТЫ  /  04"),x+28,y+30,0xFFB4A9C9,false);
        g.text(font,UiText.t("БОНУС УРОНА"),x+22,y+49,0xFFB49AFF,false); g.text(font,UiText.t("БОНУС СКОРОСТИ АТАКИ"),x+22,y+89,0xFFB49AFF,false);
        g.text(font,UiText.t("БОНУС СКОРОСТИ ПЕРЕДВИЖЕНИЯ (ПОКА В РУКЕ)"),x+22,y+127,0xFFB49AFF,false);
        ButtonSkin.draw(g,font,x+22,y+174,w-44,22,UiText.t("Добавить бонусы"),mx,my);
        ButtonSkin.draw(g,font,x+22,y+202,w-44,22,UiText.t("Убрать только бонусы мода"),mx,my);
        ButtonSkin.draw(g,font,x+22,y+230,w-44,20,UiText.t("Назад"),mx,my); g.text(font,status,x+22,y+264,0xFFF4F0FF,false);
    }
    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
