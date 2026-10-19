package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Russian-language enchantment picker with an explicit selectable list. */
public final class EnchantmentEditorScreen extends Screen {
    private record EnchantOption(String id, String name, String hint) {}
    private static final EnchantOption[] OPTIONS = {
        new EnchantOption("sharpness","Острота","Увеличивает урон по большинству мобов."),
        new EnchantOption("smite","Небесная кара","Больше урона нежити: зомби, скелетам и другим."),
        new EnchantOption("bane_of_arthropods","Бич членистоногих","Больше урона паукам и другим членистоногим."),
        new EnchantOption("knockback","Отбрасывание","Сильнее отталкивает цель."),
        new EnchantOption("fire_aspect","Заговор огня","Поджигает цель при ударе."),
        new EnchantOption("looting","Добыча","Повышает количество выпадающих предметов."),
        new EnchantOption("sweeping_edge","Разящий клинок","Усиливает размашистую атаку мечом."),
        new EnchantOption("efficiency","Эффективность","Ускоряет добычу блоков подходящим инструментом."),
        new EnchantOption("silk_touch","Шёлковое касание","Позволяет добывать некоторые блоки целиком."),
        new EnchantOption("fortune","Удача","Повышает количество добычи с некоторых блоков."),
        new EnchantOption("unbreaking","Прочность","Снижает шанс потери прочности."),
        new EnchantOption("mending","Починка","Восстанавливает прочность за счёт опыта."),
        new EnchantOption("power","Сила","Увеличивает урон лука."),
        new EnchantOption("punch","Откидывание","Увеличивает отбрасывание стрелой."),
        new EnchantOption("flame","Воспламенение","Стрелы поджигают цель."),
        new EnchantOption("infinity","Бесконечность","Обычные стрелы не расходуются при наличии одной."),
        new EnchantOption("multishot","Тройной выстрел","Арбалет выпускает три снаряда одновременно."),
        new EnchantOption("piercing","Пронзающая стрела","Снаряд арбалета проходит сквозь цели."),
        new EnchantOption("quick_charge","Быстрая перезарядка","Сокращает время перезарядки арбалета."),
        new EnchantOption("protection","Защита","Снижает большинство видов урона."),
        new EnchantOption("fire_protection","Огнеупорность","Снижает урон от огня и лавы."),
        new EnchantOption("blast_protection","Взрывоустойчивость","Снижает урон от взрывов."),
        new EnchantOption("projectile_protection","Защита от снарядов","Снижает урон от стрел и других снарядов."),
        new EnchantOption("feather_falling","Невесомость","Снижает урон от падения."),
        new EnchantOption("respiration","Подводное дыхание","Позволяет дольше находиться под водой."),
        new EnchantOption("aqua_affinity","Подводник","Ускоряет добычу под водой."),
        new EnchantOption("thorns","Шипы","Наносит ответный урон атакующим."),
        new EnchantOption("depth_strider","Подводная ходьба","Ускоряет передвижение под водой."),
        new EnchantOption("frost_walker","Ледоход","Замораживает воду под ногами."),
        new EnchantOption("soul_speed","Скорость души","Ускоряет ходьбу по песку и почве душ."),
        new EnchantOption("swift_sneak","Проворство","Ускоряет передвижение в приседе."),
        new EnchantOption("luck_of_the_sea","Морская удача","Повышает шанс ценной рыбалки."),
        new EnchantOption("lure","Приманка","Ускоряет поклёвку рыбы."),
        new EnchantOption("impaling","Пронзатель","Увеличивает урон трезубца водным целям."),
        new EnchantOption("loyalty","Верность","Трезубец возвращается после броска."),
        new EnchantOption("riptide","Тягун","Перемещает игрока вместе с брошенным трезубцем в воде/дожде."),
        new EnchantOption("channeling","Громовержец","Во время грозы вызывает молнию при попадании трезубцем."),
        new EnchantOption("density","Плотность","Усиливает удар булавы при падении."),
        new EnchantOption("breach","Пробитие","Снижает эффективность брони цели."),
        new EnchantOption("wind_burst","Порыв ветра","Подбрасывает игрока после удара булавой."),
        new EnchantOption("binding_curse","Проклятие несъёмности","Предмет нельзя снять обычным способом."),
        new EnchantOption("vanishing_curse","Проклятие утраты","Предмет исчезает после смерти."),
    };
    private final Screen parent;
    private int selected;
    private Button choiceButton;
    private EditBox levelField;
    private boolean loadingLevel;
    private String status = UiText.t("Выбери зачарование в списке");
    private long openedAt;

    public EnchantmentEditorScreen(Screen parent) { super(Component.literal(UiText.t("All-Kotoff-Want: Зачарования"))); this.parent = parent; }

    @Override protected void init() {
        try { selected = Math.max(0, Math.min(OPTIONS.length - 1, Integer.parseInt(EditorContext.draft("enchant.selected", "0")))); } catch (NumberFormatException ignored) { selected = 0; }
        openedAt = System.nanoTime(); ButtonSkin.beginAnimation();
        int w = Math.min(440, width - 24), x = (width - w) / 2, y = (height - 250) / 2;
        addRenderableOnly(new PanelWidget(x,y,w,250));
        choiceButton = Button.builder(choiceLabel(), b -> {
            if (minecraft != null) minecraft.setScreen(new CompactPickerScreen(this, UiText.t("Выбрать зачарование"),
                java.util.Arrays.stream(OPTIONS).map(o -> UiText.t(o.name())).toList(), selected, i -> {
                    selected = i; EditorContext.setDraft("enchant.selected", Integer.toString(i)); if (choiceButton != null) choiceButton.setMessage(choiceLabel()); loadSelectedLevel(); status = UiText.t(OPTIONS[selected].hint());
                }));
        })
                .bounds(x+22,y+60,w-44,24).build(); addRenderableWidget(choiceButton);
        levelField = new EditBox(font,x+22,y+111,w-44,22,Component.literal(UiText.t("Уровень зачарования")));
        levelField.setMaxLength(3); levelField.setValue("255"); levelField.setHint(Component.literal(UiText.t("УРОВЕНЬ (1-255)"))); addRenderableWidget(levelField);
        levelField.setResponder(v -> { if (!loadingLevel) EditorContext.setDraft("enchant.level." + OPTIONS[selected].id(), v); });
        loadSelectedLevel();
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить зачарование")), b -> apply()).bounds(x+22,y+149,w-44,22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Убрать это зачарование")), b -> remove()).bounds(x+22,y+177,w-44,22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")), b -> back()).bounds(x+22,y+205,(w-52)/2,20).build());
        addRenderableWidget(Button.builder(Component.literal("Сбросить вкладку"), b -> { EditorContext.resetDraftPrefix("enchant."); if (minecraft != null) minecraft.setScreen(new EnchantmentEditorScreen(parent)); })
                .bounds(x+30+(w-52)/2,y+205,(w-52)/2,20).build());
            ButtonSkin.hideVanillaButtons(this);
    }
    private Component choiceLabel() { return Component.literal(UiText.t("Выбор: ") + UiText.t(OPTIONS[selected].name()) + "  ›"); }
    private void loadSelectedLevel() {
        if (levelField == null) return;
        String key = "enchant.level." + OPTIONS[selected].id();
        String saved = EditorContext.draft(key, "");
        if (!saved.isEmpty()) { loadingLevel = true; levelField.setValue(saved); loadingLevel = false; return; }
        ItemStack stack = EditorContext.stack();
        if (stack.isEmpty()) { loadingLevel = true; levelField.setValue("255"); loadingLevel = false; return; }
        Holder<Enchantment> ench = resolve(OPTIONS[selected].id());
        if (ench == null) { loadingLevel = true; levelField.setValue("255"); loadingLevel = false; return; }
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
            current = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        int existing = current.getLevel(ench);
        loadingLevel = true;
        levelField.setValue(existing > 0 ? Integer.toString(existing) : "255");
        loadingLevel = false;
        EditorContext.setDraft(key, levelField.getValue());
    }
    private Holder<Enchantment> resolve(String raw) {
        if (minecraft == null || minecraft.level == null) return null;
        Identifier id = Identifier.tryParse("minecraft:" + raw);
        if (id == null) return null;
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT,id);
        return minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
    }
    private void apply() {
        ItemStack stack = EditorContext.stack(); if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        int level; try { level = Integer.parseInt(levelField.getValue().trim()); } catch (NumberFormatException e) { status = UiText.t("Уровень должен быть числом от 1 до 255"); return; }
        if (level < 1 || level > 255) { status = UiText.t("Допустимый уровень: от 1 до 255"); return; }
        Holder<Enchantment> ench = resolve(OPTIONS[selected].id()); if (ench == null) { status = UiText.t("Это зачарование отсутствует в текущей версии"); return; }
        boolean stored = stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        DataComponentType<ItemEnchantments> component = stored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(stack.getOrDefault(component,ItemEnchantments.EMPTY));
        mutable.set(ench,level); stack.set(component,mutable.toImmutable());
        status = UiText.t("Добавлено: ") + UiText.t(OPTIONS[selected].name()) + " " + level + ". " + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }
    private void remove() {
        ItemStack stack = EditorContext.stack(); if (stack.isEmpty()) { status = UiText.t("Сначала выбери предмет"); return; }
        Holder<Enchantment> ench = resolve(OPTIONS[selected].id()); if (ench == null) { status = UiText.t("Это зачарование отсутствует в текущей версии"); return; }
        boolean stored = stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        DataComponentType<ItemEnchantments> component = stored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(stack.getOrDefault(component,ItemEnchantments.EMPTY));
        mutable.set(ench,0); stack.set(component,mutable.toImmutable());
        status = UiText.t("Убрано: ") + UiText.t(OPTIONS[selected].name()) + ". " + ItemSync.resultMessage(ItemSync.syncIfCreative(stack));
    }
    private void back() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        EditorContext.renderBackdrop(this,g,mx,my,delta); super.extractRenderState(g,mx,my,delta);
        long elapsed=Math.max(0L,System.nanoTime()-openedAt); float t=Math.min(1f,elapsed/190_000_000f),eased=1f-(float)Math.pow(1f-t,3);
        int w=Math.min(440,width-24),x=(width-w)/2,y=(height-250)/2+Math.round((1f-eased)*12f);
        g.text(font,"ALL-KOTOFF-WANT",x+28,y+17,0xFFF4F0FF,false); g.text(font,UiText.t("ЗАЧАРОВАНИЯ"),x+28,y+30,0xFFB4A9C9,false);
        g.text(font,UiText.t("НАЖМИ, ЧТОБЫ ОТКРЫТЬ СПИСОК"),x+22,y+47,0xFFB49AFF,false);
        ButtonSkin.draw(g,font,x+22,y+60,w-44,24,UiText.t("Выбор: ")+UiText.t(OPTIONS[selected].name())+"  ›",mx,my);
        g.text(font,UiText.t(OPTIONS[selected].hint()),x+22,y+91,0xFFE0D7F4,false); g.text(font,UiText.t("УРОВЕНЬ (1-255)"),x+22,y+101,0xFFB49AFF,false);
        ButtonSkin.draw(g,font,x+22,y+149,w-44,22,UiText.t("Применить зачарование"),mx,my);
        ButtonSkin.draw(g,font,x+22,y+177,w-44,22,UiText.t("Убрать это зачарование"),mx,my); ButtonSkin.draw(g,font,x+22,y+205,(w-52)/2,20,UiText.t("Назад"),mx,my); ButtonSkin.draw(g,font,x+30+(w-52)/2,y+205,(w-52)/2,20,"Сбросить вкладку",mx,my);
        g.text(font,status,x+22,y+235,0xFFF4F0FF,false);
    }
    @Override public void onClose(){back();} @Override public boolean isPauseScreen(){return false;}
}
