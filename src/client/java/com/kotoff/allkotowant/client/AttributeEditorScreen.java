package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.List;
import java.util.Locale;

/** Compact attribute editor. Existing vanilla modifiers are retained; only this mod's selected modifier is replaced. */
public final class AttributeEditorScreen extends Screen {
    private record AttrOption(String id, String name, Holder<Attribute> attribute) {}
    private static final List<AttrOption> OPTIONS = List.of(
        new AttrOption("attack_damage", "Урон атаки", Attributes.ATTACK_DAMAGE),
        new AttrOption("attack_speed", "Скорость атаки", Attributes.ATTACK_SPEED),
        new AttrOption("attack_knockback", "Отбрасывание атаки", Attributes.ATTACK_KNOCKBACK),
        new AttrOption("armor", "Броня", Attributes.ARMOR),
        new AttrOption("armor_toughness", "Твёрдость брони", Attributes.ARMOR_TOUGHNESS),
        new AttrOption("knockback_resistance", "Сопротивление отбрасыванию", Attributes.KNOCKBACK_RESISTANCE),
        new AttrOption("max_health", "Максимальное здоровье", Attributes.MAX_HEALTH),
        new AttrOption("movement_speed", "Скорость передвижения", Attributes.MOVEMENT_SPEED),
        new AttrOption("flying_speed", "Скорость полёта", Attributes.FLYING_SPEED),
        new AttrOption("follow_range", "Дальность обнаружения", Attributes.FOLLOW_RANGE),
        new AttrOption("luck", "Удача", Attributes.LUCK),
        new AttrOption("jump_strength", "Сила прыжка", Attributes.JUMP_STRENGTH),
        new AttrOption("block_break_speed", "Скорость разрушения блоков", Attributes.BLOCK_BREAK_SPEED),
        new AttrOption("block_interaction_range", "Дальность взаимодействия с блоками", Attributes.BLOCK_INTERACTION_RANGE),
        new AttrOption("entity_interaction_range", "Дальность взаимодействия с сущностями", Attributes.ENTITY_INTERACTION_RANGE),
        new AttrOption("gravity", "Гравитация", Attributes.GRAVITY),
        new AttrOption("safe_fall_distance", "Безопасная высота падения", Attributes.SAFE_FALL_DISTANCE),
        new AttrOption("step_height", "Высота шага", Attributes.STEP_HEIGHT),
        new AttrOption("sneaking_speed", "Скорость в приседе", Attributes.SNEAKING_SPEED),
        new AttrOption("water_movement_efficiency", "Эффективность движения в воде", Attributes.WATER_MOVEMENT_EFFICIENCY),
        new AttrOption("movement_efficiency", "Эффективность передвижения", Attributes.MOVEMENT_EFFICIENCY),
        new AttrOption("mining_efficiency", "Эффективность добычи", Attributes.MINING_EFFICIENCY),
        new AttrOption("scale", "Масштаб сущности", Attributes.SCALE),
        new AttrOption("max_absorption", "Максимальное поглощение", Attributes.MAX_ABSORPTION),
        new AttrOption("fall_damage_multiplier", "Множитель урона от падения", Attributes.FALL_DAMAGE_MULTIPLIER),
        new AttrOption("explosion_knockback_resistance", "Сопротивление отбрасыванию взрывом", Attributes.EXPLOSION_KNOCKBACK_RESISTANCE),
        new AttrOption("oxygen_bonus", "Бонус кислорода", Attributes.OXYGEN_BONUS)
    );
    private final Screen parent;
    private int selected;
    private Button choiceButton;
    private EditBox amountField;
    private boolean loading;
    private String status = UiText.t("Базовые характеристики предмета сохраняются");

    public AttributeEditorScreen(Screen parent) {
        super(Component.literal(UiText.t("All-Kotoff-Want: Атрибуты")));
        this.parent = parent;
    }

    @Override protected void init() {
        try { selected = Math.max(0, Math.min(OPTIONS.size() - 1, Integer.parseInt(EditorContext.draft("attr.selected", "0")))); }
        catch (NumberFormatException ignored) { selected = 0; }
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 280) / 2;
        addRenderableOnly(new PanelWidget(x, y, w, 280));
        choiceButton = Button.builder(choiceLabel(), b -> {
            if (minecraft != null) minecraft.setScreen(new CompactPickerScreen(this, UiText.t("Выбрать атрибут"),
                    OPTIONS.stream().map(AttrOption::name).map(UiText::t).toList(), selected, i -> {
                        selected = i;
                        EditorContext.setDraft("attr.selected", Integer.toString(i));
                        if (choiceButton != null) choiceButton.setMessage(choiceLabel());
                        loadAmount();
                    }));
        }).bounds(x + 22, y + 58, w - 44, 24).build();
        addRenderableWidget(choiceButton);
        amountField = new EditBox(font, x + 22, y + 112, w - 44, 22, Component.literal(UiText.t("Значение атрибута")));
        amountField.setMaxLength(18);
        amountField.setResponder(v -> { if (!loading) EditorContext.setDraft("attr.value." + OPTIONS.get(selected).id(), v); });
        addRenderableWidget(amountField);
        loadAmount();
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить значение")), b -> apply()).bounds(x + 22, y + 148, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Убрать изменение этого атрибута")), b -> remove()).bounds(x + 22, y + 176, w - 44, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x + 22, y + 204, w - 44, 20).build());
            ButtonSkin.hideVanillaButtons(this);
    }

    private Component choiceLabel() { return Component.literal(UiText.t("Атрибут: ") + UiText.t(OPTIONS.get(selected).name()) + "  ›"); }

    private ItemAttributeModifiers baseModifiers(ItemStack stack) {
        ItemAttributeModifiers explicit = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        return explicit != null ? explicit : stack.getItem().components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    private double sum(ItemAttributeModifiers modifiers, Holder<Attribute> attribute, boolean onlyMod) {
        return modifiers.modifiers().stream()
                .filter(e -> e.attribute().equals(attribute))
                .filter(e -> !onlyMod || e.modifier().id().getNamespace().equals("allkotowant"))
                .mapToDouble(e -> e.modifier().amount()).sum();
    }

    private String formatted(double value) { return String.format(Locale.ROOT, "%+.3f", value); }

    private void loadAmount() {
        if (amountField == null) return;
        AttrOption option = OPTIONS.get(selected);
        String draft = EditorContext.draft("attr.value." + option.id(), "");
        if (!draft.isEmpty()) { loading = true; amountField.setValue(draft); loading = false; return; }
        ItemStack stack = EditorContext.stack();
        double existing = stack.isEmpty() ? 0 : baseModifiers(stack).modifiers().stream()
                .filter(e -> e.modifier().id().toString().equals("allkotowant:" + option.id()))
                .mapToDouble(e -> e.modifier().amount()).findFirst().orElse(0.0);
        loading = true; amountField.setValue(Double.toString(existing)); loading = false;
        EditorContext.setDraft("attr.value." + option.id(), amountField.getValue());
    }

    private Double parse() {
        try { double v = Double.parseDouble(amountField.getValue().trim()); return Double.isFinite(v) && Math.abs(v) <= 1_000_000 ? v : null; }
        catch (NumberFormatException e) { return null; }
    }

    private ItemAttributeModifiers replaceOurModifier(ItemAttributeModifiers existing, AttrOption option, Double amount) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            if (!entry.modifier().id().toString().equals("allkotowant:" + option.id()))
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        if (amount != null && amount != 0.0) {
            EquipmentSlotGroup slot = switch (option.id()) {
                case "armor", "armor_toughness", "knockback_resistance", "max_health", "safe_fall_distance" -> EquipmentSlotGroup.ARMOR;
                default -> EquipmentSlotGroup.MAINHAND;
            };
            builder.add(option.attribute(), new AttributeModifier(Identifier.fromNamespaceAndPath("allkotowant", option.id()), amount, AttributeModifier.Operation.ADD_VALUE), slot);
        }
        return builder.build();
    }

    private void apply() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        Double value = parse();
        if (value == null) { status = UiText.t("Введи число от −1000000 до 1000000"); return; }
        ItemAttributeModifiers existing = baseModifiers(stack);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, replaceOurModifier(existing, OPTIONS.get(selected), value));
        status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void remove() {
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        ItemAttributeModifiers existing = baseModifiers(stack);
        ItemAttributeModifiers updated = replaceOurModifier(existing, OPTIONS.get(selected), null);
        if (updated.modifiers().isEmpty()) stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        else stack.set(DataComponents.ATTRIBUTE_MODIFIERS, updated);
        EditorContext.setDraft("attr.value." + OPTIONS.get(selected).id(), "0");
        loading = true; amountField.setValue("0"); loading = false;
        status = UiText.t("Изменение этого атрибута удалено") + ". " + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }

    private void back() { if (minecraft != null) minecraft.setScreen(parent); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        EditorContext.renderBackdrop(this, g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        int w = Math.min(430, width - 24), x = (width - w) / 2, y = (height - 280) / 2;
        ItemStack stack = EditorContext.stack();
        AttrOption option = OPTIONS.get(selected);
        ItemAttributeModifiers defaults = stack.isEmpty() ? ItemAttributeModifiers.EMPTY : stack.getItem().components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers current = stack.isEmpty() ? ItemAttributeModifiers.EMPTY : baseModifiers(stack);
        g.text(font, "ALL-KOTOFF-WANT", x + 22, y + 17, 0xFFF4F0FF, false);
        g.text(font, UiText.t("АТРИБУТЫ"), x + 22, y + 34, 0xFFB49AFF, false);
        g.text(font, UiText.t("Стандарт") + ": " + formatted(sum(defaults, option.attribute(), false)), x + 22, y + 88, 0xFFB4A9C9, false);
        g.text(font, UiText.t("Сейчас на предмете") + ": " + formatted(sum(current, option.attribute(), false)), x + 22, y + 99, 0xFFB4A9C9, false);
        ButtonSkin.draw(g, font, x + 22, y + 58, w - 44, 24, UiText.t("Атрибут: ") + UiText.t(OPTIONS.get(selected).name()) + "  ›", mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 148, w - 44, 22, UiText.t("Применить значение"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 176, w - 44, 22, UiText.t("Убрать изменение этого атрибута"), mx, my);
        ButtonSkin.draw(g, font, x + 22, y + 204, w - 44, 20, UiText.t("Назад"), mx, my);
        g.text(font, status, x + 22, y + 244, 0xFFF4F0FF, false);
    }

    @Override public void onClose() { back(); }
    @Override public boolean isPauseScreen() { return false; }
}
