package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.commands.WhitelistExtraCommand;
import lpcCarpetAddition.commands.WhitelistPermitCommand;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import lpcCarpetAddition.mixinUtils.AccessorUtils;
import lpcCarpetAddition.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(WhitelistCommand.class)
public class WhitelistCommandMixin {
	@ModifyArg(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"))
	private static Predicate<CommandSourceStack> allowExtraWhitelistUsers(Predicate<CommandSourceStack> predicate) {
		return source -> predicate.test(source) || WhitelistPermitCommand.isPermitted(source);
	}
	@WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", ordinal = 0))
	private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> addExtraSubCommand(LiteralArgumentBuilder<CommandSourceStack> instance, Predicate<CommandSourceStack> predicate, Operation<ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>>> original) {
		return original.call(WhitelistExtraCommand.addExtraCommand(AccessorUtils.asAccessor(instance).invokeGetThis()), predicate);
	}
	@WrapOperation(method = "addPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/UserWhiteList;add(Lnet/minecraft/server/players/UserWhiteListEntry;)Z"))
	private static boolean wrapAddPlayersAdd(
		UserWhiteList instance, UserWhiteListEntry infos, Operation<Boolean> original,
		@Local(name = "source") final CommandSourceStack source, @Local(name = "target") NameAndId target
	) {
		boolean res = original.call(instance, infos);
		if(res && LPCCarpetSettings.rejectNonWhitelistedPlayerExecuteServerCommand)
			CommandUtils.refreshCommandTree(source.getServer(), target);
		WhitelistMethods.updatePlayerGameMode(source.getServer().getPlayerList(), target, null);
		return res;
	}
	@WrapOperation(method = "removePlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/UserWhiteList;remove(Lnet/minecraft/server/players/StoredUserEntry;)Z"))
	private static boolean wrapRemovePlayersRemove(
		UserWhiteList instance, StoredUserEntry<UserWhiteListEntry> storedUserEntry, Operation<Boolean> original,
		@Local(name = "source") final CommandSourceStack source, @Local(name = "target") NameAndId target
	) {
		boolean res = original.call(instance, storedUserEntry);
		if(res && LPCCarpetSettings.rejectNonWhitelistedPlayerExecuteServerCommand)
			CommandUtils.refreshCommandTree(source.getServer(), target);
		WhitelistMethods.updatePlayerGameMode(source.getServer().getPlayerList(), target, null);
		return res;
	}
}
