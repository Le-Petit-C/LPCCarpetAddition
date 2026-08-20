package lpcCarpetAddition;

import carpet.api.settings.Rule;
import lpcCarpetAddition.features.whitelist.NonWhitelistedPlayerJoinMode;
import lpcCarpetAddition.features.whitelist.WhitelistedPlayerGameMode;
import lpcCarpetAddition.validators.*;
import lpcCarpetAddition.features.furnaceClear.FurnaceClearMode;

import static carpet.api.settings.RuleCategory.*;

public class LPCCarpetSettings {
    public static final String LPC = "lpc";
    @Rule(categories = {FEATURE, LPC})
    public static boolean comparatorGetsRealTime = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean anvilKeepHigherLevels = false;
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
    @Rule(categories = {FEATURE, LPC}, options = {"-1", "0", "1"}, validators = LoveGhastlingValidator.class)
    public static int loveGhastling = 0;
    @Rule(categories = {FEATURE, LPC})
    public static boolean phantomSpawnMonsterCapped = false;
    @Rule(categories = {FEATURE, LPC}, strict = false, options = {"0", "64"})
    public static int phantomSpawnMonsterCappedGlobalExtra = 64;
    @Rule(categories = {FEATURE, LPC}, strict = false, options = {"0", "8"})
    public static int phantomSpawnMonsterCappedLocalExtra = 8;
    @Rule(categories = {FEATURE, LPC}, validators = PhantomsCountAsMonsterValidator.class)
    public static boolean phantomsCountAsMonster = true;
    @Rule(categories = {FEATURE, LPC}, validators = PlayerListRulesValidator.class)
    public static boolean playerListUsesUUID = true;
    @Rule(categories = {FEATURE, LPC}, validators = PlayerListRulesValidator.class)
    public static boolean playerListIgnoreCase = true;
    @Rule(categories = {FEATURE, LPC}, validators = PlayerListRulesValidator.class)
    public static boolean forceOfflineUUIDOnOfflineMode = false;
    @Rule(categories = {FEATURE, LPC}, validators = NonWhitelistValidator.class)
    public static NonWhitelistedPlayerJoinMode nonWhitelistedPlayerGameType = NonWhitelistedPlayerJoinMode.REJECT;
    @Rule(categories = {FEATURE, LPC}, validators = WhitelistValidator.class)
    public static WhitelistedPlayerGameMode whitelistedPlayerGameType = WhitelistedPlayerGameMode.UNSET;
    @Rule(categories = {FEATURE, LPC})
    public static boolean rejectNonWhitelistedPlayerAttackBlock = false;
    @Rule(categories = {FEATURE, COMMAND, LPC})
    public static boolean rejectNonWhitelistedPlayerInteractBlock = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean rejectNonWhitelistedPlayerAttackEntity = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean rejectNonWhitelistedPlayerInteractEntity = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean rejectNonWhitelistedPlayerMoveContainerItem = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean rejectNonWhitelistedPlayerDropOrPickItem = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean rejectNonWhitelistedPlayerEditSign = false;
    @Rule(categories = {FEATURE, COMMAND, LPC})
    public static boolean rejectNonWhitelistedPlayerExecuteServerCommand = false;
    @Rule(categories = {FEATURE, LPC})
    public static boolean nonWhitelistRejectionIgnoreFakePlayers = false;
}
