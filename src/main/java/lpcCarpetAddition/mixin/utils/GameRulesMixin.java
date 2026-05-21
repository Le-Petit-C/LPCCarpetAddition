package lpcCarpetAddition.mixin.utils;

import lpcCarpetAddition.utils.CommandUtils;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRules.class)
public class GameRulesMixin {
	@Inject(method = "get", at = @At("RETURN"), cancellable = true)
	<T> void injectGetValueReturn(GameRule<T> gameRule, CallbackInfoReturnable<T> cir) {
		if(cir.getReturnValue() instanceof Boolean
			&& gameRule == GameRules.SEND_COMMAND_FEEDBACK
			&& CommandUtils.getNextFeedBackSuppressed())
			//noinspection unchecked
			cir.setReturnValue((T)(Boolean)false);
	}
}
