package lpcCarpetAddition.features.whitelist;

import carpet.patches.EntityPlayerMPFake;
import lpcCarpetAddition.commands.InteractAllowCommand;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.utils.CommandUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class WhitelistMethods {
	public static void scheduleUpdatePlayersGameMode(MinecraftServer server, @Nullable Boolean updateWhitelist) {
		server.schedule(server.wrapRunnable(()->updatePlayersGameMode(server, updateWhitelist)));
	}

	public static void updatePlayersGameMode(MinecraftServer server, @Nullable Boolean updateWhitelist) {
		PlayerList playerList = server.getPlayerList();
		for(ServerPlayer player : server.getPlayerList().getPlayers())
			updatePlayerGameMode(playerList, player.nameAndId(), updateWhitelist);
	}

	public static void updatePlayerGameMode(PlayerList playerList, NameAndId player, @Nullable Boolean updateWhitelist) {
		if(!(playerList.getPlayer(player.id()) instanceof ServerPlayer serverPlayer)) return;
		if(playerList.isWhiteListed(player)) {
			if(updateWhitelist == null || updateWhitelist) {
				GameType gameType = LPCCarpetSettings.whitelistedPlayerGameType.getGameType();
				if(gameType != null) setGameModeAndFeedBack(serverPlayer, gameType);
			}
		} else {
			if(updateWhitelist == null || !updateWhitelist) {
				GameType gameType = LPCCarpetSettings.nonWhitelistedPlayerGameType.getTargetGameType();
				if(gameType != null) setGameModeAndFeedBack(serverPlayer, gameType);
			}
		}
	}

	public static void sendNotWhitelistedMessage(Player player) {
		player.sendOverlayMessage(CommandUtils.fixTranslatedText("carpet.lpc.whitelist.notWhitelisted").withColor(TextColor.DARK_RED));
	}

	public static boolean notWhiteListed(ServerPlayer player) {
		return notWhiteListed(player.level().getServer(), player);
	}

	public static boolean notWhiteListed(MinecraftServer server, Player player) {
		return !server.getPlayerList().isWhiteListed(player.nameAndId());
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

	/**
	 * 判断非白名单玩家在方块交互被拒绝时，该方块是否属于允许交互列表。
	 * 仅在调用方已确认 rejectNonWhitelistedPlayersInteractBlock 开启且玩家非白名单时才有意义。
	 */
	public static boolean shouldAllowBlockInteraction(Level level, BlockHitResult hitResult) {
		return InteractAllowCommand.shouldAllow(level, hitResult.getBlockPos());
	}

	public static boolean shouldReject(MinecraftServer server, Player player) {
		NameAndId playerInfo = player.nameAndId();
		PlayerList playerList = server.getPlayerList();
		if(playerList.isWhiteListed(playerInfo)) return false;
		if(!LPCCarpetSettings.nonWhitelistRejectionIgnoreFakePlayers) return true;
		return !(player instanceof EntityPlayerMPFake);
	}

	public static boolean shouldReject(boolean rejectRuleValue, MinecraftServer server, Player player) {
		if(!rejectRuleValue) return false;
		else return shouldReject(server, player);
	}

	public static boolean shouldReject(boolean rejectRuleValue, ServerPlayer player) {
		return shouldReject(rejectRuleValue, player.level().getServer(), player);
	}

	public static @Nullable MinecraftServer
	notNullIfServerAndShouldReject(boolean rejectRuleValue, Player player) {
		MinecraftServer server = player.level().getServer();
		if(server == null) return null;
		if(!rejectRuleValue) return server;
		else return shouldReject(server, player) ? server : null;
	}
}
