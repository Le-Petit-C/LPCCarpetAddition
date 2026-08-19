package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import lpcCarpetAddition.LPCCarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(YggdrasilMinecraftSessionService.class)
public class MinecraftSessionServiceOfflineMixin {
	@WrapOperation(method = "fetchProfile", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService;fetchProfileUncached(Ljava/util/UUID;Z)Lcom/mojang/authlib/yggdrasil/ProfileResult;"))
	ProfileResult skipOfflineFetch(YggdrasilMinecraftSessionService instance, UUID uuid, boolean secureRequest, Operation<ProfileResult> original) {
		if(LPCCarpetSettings.forceOfflineUUIDOnOfflineMode)
			return null;   // 离线：跳过网络查询，等价于 Mojang 查无此 profile
		return original.call(instance, uuid, secureRequest);
	}
}
