package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Creates a summon command with a selected vehicle and passenger. */
public final class PassengerEditorScreen extends Screen {
    private record Mob(String id, String name) {}
    private static final Mob[] MOBS = {
        new Mob("minecraft:zombie","Зомби"), new Mob("minecraft:skeleton","Скелет"), new Mob("minecraft:creeper","Крипер"),
        new Mob("minecraft:spider","Паук"), new Mob("minecraft:chicken","Курица"), new Mob("minecraft:pig","Свинья"),
        new Mob("minecraft:cow","Корова"), new Mob("minecraft:iron_golem","Железный голем"), new Mob("minecraft:horse","Лошадь"),
        new Mob("minecraft:oak_boat","Дубовая лодка"), new Mob("minecraft:minecart","Вагонетка"), new Mob("minecraft:warden","Хранитель")
    };
    private final Screen parent;
    private int vehicleIndex, passengerIndex = 4;
    private String status = UiText.t("Выбери нижнего моба и пассажира сверху");
    private long openedAt;
    public PassengerEditorScreen(Screen parent) { super(Component.literal(UiText.t("Посадка моба на моба"))); this.parent = parent; }
    @Override protected void init() {
        openedAt=System.nanoTime(); ButtonSkin.beginAnimation();
        int w=Math.min(420,width-24),x=(width-w)/2,y=(height-220)/2;
        addRenderableOnly(new PanelWidget(x,y,w,220));
        addRenderableWidget(Button.builder(vehicleLabel(),b->{vehicleIndex=(vehicleIndex+1)%MOBS.length;b.setMessage(vehicleLabel());}).bounds(x+22,y+62,w-44,22).build());
        addRenderableWidget(Button.builder(passengerLabel(),b->{passengerIndex=(passengerIndex+1)%MOBS.length;b.setMessage(passengerLabel());}).bounds(x+22,y+108,w-44,22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Создать мобов")),b->summon()).bounds(x+22,y+146,w-44,22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")),b->back()).bounds(x+22,y+174,w-44,20).build());
    }
    private Component vehicleLabel(){return Component.literal(UiText.t("Нижний моб: ")+UiText.t(MOBS[vehicleIndex].name())+"  ›");}
    private Component passengerLabel(){return Component.literal(UiText.t("Пассажир сверху: ")+UiText.t(MOBS[passengerIndex].name())+"  ›");}
    private void summon(){
        if(minecraft!=null&&minecraft.player!=null){
            minecraft.player.connection.sendCommand("summon "+MOBS[vehicleIndex].id()+" ~ ~ ~ {Passengers:[{id:\""+MOBS[passengerIndex].id()+"\"}]}");
            status=UiText.t("Команда отправлена; на сервере нужны права на команды");
        }
    }
    private void back(){if(minecraft!=null)minecraft.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        EditorContext.renderBackdrop(this,g,mx,my,delta);super.extractRenderState(g,mx,my,delta);
        int w=Math.min(420,width-24),x=(width-w)/2,y=(height-220)/2;
        long elapsed=Math.max(0L,System.nanoTime()-openedAt);float t=Math.min(1f,elapsed/190_000_000f),eased=1f-(float)Math.pow(1f-t,3);y+=Math.round((1f-eased)*10f);
        g.text(font,"ALL-KOTOFF-WANT",x+28,y+17,0xFFF4F0FF,false);g.text(font,UiText.t("ПОСАДКА МОБОВ  /  06"),x+28,y+31,0xFFB4A9C9,false);
        g.text(font,UiText.t("НАЖИМАЙ НА КНОПКИ, ЧТОБЫ ВЫБРАТЬ"),x+22,y+49,0xFFB49AFF,false);
        ButtonSkin.draw(g,font,x+22,y+62,w-44,22,UiText.t("Нижний моб: ")+UiText.t(MOBS[vehicleIndex].name())+"  ›",mx,my);
        ButtonSkin.draw(g,font,x+22,y+108,w-44,22,UiText.t("Пассажир сверху: ")+UiText.t(MOBS[passengerIndex].name())+"  ›",mx,my);
        ButtonSkin.draw(g,font,x+22,y+146,w-44,22,UiText.t("Создать мобов"),mx,my);ButtonSkin.draw(g,font,x+22,y+174,w-44,20,UiText.t("Назад"),mx,my);
        g.text(font,status,x+22,y+203,0xFFEAE2FF,false);
    }
    @Override public void onClose(){back();}@Override public boolean isPauseScreen(){return false;}
}
