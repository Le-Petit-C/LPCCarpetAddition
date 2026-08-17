package lpcCarpetAddition.features.whitelist;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.world.level.GameType;

import java.util.Collection;

public class WhitelistMethods {
	public static void updatePlayersGameMode(MinecraftServer server, Collection<NameAndId> playersToUpdate) {
		PlayerList playerList = server.getPlayerList();
		UserWhiteList whiteList = playerList.getWhiteList();
		for(NameAndId nameAndId : playersToUpdate)
			if(playerList.getPlayer(nameAndId.id()) instanceof ServerPlayer player)
				updatePlayerGameMode(whiteList, player);
	}

	public static void updatePlayerGameMode(UserWhiteList whiteList, ServerPlayer player) {
		if(whiteList.isWhiteListed(player.nameAndId())) {
			GameType gameType = LPCCarpetSettings.whitelistPlayerGameType.getGameType();
			if(gameType != null) player.setGameMode(gameType);
		} else {
			GameType gameType = LPCCarpetSettings.nonWhitelistedPlayerGameType.getTargetGameType();
			if(gameType != null) player.setGameMode(gameType);
		}
	}
}
