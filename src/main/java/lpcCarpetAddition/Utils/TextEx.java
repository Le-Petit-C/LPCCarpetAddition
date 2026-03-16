package lpcCarpetAddition.Utils;

import net.minecraft.entity.Entity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static lpcCarpetAddition.Utils.DataUtils.*;

@SuppressWarnings("unused")
public class TextEx {
    public static MutableText hoverText(MutableText text, @Nullable Text hoveredText){
        if(hoveredText == null) text.setStyle(text.getStyle().withHoverEvent(null));
        else text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(hoveredText)));
        return text;
    }
    public static MutableText hoverEntity(MutableText text, @Nullable Entity entity){
        if(entity == null) text.setStyle(text.getStyle().withHoverEvent(null));
        /*else setStyle(getStyle().withHoverEvent(new HoverEvent.ShowEntity(
                new HoverEvent.EntityContent(entity.getType(), entity.getUuid(), entity.getName()))));*/
        else text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(Text.literal(entity.getUuid().toString()))));
        return text;
    }
    public static MutableText setColor(MutableText text, TextColor color){
        text.setStyle(text.getStyle().withColor(color));
        return text;
    }
    public static MutableText setColor(MutableText text, int color){
        text.setStyle(text.getStyle().withColor(color));
        return text;
    }
    public static MutableText appendPos(MutableText text, Vec3d pos, int color, BracketPair brackets){
        Text nextStr = setColor(Text.literal(brackets.left() + " "), color);
        Text spreadStr = setColor(Text.literal(", "), color);
        for(double p : iterableFrom(pos)){
            text.append(nextStr);
            nextStr = spreadStr;
            text.append(setColor(hoverText(Text.literal(String.format("%.1f", p)), Text.of(String.valueOf(p))), color));
        }
        text.append(setColor(Text.literal(" " + brackets.right()), color));
        return text;
    }
    public static MutableText appendPos(MutableText text, Vec3d pos){
        Integer color = Formatting.AQUA.getColorValue();
        return appendPos(text, pos, color != null ? color : 0xffffffff, BracketPair.SQUARE_BRACKETS);
    }
}
