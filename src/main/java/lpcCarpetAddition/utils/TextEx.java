package lpcCarpetAddition.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static lpcCarpetAddition.utils.DataUtils.*;

@SuppressWarnings("unused")
public class TextEx {
    public static MutableComponent hoverText(MutableComponent text, @Nullable Component hoveredText){
        if(hoveredText == null) text.setStyle(text.getStyle().withHoverEvent(null));
        else text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(hoveredText)));
        return text;
    }
    public static MutableComponent hoverEntity(MutableComponent text, @Nullable Entity entity){
        if(entity == null) text.setStyle(text.getStyle().withHoverEvent(null));
        /*else setStyle(getStyle().withHoverEvent(new HoverEvent.ShowEntity(
                new HoverEvent.EntityContent(entity.getType(), entity.getUuid(), entity.getName()))));*/
        else text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(Component.literal(entity.getUUID().toString()))));
        return text;
    }
    public static MutableComponent setColor(MutableComponent text, TextColor color){
        text.setStyle(text.getStyle().withColor(color));
        return text;
    }
    public static MutableComponent setColor(MutableComponent text, int color){
        text.setStyle(text.getStyle().withColor(color));
        return text;
    }
    public static MutableComponent appendPos(MutableComponent text, Vec3 pos, int color, BracketPair brackets){
        Component nextStr = setColor(Component.literal(brackets.left() + " "), color);
        Component spreadStr = setColor(Component.literal(", "), color);
        for(double p : iterableFrom(pos)){
            text.append(nextStr);
            nextStr = spreadStr;
            text.append(setColor(hoverText(Component.literal(String.format("%.1f", p)), Component.nullToEmpty(String.valueOf(p))), color));
        }
        text.append(setColor(Component.literal(" " + brackets.right()), color));
        return text;
    }
    public static MutableComponent appendPos(MutableComponent text, Vec3 pos){
        Integer color = ChatFormatting.AQUA.getColor();
        return appendPos(text, pos, color != null ? color : 0xffffffff, BracketPair.SQUARE_BRACKETS);
    }
}
