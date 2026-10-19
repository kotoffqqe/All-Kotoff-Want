package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Main All-Kotoff-Want item workshop. */
public final class ItemEditorScreen extends Screen {
    private static final int PURPLE_BRIGHT = 0xFFB49AFF;
    private static final int TEXT = 0xFFF4F0FF;
    private static final int MUTED = 0xFFB4A9C9;

    private EditBox nameField;
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

        int panelTop = (this.height - 280) / 2;
        addRenderableOnly(new PanelWidget(left, panelTop, panelWidth, 280));
        addRenderableWidget(Button.builder(Component.literal(UiText.languageLabel()), b -> {
            UiText.nextLanguage();
            if (minecraft != null) minecraft.setScreen(new ItemEditorScreen(Component.literal("All-Kotoff-Want"), inventoryBackdrop));
        }).bounds(left + panelWidth - 132, panelTop + 14, 110, 20).build());
        int fieldWidth = panelWidth - 44;

        ItemStack stack = currentStack();
        String existing = stack.get(DataComponents.CUSTOM_NAME) == null
                ? "" : stack.get(DataComponents.CUSTOM_NAME).getString();
        nameField = new EditBox(this.font, left + 22, panelTop + 76, fieldWidth, 22,
                Component.literal(UiText.t("Пользовательское название предмета")));
        nameField.setMaxLength(128);
        nameField.setValue(existing);
        nameField.setHint(Component.literal(UiText.t("Например: Разрушитель пустоты")));
        addRenderableWidget(nameField);

        int buttonY = panelTop + 104;
        int gap = 8;
        int buttonWidth = (fieldWidth - gap) / 2;
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить имя")), b -> applyName())
                .bounds(left + 22, buttonY, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сбросить имя")), b -> resetName())
                .bounds(left + 22 + buttonWidth + gap, buttonY, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Описание")), b -> {
                    if (minecraft != null) minecraft.setScreen(new LoreEditorScreen(this));
                }).bounds(left + 22, buttonY + 30, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Зачарования")), b -> {
                    if (minecraft != null) minecraft.setScreen(new EnchantmentEditorScreen(this));
                }).bounds(left + 22 + buttonWidth + gap, buttonY + 30, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Атрибуты")), b -> {
                    if (minecraft != null) minecraft.setScreen(new AttributeEditorScreen(this));
                }).bounds(left + 22, buttonY + 58, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Настройки моба")), b -> {
                    if (minecraft != null) minecraft.setScreen(new EntityEditorScreen(this));
                }).bounds(left + 22 + buttonWidth + gap, buttonY + 58, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Настройки спавнера")), b -> {
                    if (minecraft != null) minecraft.setScreen(new SpawnerEditorScreen(this));
                }).bounds(left + 22, buttonY + 84, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Закрыть")), b -> onClose())
                .bounds(left + 22 + buttonWidth + gap, buttonY + 84, buttonWidth, 20).build());
    }

    private ItemStack currentStack() {
        return EditorContext.stack();
    }

    private void applyName() {
        ItemStack stack = currentStack();
        if (stack.isEmpty()) {
            status = UiText.t("В выбранной ячейке нет предмета");
            return;
        }
        String value = nameField.getValue().trim();
        if (value.isEmpty()) stack.remove(DataComponents.CUSTOM_NAME);
        else stack.set(DataComponents.CUSTOM_NAME, Component.literal(value));
        boolean synced = ItemSync.syncIfCreative(stack);
        status = value.isEmpty()
                ? (synced ? UiText.t("Имя удалено и отправлено на сервер") : UiText.t("Имя изменено локально; для сохранения нужен творческий режим"))
                : ItemSync.resultMessage(synced);
    }

    private void resetName() {
        nameField.setValue("");
        applyName();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EditorContext.renderBackdrop(this, graphics, mouseX, mouseY, delta);
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
        ButtonSkin.draw(graphics, this.font, x + panelWidth - 132, targetY + 14, 110, 20, UiText.languageLabel(), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by, bw, 22, UiText.t("Применить имя"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by, bw, 22, UiText.t("Сбросить имя"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 30, bw, 22, UiText.t("Описание"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 30, bw, 22, UiText.t("Зачарования"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 58, bw, 20, UiText.t("Атрибуты"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 58, bw, 20, UiText.t("Настройки моба"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 84, bw, 20, UiText.t("Настройки спавнера"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 84, bw, 20, UiText.t("Закрыть"), mouseX, mouseY);
        graphics.text(this.font, "ALL-KOTOFF-WANT", x + 28, y + 17, TEXT, false);
        graphics.text(this.font, UiText.t("РЕДАКТОР ПРЕДМЕТА  /  01"), x + 28, y + 30, MUTED, false);
        graphics.text(this.font, UiText.t("ИМЯ МОБА"), x + 22, y + 61, PURPLE_BRIGHT, false);
        graphics.text(this.font, UiText.t("РЕДАКТИРУЕТСЯ ВЫБРАННЫЙ ПРЕДМЕТ"), x + 22, y + 222, MUTED, false);
        graphics.text(this.font, status, x + 22, y + 246, TEXT, false);
        graphics.text(this.font, UiText.t("R  •  ОТКРЫТЬ РЕДАКТОР"), x + 22, y + 260, MUTED, false);
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
