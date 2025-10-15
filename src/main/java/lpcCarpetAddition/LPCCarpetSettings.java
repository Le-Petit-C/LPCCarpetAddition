package lpcCarpetAddition;

import carpet.api.settings.Rule;
import lpcCarpetAddition.Validators.RelimitedEnchantmentsValidator;
import org.jetbrains.annotations.NotNull;

import static carpet.api.settings.RuleCategory.*;
import static lpcCarpetAddition.LPCCarpetSettingsData.*;

public class LPCCarpetSettings {
    @Rule(categories = {FEATURE})
    public static boolean comparatorGetsRealTime = false;
    @Rule(categories = {FEATURE}, strict = false, options = {"", enchantmentString}, validators = {RelimitedEnchantmentsValidator.class})
    public static @NotNull String relimitedEnchantments = "";
    @Rule(categories = {FEATURE})
    public static boolean disableAnvilPunishment;
    @Rule(categories = {BUGFIX})
    public static boolean fakePlayerExperienceDuplicationFix;
    @Rule(categories = {FEATURE}, options = {"-1", "40"}, strict = false)
    public static int survivalAnvilLimit = 40;
    @Rule(categories = {FEATURE})
    public static boolean modifyUnbreakingFunction = false;
}
