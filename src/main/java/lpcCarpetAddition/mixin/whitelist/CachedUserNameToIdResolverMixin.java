package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Locale;
import java.util.Optional;

@Mixin(CachedUserNameToIdResolver.class)
public abstract class CachedUserNameToIdResolverMixin {
	@Shadow private boolean resolveOfflineUsers;

	@WrapOperation(method = {"safeAdd", "get(Ljava/lang/String;)Ljava/util/Optional;"}, at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;"))
	String wrapSafeAdd(String instance, Locale locale, Operation<String> original) {
		if(LPCCarpetSettings.playerListIgnoreCase) return original.call(instance, locale);
		else return instance;
	}

	@WrapOperation(method = "lookupGameProfile", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/GameProfileRepository;findProfileByName(Ljava/lang/String;)Ljava/util/Optional;"))
	Optional<NameAndId> skipNetworkLookupOnOffline(GameProfileRepository repo, String name, Operation<Optional<NameAndId>> original) {
		if(LPCCarpetSettings.forceOfflineUUIDOnOfflineMode && resolveOfflineUsers)
			return Optional.empty();   // 让 lookupGameProfile 走 createUnknownProfile → 离线时本地 createOffline
		return original.call(repo, name);
	}
}
