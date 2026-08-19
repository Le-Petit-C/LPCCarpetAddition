package lpcCarpetAddition.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.utils.CommandUtils;
import lpcCarpetAddition.utils.DataUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.*;
import net.minecraft.util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WhitelistExtraCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> addExtraCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
		return builder
			.then(Commands.literal("refresh")
				.executes(WhitelistExtraCommand::refreshWhitelist)
				.then(Commands.literal("help")
					.executes(ctx -> {
						ctx.getSource().sendSystemMessage(Component.literal(CommandUtils.loadHelpText("whitelist")));
						return 1;
					})
				)
			);
	}

	private static int refreshWhitelist(CommandContext<CommandSourceStack> context) {
		MinecraftServer server = context.getSource().getServer();
		UserWhiteList whiteList = server.getPlayerList().getWhiteList();
		context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.whitelist.refreshing"), true);
		List<NameAndId> origin = whiteList.getEntries().stream()
			.map(StoredUserEntry::getUser).filter(Objects::nonNull).distinct().toList();
		// 两个方向的网络解析都已被全局 Mixin 本地化（离线零网络）：
		//   name → UUID：CachedUserNameToIdResolver.lookupGameProfile 内的 findProfileByName
		//   UUID → name：YggdrasilMinecraftSessionService.fetchProfile 内的 fetchProfileUncached
		// 这里按 playerListUsesUUID 决定用名字或 UUID 解析（在线服支持玩家改名场景），
		// 用 MC 全局共享的 IO 池异步执行，避免卡主线程
		ProfileResolver resolver = server.services().profileResolver();
		List<CompletableFuture<NameAndId>> refreshing = origin.stream()
			.map(id -> CompletableFuture.supplyAsync(()->{
				try {
					return resolver.fetchByNameOrId(DataUtils.playerIdentifier(id))
						.map(NameAndId::new).orElse(id);   // 查不到就保留原条目
				} catch (RuntimeException e) {
					LPCCarpetAddition.LOGGER.warn("whitelist refresh: failed to resolve {}", id, e);
					return id;                            // 网络异常也保留
				}
			}, Util.ioPool())).toList();
		CompletableFuture.allOf(refreshing.toArray(new CompletableFuture[0])).thenRunAsync(()->{
			List<NameAndId> refreshed = refreshing.stream().map(CompletableFuture::join).toList();
			applyChanges(context, whiteList, origin, refreshed);
		}, context.getSource().getServer())
			.exceptionally(e -> {
				LPCCarpetAddition.LOGGER.warn("whitelist refresh: failed", e);
				return null;
			});
		return 1;
	}

	private static void applyChanges(CommandContext<CommandSourceStack> context, UserWhiteList whiteList, List<NameAndId> origin, List<NameAndId> refreshed) {
		Set<NameAndId> both = new HashSet<>(origin);
		both.retainAll(refreshed);
		int changed = 0;
		MutableComponent changeText = Component.empty();
		for (NameAndId player : origin) {
			if(both.contains(player)) continue;
			try {
				if (whiteList.remove(player)) {
					++changed;
					changeText.append(Component.literal("\n  - " + player.name() + " (" + player.id() + ")").withColor(TextColor.RED));
				}
			} catch (RuntimeException e) {
				LPCCarpetAddition.LOGGER.warn("whitelist refresh: failed to remove {}", player, e);
			}
		}
		for (NameAndId player : refreshed) {
			if(both.contains(player)) continue;
			try {
				if (whiteList.add(new UserWhiteListEntry(player))) {
					++changed;
					changeText.append(Component.literal("  + " + player.name() + " (" + player.id() + ")").withColor(TextColor.GREEN));
				}
			} catch (RuntimeException e) {
				LPCCarpetAddition.LOGGER.warn("whitelist refresh: failed to add {}", player, e);
			}
		}
		int finalChanged = changed;
		MutableComponent message = CommandUtils.fixTranslatedText("carpet.lpc.command.whitelist.refreshed", finalChanged, changeText);
		context.getSource().sendSuccess(() -> message, true);
	}
}
