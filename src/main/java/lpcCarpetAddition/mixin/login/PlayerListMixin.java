package lpcCarpetAddition.mixin.login;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.features.whitelist.NonWhitelistedPlayerJoinMode;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow public abstract UserWhiteList getWhiteList();

	@WrapOperation(method = {"getPlayerByName", "placeNewPlayer", "getPlayer(Ljava/lang/String;)Lnet/minecraft/server/level/ServerPlayer;"}, at = @At(value = "INVOKE", target = "Ljava/lang/String;equalsIgnoreCase(Ljava/lang/String;)Z"))
	boolean modifyEqualsIgnoreCase(String instance, String anotherString, Operation<Boolean> original) {
		if(LPCCarpetSettings.playerListIgnoreCase) return original.call(instance, anotherString);
		else return instance.equals(anotherString);
	}

	@WrapOperation(method = "canPlayerLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;isWhiteListed(Lnet/minecraft/server/players/NameAndId;)Z"))
	boolean modifyCanLoginIsWhiteListed(PlayerList instance, NameAndId nameAndId, Operation<Boolean> original) {
		if(LPCCarpetSettings.nonWhitelistedPlayerGameType != NonWhitelistedPlayerJoinMode.REJECT) return true;
		return original.call(instance, nameAndId);
	}

	@Inject(method = "placeNewPlayer", at = @At("TAIL"))
	void onPlaceNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		WhitelistMethods.updatePlayerGameMode(getWhiteList(), player);
	}
}
