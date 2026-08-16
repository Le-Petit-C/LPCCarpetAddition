package lpcCarpetAddition.mixin.login;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Locale;

@Mixin(CachedUserNameToIdResolver.class)
public class CachedUserNameToIdResolverMixin {
	@WrapOperation(method = {"safeAdd", "get(Ljava/lang/String;)Ljava/util/Optional;"}, at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;"))
	String wrapSafeAdd(String instance, Locale locale, Operation<String> original) {
		if(LPCCarpetSettings.playerListIgnoreCase) return original.call(instance, locale);
		else return instance;
	}
}
