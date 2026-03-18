package lpcCarpetAddition;

import carpet.api.settings.Rule;
import lpcCarpetAddition.Validators.RelimitedEnchantmentsValidator;
import lpcCarpetAddition.features.furnaceClear.FurnaceClearMode;
import org.jetbrains.annotations.NotNull;

import static carpet.api.settings.RuleCategory.*;
import static lpcCarpetAddition.LPCCarpetSettingsData.*;

public class LPCCarpetSettings {
    public static final String LPC = "lpc";
    @Rule(categories = {FEATURE, LPC})
    public static boolean comparatorGetsRealTime = false;
    @Rule(categories = {FEATURE, LPC}, strict = false, options = {"", enchantmentString}, validators = {RelimitedEnchantmentsValidator.class})
    public static @NotNull String relimitedEnchantments = "";
    @Rule(categories = {FEATURE, LPC})
    public static boolean disableAnvilPunishment;
    @Rule(categories = {BUGFIX, LPC})
    public static boolean fakePlayerExperienceDuplicationFix;
    @Rule(categories = {FEATURE, LPC}, options = {"-1", "40"}, strict = false)
    public static int survivalAnvilLimit = 40;
    @Rule(categories = {FEATURE, LPC})
    public static boolean modifyUnbreakingFunction = false;
    @Rule(categories = {COMMAND, LPC})
    public static boolean commandHead = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean furnaceClear = false;
    @Rule(categories = {FEATURE, LPC})
    public static FurnaceClearMode furnaceClearMode = FurnaceClearMode.SMELT_PASS;
}
