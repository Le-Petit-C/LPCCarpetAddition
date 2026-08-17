package lpcCarpetAddition.features.whitelist;

import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public enum WhitelistedPlayerGameMode {
	UNSET(null),
	SURVIVAL(GameType.SURVIVAL),
	CREATIVE(GameType.CREATIVE),
	ADVENTURE(GameType.ADVENTURE),
	SPECTATOR(GameType.SPECTATOR);
	private final @Nullable GameType gameType;
	WhitelistedPlayerGameMode(@Nullable GameType gameType) { this.gameType = gameType; }

	public @Nullable GameType getGameType() { return gameType; }
}
