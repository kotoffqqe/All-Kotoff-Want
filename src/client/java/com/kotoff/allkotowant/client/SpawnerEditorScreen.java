package com.kotoff.allkotowant.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Edits the spawner the player is actually looking at (not the block below them). */
public final class SpawnerEditorScreen extends Screen {
    private record Mob(String id, String name) {}
    private static final Mob[] MOBS = {
        new Mob("minecraft:zombie","Зомби"), new Mob("minecraft:skeleton","Скелет"),
        new Mob("minecraft:creeper","Крипер"), new Mob("minecraft:spider","Паук"),
        new Mob("minecraft:cave_spider","Пещерный паук"), new Mob("minecraft:witch","Ведьма"),
        new Mob("minecraft:blaze","Ифрит"), new Mob("minecraft:enderman","Эндермен"),
        new Mob("minecraft:pig","Свинья"), new Mob("minecraft:cow","Корова"),
        new Mob("minecraft:sheep","Овца"), new Mob("minecraft:chicken","Курица"),
        new Mob("minecraft:iron_golem","Железный голем"), new Mob("minecraft:warden","Хранитель"),
        new Mob("minecraft:ender_dragon","Дракон Края"), new Mob("minecraft:wither","Иссушитель")
    };
    private final Screen parent;
    private int mobIndex;
    private Button mobButton;
    private EditBox delay, minDelay, maxDelay, count, range;
    private String status = UiText.t("Наведи прицел на спавнер");
    private long openedAt;

    public SpawnerEditorScreen(Screen parent) { super(Component.literal(UiText.t("All-Kotoff-Want: Спавнер"))); this.parent = parent; }
    @Override protected void init() {
        try { mobIndex = Math.max(0, Math.min(MOBS.length - 1, Integer.parseInt(EditorContext.draft("spawner.mob", "0")))); } catch (NumberFormatException ignored) { mobIndex = 0; }
        openedAt=System.nanoTime(); ButtonSkin.beginAnimation();
        int w=Math.min(440,width-24),x=(width-w)/2,y=(height-300)/2;
        addRenderableOnly(new PanelWidget(x,y,w,300));
        mobButton=Button.builder(mobLabel(),b->{ if(minecraft!=null) minecraft.setScreen(new CompactPickerScreen(this, UiText.t("Выбрать моба"), java.util.Arrays.stream(MOBS).map(m->UiText.t(m.name())).toList(), mobIndex, i->{mobIndex=i;EditorContext.draft("spawner.mob", Integer.toString(i));if(mobButton!=null)mobButton.setMessage(mobLabel());})); })
                .bounds(x+22,y+56,w-44,24).build(); addRenderableWidget(mobButton);
        delay=field(x+22,y+100,(w-52)/2,UiText.t("Задержка (тики)"),EditorContext.draft("spawner.delay","20"),"spawner.delay");
        minDelay=field(x+30+(w-52)/2,y+100,(w-52)/2,UiText.t("Мин. задержка"),EditorContext.draft("spawner.min","200"),"spawner.min");
        maxDelay=field(x+22,y+144,(w-52)/2,UiText.t("Макс. задержка"),EditorContext.draft("spawner.max","800"),"spawner.max");
        count=field(x+30+(w-52)/2,y+144,(w-52)/2,UiText.t("Кол-во мобов"),EditorContext.draft("spawner.count","4"),"spawner.count");
        range=field(x+22,y+188,w-44,UiText.t("Радиус активации"),EditorContext.draft("spawner.range","16"),"spawner.range");
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Применить к спавнеру под прицелом")),b->apply()).bounds(x+22,y+222,w-44,22).build());
        addRenderableWidget(Button.builder(Component.literal(UiText.t("Назад")),b->{if(minecraft!=null)minecraft.setScreen(parent);}).bounds(x+22,y+250,w-44,20).build());
    }
    private Component mobLabel(){return Component.literal(UiText.t("Моб: ")+UiText.t(MOBS[mobIndex].name())+"  ›");}
    private EditBox field(int x,int y,int w,String hint,String value,String key){EditBox b=new EditBox(font,x,y,w,20,Component.literal(hint));b.setMaxLength(8);b.setValue(value);b.setResponder(v -> EditorContext.draft(key,v));addRenderableWidget(b);return b;}
    private Integer number(EditBox box,int min,int max){try{int n=Integer.parseInt(box.getValue().trim());return n>=min&&n<=max?n:null;}catch(NumberFormatException e){return null;}}
    private void apply(){
        if(minecraft==null||minecraft.player==null||minecraft.level==null){status=UiText.t("Мир ещё не загружен");return;}
        if(!(minecraft.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK){status=UiText.t("Наведи прицел прямо на блок спавнера");return;}
        BlockPos pos=hit.getBlockPos();
        if(!minecraft.level.getBlockState(pos).is(Blocks.SPAWNER)){status=UiText.t("Под прицелом не спавнер. Наведи на клетку со спавнером");return;}
        Integer d=number(delay,0,32767),min=number(minDelay,1,32767),max=number(maxDelay,1,32767),c=number(count,1,32767),r=number(range,1,128);
        if(d==null||min==null||max==null||c==null||r==null||min>max){status=UiText.t("Проверь числа: мин. задержка не больше макс.");return;}
        String command="data merge block "+pos.getX()+" "+pos.getY()+" "+pos.getZ()+" {Delay:"+d+",MinSpawnDelay:"+min+",MaxSpawnDelay:"+max+",SpawnCount:"+c+",RequiredPlayerRange:"+r+",SpawnData:{entity:{id:\""+MOBS[mobIndex].id()+"\"}}}";
        minecraft.player.connection.sendCommand(command); status=UiText.t("Команда отправлена. Нужны права оператора/команд");
    }
    private void back(){if(minecraft!=null)minecraft.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        EditorContext.renderBackdrop(this,g,mx,my,delta);super.extractRenderState(g,mx,my,delta);
        long elapsed=Math.max(0L,System.nanoTime()-openedAt);float t=Math.min(1f,elapsed/190_000_000f),eased=1f-(float)Math.pow(1f-t,3);
        int w=Math.min(440,width-24),x=(width-w)/2,y=(height-300)/2+Math.round((1f-eased)*10f);
        g.text(font,"ALL-KOTOFF-WANT",x+28,y+17,0xFFF4F0FF,false);g.text(font,UiText.t("НАСТРОЙКА СПАВНЕРА  /  07"),x+28,y+30,0xFFB4A9C9,false);
        g.text(font,UiText.t("НАЖМИ, ЧТОБЫ ОТКРЫТЬ СПИСОК"),x+22,y+44,0xFFB49AFF,false);
        ButtonSkin.draw(g,font,x+22,y+56,w-44,24,UiText.t("Моб: ")+UiText.t(MOBS[mobIndex].name())+"  ›",mx,my);
        g.text(font,UiText.t("ЗАДЕРЖКА / МИНИМУМ"),x+22,y+88,0xFFB49AFF,false);g.text(font,UiText.t("МАКСИМУМ / КОЛИЧЕСТВО"),x+22,y+132,0xFFB49AFF,false);g.text(font,UiText.t("РАДИУС АКТИВАЦИИ"),x+22,y+176,0xFFB49AFF,false);
        ButtonSkin.draw(g,font,x+22,y+222,w-44,22,UiText.t("Применить к спавнеру под прицелом"),mx,my);ButtonSkin.draw(g,font,x+22,y+250,w-44,20,UiText.t("Назад"),mx,my);
        g.text(font,status,x+22,y+282,0xFFEAE2FF,false);
    }
    @Override public void onClose(){back();}@Override public boolean isPauseScreen(){return false;}
}
