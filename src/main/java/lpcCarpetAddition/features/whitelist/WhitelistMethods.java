package lpcCarpetAddition.features.whitelist;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.utils.CommandUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class WhitelistMethods {
	public static void scheduleUpdatePlayersGameMode(MinecraftServer server, @Nullable Boolean updateWhitelist) {
		server.schedule(new TickTask(0, ()->updatePlayersGameMode(server, updateWhitelist)));
	}

	public static void updatePlayersGameMode(MinecraftServer server, @Nullable Boolean updateWhitelist) {
		PlayerList playerList = server.getPlayerList();
		UserWhiteList whiteList = playerList.getWhiteList();
		for(ServerPlayer player : server.getPlayerList().getPlayers())
			updatePlayerGameMode(whiteList, player, updateWhitelist);
	}

	public static void updatePlayersGameMode(MinecraftServer server, Collection<NameAndId> playersToUpdate) {
		PlayerList playerList = server.getPlayerList();
		UserWhiteList whiteList = playerList.getWhiteList();
		for(NameAndId nameAndId : playersToUpdate)
			if(playerList.getPlayer(nameAndId.id()) instanceof ServerPlayer player)
				updatePlayerGameMode(whiteList, player, null);
	}

	public static void updatePlayerGameMode(UserWhiteList whiteList, ServerPlayer player, @Nullable Boolean updateWhitelist) {
		if(whiteList.isWhiteListed(player.nameAndId())) {
			if(updateWhitelist == null || updateWhitelist) {
				GameType gameType = LPCCarpetSettings.whitelistPlayerGameType.getGameType();
				if(gameType != null) setGameModeAndFeedBack(player, gameType);
			}
		} else {
			if(updateWhitelist == null || !updateWhitelist) {
				GameType gameType = LPCCarpetSettings.nonWhitelistedPlayerGameType.getTargetGameType();
				if(gameType != null) setGameModeAndFeedBack(player, gameType);
			}
		}
	}

	public static void sendNotWhitelistedMessage(ServerPlayer player) {
		player.sendSystemMessage(CommandUtils.fixTranslatedText("carpet.lpc.whitelist.notWhitelisted").withColor(TextColor.DARK_RED), true);
	}

	public static boolean notWhiteListed(ServerPlayer player) {
		return !player.level().getServer().getPlayerList().isWhiteListed(player.nameAndId());
	}

	public static void setGameModeAndFeedBack(ServerPlayer player, GameType newType) {
		if(player.setGameMode(newType)) {
			Component mode = Component.translatable("gameMode." + newType.getName());
			player.sendSystemMessage(Component.translatable("gameMode.changed", mode).withColor(TextColor.GRAY));
		}
	}

	public static void sendBlockUpdatePackets(ServerPlayer player, BlockHitResult hitResult) {
		player.resetLastActionTime();
		player.containerMenu.sendAllDataToRemote();
		ServerLevel level = player.level();
		BlockPos pos = hitResult.getBlockPos().immutable();
		Direction direction = hitResult.getDirection();
		player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
		player.connection.send(new ClientboundBlockUpdatePacket(level, pos.relative(direction)));
	}
}
