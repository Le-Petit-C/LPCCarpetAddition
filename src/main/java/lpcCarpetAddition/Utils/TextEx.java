package lpcCarpetAddition.Utils;

import net.minecraft.entity.Entity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpcCarpetAddition.Utils.DataUtils.*;

@SuppressWarnings("unused")
public class TextEx extends MutableText {
    public TextEx(String str) {
        super(PlainTextContent.of(str), new ArrayList<>(), Style.EMPTY);
    }
    public static TextEx of(String str){return new TextEx(str);}
    public static TextEx of(Text text){return new TextEx(text.getString());}
    public static TextEx of(Object obj){return new TextEx(obj.toString());}
    public static TextEx newEmpty(){return new TextEx("");}
    @Override public TextEx append(Text text){
        super.append(text);
        return this;
    }
    public TextEx append(Object obj){
        super.append(obj.toString());
        return this;
    }
    public TextEx append(String str){
        super.append(str);
        return this;
    }
    public TextEx hoverText(@Nullable Text text){
        if(text == null) setStyle(getStyle().withHoverEvent(null));
        else setStyle(getStyle().withHoverEvent(new HoverEvent.ShowText(text)));
        return this;
    }
    public TextEx hoverEntity(@Nullable Entity entity){
        if(entity == null) setStyle(getStyle().withHoverEvent(null));
        /*else setStyle(getStyle().withHoverEvent(new HoverEvent.ShowEntity(
                new HoverEvent.EntityContent(entity.getType(), entity.getUuid(), entity.getName()))));*/
        else setStyle(getStyle().withHoverEvent(new HoverEvent.ShowText(TextEx.of(entity.getUuid()))));
        return this;
    }
    public TextEx setColor(TextColor color){
        setStyle(getStyle().withColor(color));
        return this;
    }
    public TextEx setColor(int color){
        setStyle(getStyle().withColor(color));
        return this;
    }
    public TextEx appendPos(Vec3d pos, int color, BracketPair brackets){
        Text nextStr = of(brackets.left() + " ").setColor(color);
        Text spreadStr = of(", ").setColor(color);
        for(double p : iterableFrom(pos)){
            append(nextStr);
            nextStr = spreadStr;
            append(new TextEx(String.format("%.1f", p)).hoverText(TextEx.of(p)).setColor(color));
        }
        append(of(" " + brackets.right()).setColor(color));
        return this;
    }
    public TextEx appendPos(Vec3d pos){
        Integer color = Formatting.AQUA.getColorValue();
        return appendPos(pos, color != null ? color : 0xffffffff, BracketPair.SQUARE_BRACKETS);
    }
}
