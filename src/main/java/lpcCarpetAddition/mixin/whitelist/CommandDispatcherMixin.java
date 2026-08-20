package lpcCarpetAddition.mixin.whitelist;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.commands.CommandAllowCommand;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin {
	@Inject(method = "register", at = @At("HEAD"))
	private static <S> void modifyInitRequirement(LiteralArgumentBuilder<S> command, CallbackInfoReturnable<LiteralCommandNode<S>> cir) {
		String literal = command.getLiteral();
		Predicate<S> oldRequirement = command.getRequirement();
		command.requires(source -> {
			if(!oldRequirement.test(source)) return false;
			if(!(source instanceof CommandSourceStack stack)) return true;
			if(!(stack.getPlayer() instanceof ServerPlayer player)) return true;
			if(!WhitelistMethods.shouldReject(LPCCarpetSettings.rejectNonWhitelistedPlayerExecuteServerCommand, player)) return true;
			return CommandAllowCommand.isAllowed(stack.getServer(), literal);
		});
	}
}
