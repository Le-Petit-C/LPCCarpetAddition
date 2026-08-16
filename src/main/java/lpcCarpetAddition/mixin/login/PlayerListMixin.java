package lpcCarpetAddition.mixin.login;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@WrapOperation(method = {"getPlayerByName", "placeNewPlayer", "getPlayer(Ljava/lang/String;)Lnet/minecraft/server/level/ServerPlayer;"}, at = @At(value = "INVOKE", target = "Ljava/lang/String;equalsIgnoreCase(Ljava/lang/String;)Z"))
	boolean modifyEqualsIgnoreCase(String instance, String anotherString, Operation<Boolean> original) {
		if(LPCCarpetSettings.playerListIgnoreCase) return original.call(instance, anotherString);
		else return instance.equals(anotherString);
	}
}
