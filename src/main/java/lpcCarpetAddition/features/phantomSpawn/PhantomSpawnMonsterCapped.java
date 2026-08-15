package lpcCarpetAddition.features.phantomSpawn;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.mixin.accessors.LocalMobCapCalculatorAccessor;
import lpcCarpetAddition.mixin.accessors.NaturalSpawnerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.jspecify.annotations.Nullable;

public class PhantomSpawnMonsterCapped {
	public static NaturalSpawner.@Nullable SpawnState capturedSpawnState = null;
	public static boolean isValidGlobally() {
		if(capturedSpawnState == null) return true;
		MobCategory category = MobCategory.MONSTER;
		int globalMaxCount = category.getMaxInstancesPerChunk() * capturedSpawnState.getSpawnableChunkCount() / (17 * 17);
		return capturedSpawnState.getMobCategoryCounts().getOrDefault(category, 0) < globalMaxCount + LPCCarpetSettings.phantomSpawnMonsterCappedGlobalExtra;
	}
	public static boolean isValidLocally(ServerPlayer player) {
		if(capturedSpawnState == null) return true;
		MobCategory category = MobCategory.MONSTER;
		var stateAccessor = (NaturalSpawnerAccessor.SpawnStateAccessor)capturedSpawnState;
		var calculatorAccessor = (LocalMobCapCalculatorAccessor)stateAccessor.getLocalMobCapCalculator();
		var mobCountsAccessor = (LocalMobCapCalculatorAccessor.MobCountsAccessor)calculatorAccessor.getPlayerMobCounts().getOrDefault(player, null);
		if(mobCountsAccessor != null) return mobCountsAccessor.getCounts().getOrDefault(category, 0) < category.getMaxInstancesPerChunk() + LPCCarpetSettings.phantomSpawnMonsterCappedLocalExtra;
		return true;
	}
}
