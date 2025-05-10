package lpcCarpetAddition;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LPCCarpetSettings {
    @Rule(categories = {RuleCategory.FEATURE})
    public static @NotNull Boolean comparatorGetsRealTime = false;
    //一个Carpet中没有的类，得自己试试看怎么处理了，暂时设为 TODO
    public static @Nullable Block comparatorGetsRealTimeHigh32Block = Blocks.CRYING_OBSIDIAN;
}
