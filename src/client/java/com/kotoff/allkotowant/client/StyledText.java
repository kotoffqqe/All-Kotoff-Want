package com.kotoff.allkotowant.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import java.util.Map;

/** Parses familiar Minecraft legacy-style codes (&a, &l, &n, &m, &k, &o, &r) into styled components. */
final class StyledText {
    private static final Map<Character,Integer> COLORS = Map.ofEntries(
        Map.entry('0',0x000000),Map.entry('1',0x0000AA),Map.entry('2',0x00AA00),Map.entry('3',0x00AAAA),
        Map.entry('4',0xAA0000),Map.entry('5',0xAA00AA),Map.entry('6',0xFFAA00),Map.entry('7',0xAAAAAA),
        Map.entry('8',0x555555),Map.entry('9',0x5555FF),Map.entry('a',0x55FF55),Map.entry('b',0x55FFFF),
        Map.entry('c',0xFF5555),Map.entry('d',0xFF55FF),Map.entry('e',0xFFFF55),Map.entry('f',0xFFFFFF));
    private StyledText() {}

    static Component parse(String raw) {
        MutableComponent result=Component.empty(); StringBuilder part=new StringBuilder();
        Style style=Style.EMPTY; int i=0;
        while(i<raw.length()) {
            if(raw.charAt(i)=='&'&&i+1<raw.length()) {
                if(raw.charAt(i+1)=='#' && i+8<=raw.length()) {
                    String hex=raw.substring(i+2,i+8);
                    try {
                        int rgb=Integer.parseInt(hex,16);
                        if(!part.isEmpty()){result.append(Component.literal(part.toString()).withStyle(style));part.setLength(0);}
                        style=Style.EMPTY.withColor(TextColor.fromRgb(rgb)); i+=8; continue;
                    } catch(NumberFormatException ignored) { /* Treat invalid hex as ordinary text. */ }
                }
                char code=Character.toLowerCase(raw.charAt(i+1));
                if(COLORS.containsKey(code)||"klmnor".indexOf(code)>=0) {
                    if(!part.isEmpty()){result.append(Component.literal(part.toString()).withStyle(style));part.setLength(0);}
                    if(COLORS.containsKey(code)) style=Style.EMPTY.withColor(TextColor.fromRgb(COLORS.get(code)));
                    else style=switch(code){
                        case 'k' -> style.withObfuscated(true); case 'l' -> style.withBold(true);
                        case 'm' -> style.withStrikethrough(true); case 'n' -> style.withUnderlined(true);
                        case 'o' -> style.withItalic(true); default -> Style.EMPTY;
                    };
                    i+=2; continue;
                }
            }
            part.append(raw.charAt(i++));
        }
        if(!part.isEmpty()) result.append(Component.literal(part.toString()).withStyle(style));
        return result;
    }
}
