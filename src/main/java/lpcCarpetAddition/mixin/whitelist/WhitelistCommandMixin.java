package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.commands.WhitelistPermitCommand;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Mixin(WhitelistCommand.class)
public class WhitelistCommandMixin {
	@ModifyArg(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"))
	private static Predicate<CommandSourceStack> allowExtraWhitelistUsers(Predicate<CommandSourceStack> predicate) {
		return source -> predicate.test(source) || WhitelistPermitCommand.isPermitted(source);
	}

	@Inject(method = {"addPlayers"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/UserWhiteList;add(Lnet/minecraft/server/players/UserWhiteListEntry;)Z", shift = At.Shift.AFTER))
	private static void playerAddedToWhiteList(CommandSourceStack source, Collection<NameAndId> targets, CallbackInfoReturnable<Integer> cir, @Local(name = "target") NameAndId target) {
		WhitelistMethods.updatePlayersGameMode(source.getServer(), List.of(target));
	}
	@Inject(method = {"removePlayers"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/UserWhiteList;remove(Lnet/minecraft/server/players/StoredUserEntry;)Z", shift = At.Shift.AFTER))
	private static void playerRemovedFromWhiteList(CommandSourceStack source, Collection<NameAndId> targets, CallbackInfoReturnable<Integer> cir, @Local(name = "target") NameAndId target) {
		WhitelistMethods.updatePlayersGameMode(source.getServer(), List.of(target));
	}
}
