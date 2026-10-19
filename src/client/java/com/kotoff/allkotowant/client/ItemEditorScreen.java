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

        int panelTop = (this.height - 330) / 2;
        addRenderableOnly(new PanelWidget(left, panelTop, panelWidth, 330));
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
        String draft = EditorContext.nameDraft();
        nameField.setValue(draft != null ? draft : existing);
        nameField.setResponder(EditorContext::nameDraft);
        nameField.setHint(Component.literal(UiText.t("Например: Разрушитель пустоты")));
        addRenderableWidget(nameField);
        String[][] format = {{UiText.t("Красн."),"&c"},{UiText.t("Зел."),"&a"},{UiText.t("Голуб."),"&b"},{UiText.t("Золото"),"&6"},{UiText.t("Жирн."),"&l"},{UiText.t("Курсив"),"&o"},{UiText.t("Подч."),"&n"},{UiText.t("Зачерк."),"&m"},{UiText.t("Магия"),"&k"},{UiText.t("Сброс"),"&r"}};
        int fmtGap=4, fmtW=(fieldWidth-fmtGap*4)/5;
        for(int i=0;i<format.length;i++) {
            final String code=format[i][1]; int col=i%5,row=i/5;
            addRenderableWidget(Button.builder(Component.literal(format[i][0]),b->nameField.insertText(code))
                .bounds(left+22+col*(fmtW+fmtGap),panelTop+101+row*20,fmtW,18).build());
        }

        int buttonY = panelTop + 148;
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
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Сбросить всё до заводских")), b -> resetAll())
                .bounds(left + 22, panelTop + 258, fieldWidth, 20).build());
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
        else stack.set(DataComponents.CUSTOM_NAME, StyledText.parse(value));
        boolean synced = ItemSync.syncIfCreative(stack);
        status = value.isEmpty()
                ? (synced ? UiText.t("Имя удалено и отправлено на сервер") : UiText.t("Имя изменено локально; для сохранения нужен творческий режим"))
                : ItemSync.resultMessage(synced);
    }

    private void resetName() {
        nameField.setValue("");
        EditorContext.nameDraft("");
        applyName();
    }

    private void resetAll() {
        ItemStack stack = currentStack();
        if (stack.isEmpty()) { status = UiText.t("В выбранной ячейке нет предмета"); return; }
        stack.remove(DataComponents.CUSTOM_NAME);
        stack.remove(DataComponents.LORE);
        stack.remove(DataComponents.ENCHANTMENTS);
        stack.remove(DataComponents.STORED_ENCHANTMENTS);
        stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        nameField.setValue("");
        EditorContext.clearDrafts();
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
        int panelHeight = 330;
        int x = (this.width - panelWidth) / 2;
        int targetY = (this.height - panelHeight) / 2;
        int y = targetY + Math.round((1.0f - eased) * 12.0f);

        int by = targetY + 148;
        int fw = panelWidth - 44;
        int bw = (fw - 8) / 2;
        ButtonSkin.draw(graphics, this.font, x + panelWidth - 132, targetY + 14, 110, 20, UiText.languageLabel(), mouseX, mouseY);
        String[][] format = {{UiText.t("Красн."),"&c"},{UiText.t("Зел."),"&a"},{UiText.t("Голуб."),"&b"},{UiText.t("Золото"),"&6"},{UiText.t("Жирн."),"&l"},{UiText.t("Курсив"),"&o"},{UiText.t("Подч."),"&n"},{UiText.t("Зачерк."),"&m"},{UiText.t("Магия"),"&k"},{UiText.t("Сброс"),"&r"}};
        int fmtGap=4, fmtW=(fw-fmtGap*4)/5;
        for(int i=0;i<format.length;i++){int col=i%5,row=i/5;ButtonSkin.draw(graphics,this.font,x+22+col*(fmtW+fmtGap),targetY+101+row*20,fmtW,18,format[i][0],mouseX,mouseY);}
        ButtonSkin.draw(graphics, this.font, x + 22, by, bw, 22, UiText.t("Применить имя"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by, bw, 22, UiText.t("Сбросить имя"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 30, bw, 22, UiText.t("Описание"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 30, bw, 22, UiText.t("Зачарования"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 58, bw, 20, UiText.t("Атрибуты"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 58, bw, 20, UiText.t("Настройки моба"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, by + 84, bw, 20, UiText.t("Настройки спавнера"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22 + bw + 8, by + 84, bw, 20, UiText.t("Закрыть"), mouseX, mouseY);
        ButtonSkin.draw(graphics, this.font, x + 22, targetY + 258, fw, 20, UiText.t("Сбросить всё до заводских"), mouseX, mouseY);
        graphics.text(this.font, "ALL-KOTOFF-WANT", x + 28, y + 17, TEXT, false);
        graphics.text(this.font, UiText.t("Редактор предмета  /  01"), x + 28, y + 30, MUTED, false);
        graphics.text(this.font, UiText.t("Название предмета"), x + 22, y + 61, PURPLE_BRIGHT, false);
        ItemStack selected = currentStack();
        String selectedLabel = selected.isEmpty() ? UiText.t("Предмет не выбран") : selected.getHoverName().getString();
        if (!selected.isEmpty()) graphics.item(selected, x + 22, y + 278);
        graphics.text(this.font, selectedLabel, x + (selected.isEmpty() ? 22 : 44), y + 286, MUTED, false);
        graphics.text(this.font, status, x + 22, y + 300, TEXT, false);
        graphics.text(this.font, UiText.t("R  •  ОТКРЫТЬ РЕДАКТОР"), x + 22, y + 314, MUTED, false);
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
