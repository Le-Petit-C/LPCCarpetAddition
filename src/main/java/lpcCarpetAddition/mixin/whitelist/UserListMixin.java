package lpcCarpetAddition.mixin.whitelist;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserWhiteList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin({UserWhiteList.class, UserBanList.class})
public class UserListMixin {
	@Inject(method = "getKeyForUser*", at = @At("HEAD"), cancellable = true)
	void injectGetKeyForUserHead(NameAndId user, CallbackInfoReturnable<String> cir) {
		if(!LPCCarpetSettings.playerListUsesUUID)
			cir.setReturnValue(LPCCarpetSettings.playerListIgnoreCase ? user.name().toLowerCase(Locale.ROOT) : user.name());
	}
}
