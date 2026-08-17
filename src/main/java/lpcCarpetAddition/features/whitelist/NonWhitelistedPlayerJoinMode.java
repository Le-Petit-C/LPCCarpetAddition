package lpcCarpetAddition.features.whitelist;

import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public enum NonWhitelistedPlayerJoinMode {
	REJECT(null),
	SURVIVAL(GameType.SURVIVAL),
	CREATIVE(GameType.CREATIVE),
	ADVENTURE(GameType.ADVENTURE),
	SPECTATOR(GameType.SPECTATOR);
	private final @Nullable GameType targetGameType;
	NonWhitelistedPlayerJoinMode(@Nullable GameType targetGameType) {
		this.targetGameType = targetGameType;
	}

	public @Nullable GameType getTargetGameType() { return targetGameType; }
}
