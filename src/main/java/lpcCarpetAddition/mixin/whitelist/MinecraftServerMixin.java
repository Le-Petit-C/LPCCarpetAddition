package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@WrapOperation(method = "kickUnlistedPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
	void onKickingPlayer(ServerGamePacketListenerImpl instance, Component component, Operation<Void> original) {
		GameType gameType = LPCCarpetSettings.nonWhitelistedPlayerGameType.getTargetGameType();
		if(gameType == null) original.call(instance, component);
	}
}
