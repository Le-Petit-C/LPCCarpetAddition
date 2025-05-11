package lpcCarpetAddition;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import lpcCarpetAddition.Validators.RelimitedEnchantmentsValidator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LPCCarpetSettings {
    @Rule(categories = {RuleCategory.FEATURE})
    public static boolean comparatorGetsRealTime = false;
    //一个Carpet中没有的类，得自己试试看怎么处理了，暂时设为 TODO
    public static @Nullable Block comparatorGetsRealTimeHigh32Block = Blocks.CRYING_OBSIDIAN;
    public static final String enchantmentString =
            "minecraft:efficiency;"
                    + "minecraft:protection,20;"
                    + "minecraft:blast_protection,10;"
                    + "minecraft:fire_protection,10;"
                    + "minecraft:projectile_protection,10;"
                    + "minecraft:feather_falling,7;"
                    + "minecraft:unbreaking";
    @Rule(categories = {RuleCategory.FEATURE}, strict = false, options = {"", enchantmentString}, validators = {RelimitedEnchantmentsValidator.class})
    public static @NotNull String relimitedEnchantments = "";
    @Rule(categories = {RuleCategory.FEATURE})
    public static boolean disableAnvilPunishment;
    @Rule(categories = {RuleCategory.BUGFIX})
    public static boolean fakePlayerExperienceDuplicationFix;
}
