package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

/** Main All-Kotoff-Want item workshop. */
public final class ItemEditorScreen extends Screen {
    private static final int PURPLE_BRIGHT = 0xFFB49AFF;
    private static final int TEXT = 0xFFF4F0FF;
    private static final int MUTED = 0xFFB4A9C9;

    private EditBox countField;
    private long openedAt;
    private String status = UiText.t("Наведи на ячейку инвентаря и нажми R");
    private final net.minecraft.client.gui.screens.Screen inventoryBackdrop;

    public ItemEditorScreen(Component title) { this(title, null); }

    public ItemEditorScreen(Component title, net.minecraft.client.gui.screens.Screen inventoryBackdrop) {
        super(title);
        this.inventoryBackdrop = inventoryBackdrop;
    }

    @Override
    protected void init() {
        openedAt = System.nanoTime();
        ButtonSkin.beginAnimation();
        int panelWidth = Math.min(430, this.width - 24);
        int left = (this.width - panelWidth) / 2;

        int panelTop = (this.height - 310) / 2;
        addRenderableOnly(new PanelWidget(left, panelTop, panelWidth, 310));
        addRenderableWidget(Button.builder(Component.literal(UiText.languageLabel()), b -> {
            UiText.nextLanguage();
            if (minecraft != null) minecraft.setScreen(new ItemEditorScreen(Component.literal(UiText.t("All-Kotoff-Want")), inventoryBackdrop));
        }).bounds(left + panelWidth - 132, panelTop + 14, 110, 20).build());
        int fieldWidth = panelWidth - 44;

        ItemStack stack = currentStack();
        int countX = left + 22;
        countField = new EditBox(this.font, countX, panelTop + 76, 70, 22, Component.literal(UiText.t("Количество")));
        countField.setMaxLength(7);
        countField.setValue(EditorContext.draft("item.count", Integer.toString(Math.max(1, stack.getCount()))));
        countField.setResponder(v -> EditorContext.setDraft("item.count", v));
        countField.setHint(Component.literal(UiText.t("1")));
        addRenderableWidget(countField);
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить")), b -> applyCount())
                .bounds(countX + 76, panelTop + 76, 82, 22).build());



        int buttonY = panelTop + 112;
        int gap = 8;
        int buttonWidth = (fieldWidth - gap) / 2;
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Текст и описание")), b -> {
                    if (minecraft != null) minecraft.setScreen(new TextEditorScreen(this));
                }).bounds(left + 22, buttonY, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Зачарования")), b -> {
                    if (minecraft != null) minecraft.setScreen(new EnchantmentEditorScreen(this));
                }).bounds(left + 22 + buttonWidth + gap, buttonY, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Атрибуты")), b -> {
                    if (minecraft != null) minecraft.setScreen(new AttributeEditorScreen(this));
                }).bounds(left + 22, buttonY + 28, buttonWidth, 20).build());
        if (isLeatherDyeable(stack)) {
            addRenderableWidget(Button.builder(Component.literal(UiText.t("Цвет брони")), b -> {
                        if (minecraft != null) minecraft.setScreen(new DyeEditorScreen(this));
                    }).bounds(left + 22 + buttonWidth + gap, buttonY + 28, buttonWidth, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal(UiText.t("Настройки моба")), b -> {
                        if (minecraft != null) minecraft.setScreen(new EntityEditorScreen(this));
                    }).bounds(left + 22 + buttonWidth + gap, buttonY + 28, buttonWidth, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сохранённые предметы")), b -> {
                    if (minecraft != null) minecraft.setScreen(new SavedItemsScreen(this));
                }).bounds(left + 22, buttonY + 54, fieldWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("О моде")), b -> {
                    if (minecraft != null) minecraft.setScreen(new AboutModScreen(this));
                }).bounds(left + 22, buttonY + 80, fieldWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сбросить всё до заводских")), b -> resetAll())
                .bounds(left + 22, panelTop + 244, fieldWidth, 20).build());
            ButtonSkin.hideVanillaButtons(this);
    }

    private ItemStack currentStack() {
        return EditorContext.stack();
    }

    private static boolean isLeatherDyeable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.startsWith("leather_");
    }

    private void applyCount() {
        ItemStack stack = currentStack();
        if (stack.isEmpty()) { status = UiText.t("В выбранной ячейке нет предмета"); return; }
        try {
            int count = Integer.parseInt(countField.getValue().trim());
            if (count < 1 || count > 9_999_999) { status = UiText.t("Количество должно быть от 1 до 9999999"); return; }
            int naturalMax = stack.getItem().getDefaultMaxStackSize();
            if (count > naturalMax) {
                // Keep the oversized amount as a real stack. A unique component makes independently
                // created oversized stacks distinct, so they do not merge with each other on pickup.
                stack.set(DataComponents.MAX_STACK_SIZE, Math.min(count, 99));
                net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack,
                        tag -> tag.putString("all_kotoff_stack_id", java.util.UUID.randomUUID().toString()));
            } else {
                stack.remove(DataComponents.MAX_STACK_SIZE);
                net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove("all_kotoff_stack_id"));
            }
            stack.setCount(count);
            EditorContext.setDraft("item.count", Integer.toString(count));
            status = ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
        } catch (NumberFormatException e) {
            status = UiText.t("Введи целое количество от 1 до 9999999");
        }
    }

    private void resetAll() {
        ItemStack stack = currentStack();
        if (stack.isEmpty()) { status = UiText.t("В выбранной ячейке нет предмета"); return; }
        stack.remove(DataComponents.CUSTOM_NAME);
        stack.remove(DataComponents.LORE);
        stack.remove(DataComponents.ENCHANTMENTS);
        stack.remove(DataComponents.STORED_ENCHANTMENTS);
        stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        stack.remove(DataComponents.MAX_STACK_SIZE);
        stack.remove(DataComponents.DYED_COLOR);
        stack.setCount(EditorContext.originalCount());
        countField.setValue(Integer.toString(EditorContext.originalCount()));
        EditorContext.clearDrafts();
        EditorContext.setDraft("item.count", Integer.toString(EditorContext.originalCount()));
        EditorContext.nameDraft("");
        EditorContext.loreDraft("");
        boolean synced = ItemSync.syncIfCreative(stack);
        status = synced ? UiText.t("Параметры редактора сброшены") : UiText.t("Сброшено локально; для отправки нужен творческий режим");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EditorContext.renderBackdrop(this, graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        long elapsed = Math.max(0L, System.nanoTime() - openedAt);
        float t = Math.min(1.0f, elapsed / 190_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0);
        int panelWidth = Math.min(430, this.width - 24);
        int panelHeight = 310;
        int x = (this.width - panelWidth) / 2;
        int targetY = (this.height - panelHeight) / 2;
        int y = targetY + Math.round((1.0f - eased) * 12.0f);

        int by = targetY + 112;
        int fw = panelWidth - 44;
        int bw = (fw - 8) / 2;
        ButtonSkin.draw(graphics, this.font, x + panelWidth - 132, targetY + 14, 110, 20, UiText.languageLabel(), mouseX, mouseY);
        int countX = x + 22;
        ButtonSkin.draw(graphics, this.font, countX + 76, targetY + 76, 82, 22, UiText.t("Применить"), mouseX, mouseY);
        graphics.text(this.font, UiText.t("Количество"), countX, targetY + 61, PURPLE_BRIGHT, false);
        ButtonSkin.draw(graphics, this.font, x + 22, by, bw, 22, UiText.t("Текст и описание"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by, bw, 22, UiText.t("Зачарования"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 28, bw, 20, UiText.t("Атрибуты"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 28, bw, 20,
                isLeatherDyeable(currentStack()) ? UiText.t("Цвет брони") : UiText.t("Настройки моба"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 54, fw, 22, UiText.t("Сохранённые предметы"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 80, fw, 20, UiText.t("О моде"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, targetY + 244, fw, 20, UiText.t("Сбросить всё до заводских"), mouseX, mouseY);
        graphics.text(this.font, UiText.t("ALL-KOTOFF-WANT"), x + 28, y + 17, TEXT, false);
        graphics.text(this.font, UiText.t("Редактор предмета"), x + 28, y + 30, MUTED, false);
        ItemStack selected = currentStack();
        String selectedLabel = selected.isEmpty() ? UiText.t("Предмет не выбран") : selected.getHoverName().getString();
        if (!selected.isEmpty()) graphics.item(selected, x + 22, y + 42);
        graphics.text(this.font, selectedLabel, x + (selected.isEmpty() ? 22 : 44), y + 50, MUTED, false);
        graphics.text(this.font, status, x + 22, y + 282, TEXT, false);
        graphics.text(this.font, UiText.t("R  •  ОТКРЫТЬ РЕДАКТОР"), x + 22, y + 296, MUTED, false);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            net.minecraft.client.gui.screens.Screen backdrop = EditorContext.backdrop();
            minecraft.setScreen(backdrop != null ? backdrop : null);
            EditorContext.clear();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
